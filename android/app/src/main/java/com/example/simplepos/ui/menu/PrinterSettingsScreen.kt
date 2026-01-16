package com.jun.simplepos.ui.menu

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jun.simplepos.ui.receipt.PrinterManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("printer_settings", Context.MODE_PRIVATE)
    val scope = rememberCoroutineScope()

    var ipAddress by remember { mutableStateOf(prefs.getString("ip_address", "") ?: "") }
    var port by remember { mutableStateOf(prefs.getString("port", "9100") ?: "9100") }
    var testStatus by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Printer Settings") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Network Printer Configuration")

            OutlinedTextField(
                value = ipAddress,
                onValueChange = { ipAddress = it },
                label = { Text("IP Address") },
                placeholder = { Text("192.168.1.100") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = port,
                onValueChange = { port = it },
                label = { Text("Port") },
                placeholder = { Text("9100") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    prefs.edit().apply {
                        putString("ip_address", ipAddress)
                        putString("port", port)
                        apply()
                    }
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = ipAddress.isNotBlank() && port.isNotBlank()
            ) {
                Text("Save")
            }

            OutlinedButton(
                onClick = {
                    scope.launch {
                        testStatus = "Printing..."
                        val testReceipt = """
                            *** TEST PRINT ***
                            
                            Restaurant Name
                            123 Main St
                            City, State 12345
                            
                            Date: ${SimpleDateFormat("MMM dd, yyyy HH:mm").format(Date())}
                            
                            If you can read this,
                            your printer is working!
                            
                            ===================
                        """.trimIndent()

                        val result = PrinterManager.print(context, testReceipt)
                        testStatus = if (result.isSuccess) {
                            "Print successful!"
                        } else {
                            "Print failed: ${result.exceptionOrNull()?.message}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = ipAddress.isNotBlank() && port.isNotBlank()
            ) {
                Text("Test Print")
            }

            if (testStatus.isNotEmpty()) {
                Text(testStatus)
            }
        }
    }
}