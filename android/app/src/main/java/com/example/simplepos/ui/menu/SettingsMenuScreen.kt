package com.jun.simplepos.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMenuScreen(
    onNavigateBack: () -> Unit,
    onNavigateToGlobalSettings: () -> Unit,
    onNavigateToBusinessProfile: () -> Unit,
    onNavigateToPrinterSettings: () -> Unit,
    onNavigateToMenuItems: () -> Unit,
    onNavigateToOrderHistory: () -> Unit,
    onNavigateToReceiptPreview: () -> Unit,
    onNavigateToFloorPlanEditor: () -> Unit,
    onNavigateToSalesReport: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Tools") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsButton("Business Profile", onNavigateToBusinessProfile)
            SettingsButton("Printer Settings", onNavigateToPrinterSettings)
            SettingsButton("Global Settings (Tax/Gratuity)", onNavigateToGlobalSettings)
            SettingsButton("Menu Management", onNavigateToMenuItems)
            SettingsButton("Floor Plan Editor", onNavigateToFloorPlanEditor)
            SettingsButton("Receipt Preview", onNavigateToReceiptPreview)
            SettingsButton("Order History", onNavigateToOrderHistory)
            SettingsButton("Sales Report", onNavigateToSalesReport)
        }
    }
}

@Composable
fun SettingsButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text)
    }
}