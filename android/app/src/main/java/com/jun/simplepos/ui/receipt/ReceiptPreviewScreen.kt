package com.jun.simplepos.ui.receipt

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import android.content.Context
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptPreviewScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("business_profile", Context.MODE_PRIVATE)

    val sampleReceipt = ReceiptData(
        orderId = 123,
        restaurantName = prefs.getString("business_name", "Your Restaurant Name") ?: "Your Restaurant Name",
        restaurantAddress = prefs.getString("address", "123 Main St, City, State 12345") ?: "123 Main St, City, State 12345",
        tableNumber = "5",
        dateTime = "Dec 17, 2025 14:30",
        items = listOf(
            ReceiptItem("Burger", 2, 12.99, 25.98),
            ReceiptItem("Fries", 1, 4.99, 4.99),
            ReceiptItem("Coke", 2, 2.50, 5.00)
        ),
        subtotal = 35.97,
        discount = 3.60,
        discountRate = 0.10,
        tax = 2.91,
        taxRate = 0.09,
        gratuity = 4.86,
        gratuityRate = 0.15,
        total = 40.14
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipt Preview") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Customer Receipt") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Kitchen Receipt") }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = when (selectedTab) {
                        0 -> ReceiptFormatter.formatCustomerReceipt(sampleReceipt)
                        else -> ReceiptFormatter.formatKitchenReceipt(sampleReceipt)
                    },
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}