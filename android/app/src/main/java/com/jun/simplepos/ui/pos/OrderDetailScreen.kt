package com.jun.simplepos.ui.pos

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jun.simplepos.PosApplication
import com.jun.simplepos.ui.receipt.PrinterManager
import com.jun.simplepos.ui.receipt.ReceiptData
import com.jun.simplepos.ui.receipt.ReceiptFormatter
import com.jun.simplepos.ui.receipt.ReceiptItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    app: PosApplication,
    orderId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: OrderDetailViewModel = viewModel(
        factory = OrderDetailViewModelFactory(app.database.orderDao(), orderId)
    )
    val fullOrder by viewModel.order.collectAsState()
    var isSummaryExpanded by remember { mutableStateOf(true) }
    var printStatus by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order #$orderId") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(onClick = {
                        fullOrder?.let { order ->
                            scope.launch {
                                printStatus = "Printing..."
                                val receiptData = createReceiptData(context, order.order.tableId, order)
                                val receiptText = ReceiptFormatter.formatCustomerReceipt(receiptData)
                                val result = PrinterManager.print(context, receiptText)
                                printStatus = if (result.isSuccess) {
                                    "Print successful!"
                                } else {
                                    "Print failed: ${result.exceptionOrNull()?.message}"
                                }
                            }
                        }
                    }) {
                        Text("Print Receipt")
                    }
                }
            )
        }
    ) { innerPadding ->
        fullOrder?.let { order ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                        val completedDate = order.order.completedAt?.let { Date(it) }

                        Text(
                            "Completed: ${completedDate?.let { dateFormat.format(it) } ?: "N/A"}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (printStatus.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(printStatus, style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Items:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(order.items.filter { it.status == "ORDERED" }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.quantity}x ${item.nameAtOrder}")
                            Text("$${String.format("%.2f", item.priceAtOrder * item.quantity)}")
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Divider()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(16.dp)
                        ) {
                            val total = order.items
                                .filter { it.status == "ORDERED" }
                                .sumOf { it.priceAtOrder * it.quantity }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text("$${String.format("%.2f", total)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

private fun createReceiptData(context: Context, tableId: Int, order: com.jun.simplepos.data.FullOrder): ReceiptData {
    val prefs = context.getSharedPreferences("business_profile", Context.MODE_PRIVATE)
    val businessName = prefs.getString("business_name", "Restaurant") ?: "Restaurant"
    val address = prefs.getString("address", "") ?: ""

    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val completedDate = order.order.completedAt?.let { Date(it) } ?: Date()

    val items = order.items.filter { it.status == "ORDERED" }.map {
        ReceiptItem(
            name = it.nameAtOrder,
            quantity = it.quantity,
            unitPrice = it.priceAtOrder,
            totalPrice = it.priceAtOrder * it.quantity
        )
    }

    val subtotal = items.sumOf { it.totalPrice }

    return ReceiptData(
        orderId = order.order.id,
        restaurantName = businessName,
        restaurantAddress = address,
        tableNumber = tableId.toString(),
        dateTime = dateFormat.format(completedDate),
        items = items,
        subtotal = subtotal,
        discount = 0.0,
        discountRate = 0.0,
        tax = 0.0,
        taxRate = 0.0,
        gratuity = 0.0,
        gratuityRate = 0.0,
        total = subtotal
    )
}