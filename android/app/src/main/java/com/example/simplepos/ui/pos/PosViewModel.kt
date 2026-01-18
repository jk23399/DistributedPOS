package com.jun.simplepos.ui.pos

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jun.simplepos.data.MenuItem
import com.jun.simplepos.data.OrderDao
import com.jun.simplepos.data.OrderItem
import com.jun.simplepos.ui.receipt.PrinterManager
import com.jun.simplepos.ui.receipt.ReceiptFormatter
import com.jun.simplepos.ui.receipt.ReceiptData
import com.jun.simplepos.ui.receipt.ReceiptItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.jun.simplepos.network.RetrofitClient

data class ServerOrderItem(
    val orderId: Int,
    val menuId: Int,
    val menuName: String,
    val price: Double,
    val quantity: Int
)

data class CartItem(
    val menuItem: MenuItem,
    var quantity: Int,
    val selectedOptions: String? = null,
    var memo: String = ""
)

data class PosUiState(
    val orderedItems: List<OrderItem> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val discountRate: Double = 0.0,
    val tax: Double = 0.0,
    val taxRate: Double = 0.0,
    val gratuity: Double = 0.0,
    val gratuityRate: Double = 0.0,
    val total: Double = 0.0,
    val hasUnsavedChanges: Boolean = false
)

class PosViewModel(
    private val orderDao: OrderDao,
    private val tableId: Int,
    private val taxState: StateFlow<TaxState>,
    private val gratuityRate: StateFlow<Double>,
    private val discountRate: StateFlow<Double>,
    private val context: Context
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    private val _unsavedChanges = MutableStateFlow<Map<Int, OrderItem>>(emptyMap())
    private val openOrderFlow = orderDao.getOpenOrderForTable(tableId)

    val uiState: StateFlow<PosUiState> = combine(
        openOrderFlow, _cartItems, taxState, gratuityRate, discountRate, _unsavedChanges
    ) { flows ->
        val openOrder = flows[0] as? com.jun.simplepos.data.FullOrder
        val cartItems = flows[1] as List<CartItem>
        val currentTax = flows[2] as TaxState
        val currentGratuityRate = flows[3] as Double
        val currentDiscountRate = flows[4] as Double
        val unsavedChanges = flows[5] as Map<Int, OrderItem>

        val baseOrderedItems = openOrder?.items ?: emptyList()
        val currentOrderedItems = baseOrderedItems.map {
            unsavedChanges[it.id] ?: it
        }

        val activeItems = currentOrderedItems.filter { it.status == "ORDERED" }
        val subtotal = activeItems.sumOf { it.priceAtOrder * it.quantity } + cartItems.sumOf { it.menuItem.price * it.quantity }
        val discount = subtotal * currentDiscountRate
        val subtotalAfterDiscount = subtotal - discount
        val tax = subtotalAfterDiscount * currentTax.rate
        val gratuity = subtotalAfterDiscount * currentGratuityRate
        val total = subtotalAfterDiscount + tax + gratuity

        PosUiState(
            orderedItems = currentOrderedItems,
            cartItems = cartItems,
            subtotal = subtotal,
            discount = discount,
            discountRate = currentDiscountRate,
            tax = tax,
            taxRate = currentTax.rate,
            gratuity = gratuity,
            gratuityRate = currentGratuityRate,
            total = total,
            hasUnsavedChanges = unsavedChanges.isNotEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PosUiState()
    )

    fun addToCart(menuItem: MenuItem) {
        val currentCart = _cartItems.value.toMutableList()
        val existingItem = currentCart.find {
            it.menuItem.id == menuItem.id &&
                    it.menuItem.price == menuItem.price &&
                    it.selectedOptions == null &&
                    it.memo.isEmpty()
        }

        if (existingItem != null) {
            _cartItems.value = currentCart.map {
                if (it.menuItem.id == menuItem.id && it.menuItem.price == menuItem.price && it.selectedOptions == null && it.memo.isEmpty()) {
                    it.copy(quantity = it.quantity + 1)
                } else {
                    it
                }
            }
        } else {
            _cartItems.value = currentCart + CartItem(menuItem = menuItem, quantity = 1)
        }
    }

    fun addToCartWithOptions(menuItem: MenuItem, options: String, finalPrice: Double) {
        val currentCart = _cartItems.value.toMutableList()
        val pricedMenuItem = menuItem.copy(price = finalPrice)

        _cartItems.value = currentCart + CartItem(
            menuItem = pricedMenuItem,
            quantity = 1,
            selectedOptions = options
        )
    }

    fun updateCartItemMemo(cartItem: CartItem, newMemo: String) {
        _cartItems.value = _cartItems.value.map {
            if (it == cartItem) it.copy(memo = newMemo) else it
        }
    }

    fun updateOrderItemMemo(orderItem: OrderItem, newMemo: String) {
        viewModelScope.launch {
            orderDao.updateOrderItem(orderItem.copy(memo = newMemo))
        }
    }

    fun sendOrderToKitchen() {
        viewModelScope.launch {
            val cart = _cartItems.value
            if (cart.isNotEmpty()) {
                val newOrderItems = cart.map {
                    OrderItem(
                        orderId = 0,
                        menuItemId = it.menuItem.id,
                        quantity = it.quantity,
                        priceAtOrder = it.menuItem.price,
                        nameAtOrder = it.menuItem.name,
                        selectedOptions = it.selectedOptions,
                        memo = it.memo
                    )
                }

                val savedOrderId = orderDao.addItemsToOpenOrder(tableId, newOrderItems).toInt()

                val cartForPrint = cart
                _cartItems.value = emptyList()

                try {
                    printKitchenReceipts(cartForPrint, savedOrderId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                try {
                    withContext(Dispatchers.IO) {
                        val order = com.jun.simplepos.data.Order(
                            tableId = tableId,
                            status = "PENDING"
                        )
                        val createdOrder = RetrofitClient.api.createOrder(order).execute().body()

                        createdOrder?.id?.let { orderId ->
                            val serverItems = newOrderItems.map { item ->
                                ServerOrderItem(
                                    orderId = orderId,
                                    menuId = item.menuItemId ?: 0,
                                    menuName = item.nameAtOrder,
                                    price = item.priceAtOrder,
                                    quantity = item.quantity
                                )
                            }

                            RetrofitClient.api.addOrderItems(orderId, serverItems).execute()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun printKitchenReceipts(cartItems: List<CartItem>, orderId: Int) {
        val prefs = context.getSharedPreferences("business_profile", Context.MODE_PRIVATE)
        val businessName = prefs.getString("business_name", "Restaurant") ?: "Restaurant"
        val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US)
        val currentTime = dateFormat.format(Date())

        val kitchenItems = cartItems.filter {
            it.menuItem.station == "Kitchen" || it.menuItem.station == "Both"
        }
        val sushiItems = cartItems.filter {
            it.menuItem.station == "Sushi Bar" || it.menuItem.station == "Both"
        }

        val realTableName = orderDao.getTableName(tableId) ?: tableId.toString()

        if (kitchenItems.isNotEmpty()) {
            val kitchenReceipt = buildKitchenReceipt(
                businessName, currentTime, realTableName, orderId, kitchenItems, "KITCHEN"
            )
            PrinterManager.print(context, kitchenReceipt)
        }

        if (sushiItems.isNotEmpty()) {
            val sushiReceipt = buildKitchenReceipt(
                businessName, currentTime, realTableName, orderId, sushiItems, "SUSHI BAR"
            )
            PrinterManager.print(context, sushiReceipt)
        }
    }

    private fun buildKitchenReceipt(
        businessName: String,
        dateTime: String,
        tableNumber: String,
        orderId: Int,
        items: List<CartItem>,
        station: String
    ): String {
        return buildString {
            appendLine(ReceiptFormatter.center("*** $station ORDER ***", 48))
            appendLine("=".repeat(40))

            appendLine()
            appendLine(ReceiptFormatter.center("Table: $tableNumber", 48))
            appendLine()

            appendLine("Time: $dateTime")
            appendLine("Order: #$orderId")
            appendLine("=".repeat(40))
            appendLine()

            items.forEach { item ->
                appendLine("${item.quantity}x ${item.menuItem.name}")
                if (!item.selectedOptions.isNullOrBlank()) {
                    item.selectedOptions.lines().forEach { line ->
                        if (line.isNotBlank()) {
                            appendLine("   $line")
                        }
                    }
                }
                if (item.memo.isNotBlank()) {
                    appendLine("   [NOTE: ${item.memo}]")
                }
                appendLine()
            }

            appendLine("=".repeat(40))
        }
    }

    fun saveChanges() {
        viewModelScope.launch {
            val changesToSave = _unsavedChanges.value
            changesToSave.values.forEach { item ->
                if (item.quantity > 0) {
                    orderDao.updateOrderItem(item)
                } else {
                    orderDao.deleteOrderItem(item)
                }
            }
            _unsavedChanges.value = emptyMap()
        }
    }

    fun completePayment() {
        viewModelScope.launch {
            openOrderFlow.first()?.let { fullOrder ->
                val paidOrder = fullOrder.order.copy(
                    status = "PAID",
                    completedAt = System.currentTimeMillis()
                )
                orderDao.updateOrder(paidOrder)
            }
        }
    }

    private fun updateUnsavedChanges(orderItem: OrderItem) {
        _unsavedChanges.update { currentChanges ->
            currentChanges + (orderItem.id to orderItem)
        }
    }

    fun incrementOrderedItem(orderItem: OrderItem) {
        val menuItem = MenuItem(
            id = orderItem.menuItemId ?: 0,
            name = orderItem.nameAtOrder,
            price = orderItem.priceAtOrder,
            category = "",
            description = null,
            unit = null
        )
        addToCart(menuItem)
    }

    fun decrementOrderedItem(orderItem: OrderItem) {
        if (orderItem.quantity == 1) {
            cancelOrderItem(orderItem)
        } else {
            val updatedItem = orderItem.copy(quantity = orderItem.quantity - 1)
            updateUnsavedChanges(updatedItem)
        }
    }

    fun cancelOrderItem(orderItem: OrderItem) {
        val updatedItem = orderItem.copy(status = "CANCELED")
        updateUnsavedChanges(updatedItem)
    }

    fun incrementCartItem(cartItem: CartItem) {
        _cartItems.value = _cartItems.value.map {
            if (it.menuItem.id == cartItem.menuItem.id && it.selectedOptions == cartItem.selectedOptions && it.memo == cartItem.memo) {
                it.copy(quantity = it.quantity + 1)
            } else {
                it
            }
        }
    }

    fun decrementCartItem(cartItem: CartItem) {
        val currentCart = _cartItems.value
        val item = currentCart.find { it.menuItem.id == cartItem.menuItem.id && it.selectedOptions == cartItem.selectedOptions && it.memo == cartItem.memo }
        if (item != null) {
            if (item.quantity > 1) {
                _cartItems.value = currentCart.map {
                    if (it.menuItem.id == cartItem.menuItem.id && it.selectedOptions == cartItem.selectedOptions && it.memo == cartItem.memo) {
                        it.copy(quantity = it.quantity - 1)
                    } else {
                        it
                    }
                }
            } else {
                _cartItems.value = currentCart.filterNot {
                    it.menuItem.id == cartItem.menuItem.id && it.selectedOptions == cartItem.selectedOptions && it.memo == cartItem.memo
                }
            }
        }
    }

    fun printCustomerReceipt() {
        viewModelScope.launch {
            val fullOrder = openOrderFlow.first() ?: return@launch
            val currentState = uiState.value

            val prefs = context.getSharedPreferences("business_profile", Context.MODE_PRIVATE)
            val businessName = prefs.getString("business_name", "Restaurant") ?: "Restaurant"
            val businessAddress = prefs.getString("address", "") ?: ""

            val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US)
            val currentTime = dateFormat.format(Date())
            val realTableName = orderDao.getTableName(tableId) ?: tableId.toString()

            val receiptData = ReceiptData(
                orderId = fullOrder.order.id,
                restaurantName = businessName,
                restaurantAddress = businessAddress,
                tableNumber = realTableName,
                dateTime = currentTime,
                items = currentState.orderedItems.filter { it.status == "ORDERED" }.map {
                    ReceiptItem(
                        name = it.nameAtOrder,
                        quantity = it.quantity,
                        unitPrice = it.priceAtOrder,
                        totalPrice = it.priceAtOrder * it.quantity
                    )
                } + currentState.cartItems.map {
                    ReceiptItem(
                        name = it.menuItem.name,
                        quantity = it.quantity,
                        unitPrice = it.menuItem.price,
                        totalPrice = it.menuItem.price * it.quantity
                    )
                },
                subtotal = currentState.subtotal,
                discount = currentState.discount,
                discountRate = currentState.discountRate,
                tax = currentState.tax,
                taxRate = currentState.taxRate,
                gratuity = currentState.gratuity,
                gratuityRate = currentState.gratuityRate,
                total = currentState.total
            )

            val receiptText = ReceiptFormatter.formatCustomerReceipt(receiptData)
            PrinterManager.print(context, receiptText)
        }
    }
}

class PosViewModelFactory(
    private val orderDao: OrderDao,
    private val tableId: Int,
    private val taxState: StateFlow<TaxState>,
    private val gratuityRate: StateFlow<Double>,
    private val discountRate: StateFlow<Double>,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PosViewModel(orderDao, tableId, taxState, gratuityRate, discountRate, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}