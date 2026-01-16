package com.jun.simplepos.ui.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jun.simplepos.PosApplication
import com.jun.simplepos.data.MenuItem
import com.jun.simplepos.data.OrderItem
import com.jun.simplepos.ui.menu.MenuViewModel
import com.jun.simplepos.ui.menu.MenuViewModelFactory
import com.jun.simplepos.ui.theme.PayButtonGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    onNavigateToMenu: () -> Unit,
    onNavigateBack: () -> Unit,
    app: PosApplication,
    tableId: Int
) {
    val context = LocalContext.current
    val menuViewModel: MenuViewModel = viewModel(factory = MenuViewModelFactory(app.repository))
    val posViewModel: PosViewModel = viewModel(
        factory = PosViewModelFactory(
            app.database.orderDao(),
            tableId,
            app.selectedTaxState,
            app.selectedGratuityRate,
            app.selectedDiscountRate,
            context
        )
    )

    val lastGratuityRate = rememberSaveable {
        mutableStateOf(app.selectedGratuityRate.value.takeIf { it > 0 } ?: 0.15)
    }

    val lastDiscountRate = rememberSaveable {
        mutableStateOf(app.selectedDiscountRate.value.takeIf { it > 0 } ?: 0.10)
    }

    var showPaymentDialog by remember { mutableStateOf(false) }
    var showOptionDialog by remember { mutableStateOf(false) }
    var showPriceDialog by remember { mutableStateOf(false) }

    var selectedMenuItem by remember { mutableStateOf<MenuItem?>(null) }
    var tempCustomItem by remember { mutableStateOf<MenuItem?>(null) }

    if (showPaymentDialog) {
        PaymentDialog(
            onDismiss = { showPaymentDialog = false },
            onPrintReceipt = {
                posViewModel.printCustomerReceipt()
                showPaymentDialog = false
            },
            onCompletePayment = {
                posViewModel.completePayment()
                showPaymentDialog = false
                onNavigateBack()
            }
        )
    }

    if (showOptionDialog && selectedMenuItem != null) {
        MenuOptionDialog(
            menuItem = selectedMenuItem!!,
            onDismiss = { showOptionDialog = false },
            onConfirm = { options, finalPrice ->
                posViewModel.addToCartWithOptions(selectedMenuItem!!, options, finalPrice)
                showOptionDialog = false
            }
        )
    }

    if (showPriceDialog && tempCustomItem != null) {
        PriceInputDialog(
            menuItem = tempCustomItem!!,
            onDismiss = { showPriceDialog = false },
            onConfirm = { price ->
                val customPricedItem = tempCustomItem!!.copy(price = price)
                posViewModel.addToCart(customPricedItem)
                showPriceDialog = false
            }
        )
    }

    Row(modifier = Modifier.fillMaxSize()) {
        OrderPane(
            modifier = Modifier.weight(0.4f),
            posViewModel = posViewModel,
            onGratuityToggle = { isChecked ->
                if (isChecked) {
                    app.selectedGratuityRate.value = lastGratuityRate.value
                } else {
                    val currentRate = app.selectedGratuityRate.value
                    if (currentRate > 0) {
                        lastGratuityRate.value = currentRate
                    }
                    app.selectedGratuityRate.value = 0.0
                }
            },
            onDiscountToggle = { isChecked ->
                if (isChecked) {
                    app.selectedDiscountRate.value = lastDiscountRate.value
                } else {
                    val currentRate = app.selectedDiscountRate.value
                    if (currentRate > 0) {
                        lastDiscountRate.value = currentRate
                    }
                    app.selectedDiscountRate.value = 0.0
                }
            },
            onSendToKitchen = {
                posViewModel.sendOrderToKitchen()
            },
            onPay = { showPaymentDialog = true },
            onSaveChanges = { posViewModel.saveChanges() }
        )

        Scaffold(
            modifier = Modifier.weight(0.6f),
            topBar = {
                TopAppBar(
                    title = { Text("Menu") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToMenu) {
                            Icon(imageVector = Icons.Filled.Settings, contentDescription = "Menu Management")
                        }
                    }
                )
            }
        ) { innerPadding ->
            MenuSelection(
                modifier = Modifier.padding(innerPadding),
                menuViewModel = menuViewModel,
                onMenuItemClick = { menuItem ->
                    if (menuItem.price == 0.0) {
                        tempCustomItem = menuItem
                        showPriceDialog = true
                    } else if (menuItem.modifierGroups.isNotEmpty()) {
                        selectedMenuItem = menuItem
                        showOptionDialog = true
                    } else {
                        posViewModel.addToCart(menuItem)
                    }
                }
            )
        }
    }
}

@Composable
fun OrderPane(
    modifier: Modifier = Modifier,
    posViewModel: PosViewModel,
    onGratuityToggle: (Boolean) -> Unit,
    onDiscountToggle: (Boolean) -> Unit,
    onSendToKitchen: () -> Unit,
    onPay: () -> Unit,
    onSaveChanges: () -> Unit
) {
    val uiState by posViewModel.uiState.collectAsState()
    var isSummaryExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Text("Order Details", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Order Items", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(uiState.orderedItems, key = { it.id }) { orderedItem ->
                OrderedItemRow(
                    orderedItem = orderedItem,
                    viewModel = posViewModel,
                    onCancel = { posViewModel.cancelOrderItem(orderedItem) },
                    isNew = false
                )
                Divider()
            }

            items(uiState.cartItems) { cartItem ->
                var showMemoDialog by remember { mutableStateOf(false) }

                if (showMemoDialog) {
                    MemoDialog(
                        initialMemo = cartItem.memo,
                        onDismiss = { showMemoDialog = false },
                        onConfirm = { newMemo ->
                            posViewModel.updateCartItemMemo(cartItem, newMemo)
                            showMemoDialog = false
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PayButtonGreen.copy(alpha = 0.1f))
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            cartItem.menuItem.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (cartItem.selectedOptions != null) {
                            Text(
                                cartItem.selectedOptions,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (cartItem.memo.isNotEmpty()) {
                            Text(
                                "Note: ${cartItem.memo}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            "$%.2f".format(cartItem.menuItem.price),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { showMemoDialog = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit Memo", tint = Color.Blue)
                        }

                        IconButton(onClick = { posViewModel.decrementCartItem(cartItem) }) {
                            Icon(Icons.Filled.RemoveCircle, contentDescription = "Decrement")
                        }
                        Text(
                            cartItem.quantity.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { posViewModel.incrementCartItem(cartItem) }) {
                            Icon(Icons.Filled.AddCircle, contentDescription = "Increment")
                        }
                    }
                }
                Divider()
            }
        }

        Divider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isSummaryExpanded = !isSummaryExpanded }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isSummaryExpanded) "Hide Summary" else "Show Total",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = if (isSummaryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Toggle Summary"
            )
        }

        if (isSummaryExpanded) {
            AmountRow("Subtotal", uiState.subtotal, style = MaterialTheme.typography.bodyMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val discountLabel = if (uiState.discountRate > 0) {
                        "Discount (${String.format("%.1f", uiState.discountRate * 100)}%)"
                    } else {
                        "Discount"
                    }
                    Text(discountLabel, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(4.dp))
                    Switch(
                        checked = uiState.discountRate > 0,
                        onCheckedChange = onDiscountToggle,
                        modifier = Modifier.scale(0.8f)
                    )
                }
                Text(
                    text = "-$%.2f".format(uiState.discount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            AmountRow(
                "Tax (${String.format("%.2f", uiState.taxRate * 100)}%)",
                uiState.tax,
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val gratuityLabel = if (uiState.gratuityRate > 0) {
                        "Gratuity (${String.format("%.1f", uiState.gratuityRate * 100)}%)"
                    } else {
                        "Gratuity"
                    }
                    Text(gratuityLabel, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(4.dp))
                    Switch(
                        checked = uiState.gratuityRate > 0,
                        onCheckedChange = onGratuityToggle,
                        modifier = Modifier.scale(0.8f)
                    )
                }
                Text(
                    text = "$%.2f".format(uiState.gratuity),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Divider()
            AmountRow("Total", uiState.total, isBold = true, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSaveChanges,
                modifier = Modifier.weight(1f),
                enabled = uiState.hasUnsavedChanges
            ) {
                Text("Save")
            }
            Button(
                onClick = onSendToKitchen,
                modifier = Modifier.weight(1f),
                enabled = uiState.cartItems.isNotEmpty()
            ) {
                Text("Send")
            }
            Button(
                onClick = onPay,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = uiState.orderedItems.isNotEmpty() || uiState.cartItems.isNotEmpty()
            ) {
                Text("Pay")
            }
        }
    }
}

@Composable
fun PaymentDialog(
    onDismiss: () -> Unit,
    onPrintReceipt: () -> Unit,
    onCompletePayment: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Payment") },
        text = { Text("Choose an option.") },
        confirmButton = {
            TextButton(onClick = onCompletePayment) {
                Text("Complete Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onPrintReceipt) {
                Text("Print Receipt")
            }
        }
    )
}

@Composable
fun MenuOptionDialog(
    menuItem: MenuItem,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    val selectedOptions = remember { mutableStateMapOf<String, MutableSet<String>>() }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        menuItem.modifierGroups.forEach { group ->
            if (!selectedOptions.containsKey(group.title)) {
                selectedOptions[group.title] = mutableSetOf()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(menuItem.name) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                menuItem.modifierGroups.forEach { group ->
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(group.title, fontWeight = FontWeight.Bold)

                            val infoText = buildString {
                                if (group.isRequired) append(" *Required")
                                if (group.maxSelection > 1) append(" (Max ${group.maxSelection})")
                                else append(" (Select 1)")
                            }
                            Text(infoText, style = MaterialTheme.typography.bodySmall, color = if(group.isRequired) MaterialTheme.colorScheme.error else Color.Gray, modifier = Modifier.padding(start = 8.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(group.options) { option ->
                                    val currentSet = selectedOptions[group.title] ?: mutableSetOf()
                                    val isSelected = currentSet.contains(option.name)

                                    Button(
                                        onClick = {
                                            val newSet = currentSet.toMutableSet()

                                            if (isSelected) {
                                                newSet.remove(option.name)
                                            } else {
                                                if (group.maxSelection == 1) {
                                                    newSet.clear()
                                                    newSet.add(option.name)
                                                } else {
                                                    if (newSet.size < group.maxSelection) {
                                                        newSet.add(option.name)
                                                    }
                                                }
                                            }
                                            selectedOptions[group.title] = newSet
                                        },
                                        colors = if (isSelected) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                                    ) {
                                        Text("${option.name} (+$${option.price})")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var extraPrice = 0.0
                    selectedOptions.forEach { (groupTitle, selectedNames) ->
                        val group = menuItem.modifierGroups.find { it.title == groupTitle }
                        selectedNames.forEach { name ->
                            val option = group?.options?.find { it.name == name }
                            if (option != null) extraPrice += option.price
                        }
                    }
                    val finalPrice = menuItem.price + extraPrice

                    val finalOptionsString = selectedOptions.entries
                        .filter { it.value.isNotEmpty() }
                        .flatMap { (groupTitle, selectedNames) ->
                            selectedNames.map { name -> "$groupTitle: $name" }
                        }
                        .joinToString("\n")

                    onConfirm(finalOptionsString, finalPrice)
                },
                enabled = true
            ) {
                val currentExtra = selectedOptions.entries.sumOf { (groupTitle, selectedNames) ->
                    val group = menuItem.modifierGroups.find { it.title == groupTitle }
                    selectedNames.sumOf { name ->
                        group?.options?.find { it.name == name }?.price ?: 0.0
                    }
                }
                Text("Add ($${"%.2f".format(menuItem.price + currentExtra)})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun OrderedItemRow(
    orderedItem: OrderItem,
    viewModel: PosViewModel,
    onCancel: () -> Unit,
    isNew: Boolean = false
) {
    var showMemoDialog by remember { mutableStateOf(false) }

    if (showMemoDialog) {
        MemoDialog(
            initialMemo = orderedItem.memo,
            onDismiss = { showMemoDialog = false },
            onConfirm = { newMemo ->
                viewModel.updateOrderItemMemo(orderedItem, newMemo)
                showMemoDialog = false
            }
        )
    }

    val isCancelled = orderedItem.status == "CANCELED"
    val textColor = if (isCancelled) Color.Gray else MaterialTheme.colorScheme.onSurface
    val textDecoration = if (isCancelled) TextDecoration.LineThrough else TextDecoration.None
    val backgroundColor = when {
        isCancelled -> Color.Transparent
        isNew -> PayButtonGreen.copy(alpha = 0.1f)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = !isCancelled, onClick = onCancel)
        ) {
            Text(
                orderedItem.nameAtOrder,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                textDecoration = textDecoration,
                fontWeight = if (isNew) FontWeight.Bold else FontWeight.Normal
            )
            if (orderedItem.selectedOptions != null) {
                Text(
                    orderedItem.selectedOptions,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCancelled) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = textDecoration
                )
            }
            if (orderedItem.memo.isNotEmpty()) {
                Text(
                    "Note: ${orderedItem.memo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCancelled) Color.Gray else Color.Red,
                    textDecoration = textDecoration,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                "$%.2f".format(orderedItem.priceAtOrder),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCancelled) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = textDecoration
            )
        }

        if (isCancelled) {
            Text(
                "Canceled",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { showMemoDialog = true }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Memo", tint = Color.Blue)
                }

                IconButton(onClick = { viewModel.decrementOrderedItem(orderedItem) }) {
                    Icon(Icons.Filled.RemoveCircle, contentDescription = "Decrement")
                }
                Text(
                    orderedItem.quantity.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    fontWeight = if (isNew) FontWeight.Bold else FontWeight.Normal
                )
                IconButton(onClick = { viewModel.incrementOrderedItem(orderedItem) }) {
                    Icon(Icons.Filled.AddCircle, contentDescription = "Increment")
                }
            }
        }
    }
}

@Composable
fun AmountRow(label: String, amount: Double, isBold: Boolean = false, style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = style, fontWeight = if(isBold) FontWeight.Bold else FontWeight.Normal)
        Text("$%.2f".format(amount), style = style, fontWeight = if(isBold) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun MenuSelection(
    modifier: Modifier = Modifier,
    menuViewModel: MenuViewModel,
    onMenuItemClick: (MenuItem) -> Unit
) {
    val menuItems by menuViewModel.menuItems.collectAsState()
    val categories by menuViewModel.categories.collectAsState()
    var selectedCategory by rememberSaveable { mutableStateOf("All") }

    val filteredItems = if (selectedCategory == "All") {
        menuItems
    } else {
        menuItems.filter { it.category == selectedCategory }
    }

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        CategoryTabs(
            categories = listOf("All") + categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 128.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredItems) { item ->
                MenuItemCard(item = item, onClick = { onMenuItemClick(item) })
            }
        }
    }
}

@Composable
fun CategoryTabs(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) {
            Button(
                onClick = { onCategorySelected(it) },
                colors = if (it == selectedCategory) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                border = if (it != selectedCategory) ButtonDefaults.outlinedButtonBorder else null
            ) {
                Text(it)
            }
        }
    }
}

@Composable
fun MenuItemCard(item: MenuItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(120.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(item.name, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text("$${item.price}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun PriceInputDialog(
    menuItem: MenuItem,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var priceText by remember { mutableStateOf("") }
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Price for ${menuItem.name}") },
        text = {
            Column {
                androidx.compose.material3.OutlinedTextField(
                    value = priceText,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() || it == '.' }) {
                            priceText = newValue
                        }
                    },
                    label = { Text("Price ($)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull()
                    if (price != null && price >= 0) {
                        onConfirm(price)
                    }
                },
                enabled = priceText.isNotEmpty()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MemoDialog(
    initialMemo: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialMemo) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Memo") },
        text = {
            androidx.compose.material3.TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Special Request") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(text) }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}