package com.jun.simplepos.ui.sales

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jun.simplepos.data.FullOrder
import com.jun.simplepos.data.OrderDao
import com.jun.simplepos.ui.receipt.PrinterManager
import com.jun.simplepos.ui.receipt.ReceiptFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class SalesSummary(
    val totalRevenue: Double = 0.0,
    val orderCount: Int = 0,
    val startTime: Long = 0L,
    val endTime: Long = 0L
)

enum class SalesPeriod { DAILY, WEEKLY, MONTHLY }

class SalesViewModel(
    private val orderDao: OrderDao,
    private val context: Context
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(SalesPeriod.DAILY)
    val selectedPeriod: StateFlow<SalesPeriod> = _selectedPeriod

    @OptIn(ExperimentalCoroutinesApi::class)
    val salesSummary: StateFlow<SalesSummary> = _selectedPeriod.flatMapLatest { period ->
        val (start, end) = getDateRange(period)
        orderDao.getCompletedOrdersInRange(start, end).map { orders ->
            calculateSummary(orders, start, end)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SalesSummary()
    )

    fun setPeriod(period: SalesPeriod) {
        _selectedPeriod.value = period
    }

    private fun calculateSummary(orders: List<FullOrder>, start: Long, end: Long): SalesSummary {
        var totalSubtotal = 0.0
        orders.forEach { fullOrder ->
            val itemsTotal = fullOrder.items.sumOf { it.priceAtOrder * it.quantity }
            totalSubtotal += itemsTotal
        }
        return SalesSummary(
            totalRevenue = totalSubtotal,
            orderCount = orders.size,
            startTime = start,
            endTime = end
        )
    }

    private fun getDateRange(period: SalesPeriod): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val end = System.currentTimeMillis()
        val start: Long

        when (period) {
            SalesPeriod.DAILY -> {
                start = calendar.timeInMillis
            }
            SalesPeriod.WEEKLY -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                start = calendar.timeInMillis
            }
            SalesPeriod.MONTHLY -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                start = calendar.timeInMillis
            }
        }
        return Pair(start, end)
    }

    fun printSalesReport() {
        viewModelScope.launch {
            val summary = salesSummary.value
            val prefs = context.getSharedPreferences("business_profile", Context.MODE_PRIVATE)
            val businessName = prefs.getString("business_name", "Restaurant") ?: "Restaurant"

            val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US)
            val rangeFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            val now = dateFormat.format(Date())

            val receiptText = buildString {
                appendLine(ReceiptFormatter.center("SALES REPORT", 48))
                appendLine(ReceiptFormatter.center("(${_selectedPeriod.value})", 48))
                appendLine("=".repeat(40))
                appendLine(ReceiptFormatter.center(businessName, 48))
                appendLine()
                appendLine("Printed: $now")
                appendLine("Period: ${rangeFormat.format(Date(summary.startTime))} - ${rangeFormat.format(Date(summary.endTime))}")
                appendLine("=".repeat(40))
                appendLine()

                appendLine("Orders Completed: ${summary.orderCount}")
                appendLine()

                val grossLine = "Gross Sales"
                val grossPrice = "$%.2f".format(summary.totalRevenue)
                appendLine(grossLine + grossPrice.padStart(40 - grossLine.length))

                appendLine("-".repeat(40))

                val totalLine = "TOTAL COLLECTED"
                val totalPrice = "$%.2f".format(summary.totalRevenue)
                appendLine(totalLine + totalPrice.padStart(40 - totalLine.length))

                appendLine()
                appendLine("=".repeat(40))
                appendLine(ReceiptFormatter.center("END OF REPORT", 48))
            }

            PrinterManager.print(context, receiptText)
        }
    }
}

class SalesViewModelFactory(
    private val orderDao: OrderDao,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SalesViewModel(orderDao, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}