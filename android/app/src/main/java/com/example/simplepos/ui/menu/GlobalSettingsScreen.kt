package com.jun.simplepos.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jun.simplepos.PosApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSettingsScreen(
    app: PosApplication,
    onNavigateBack: () -> Unit
) {
    val taxStates by app.taxStates.collectAsState()
    val gratuityOptions by app.gratuityOptions.collectAsState()
    val discountOptions by app.discountOptions.collectAsState()
    val selectedTax by app.selectedTaxState.collectAsState()
    val selectedGratuity by app.selectedGratuityRate.collectAsState()
    val selectedDiscount by app.selectedDiscountRate.collectAsState()

    var taxMenuExpanded by remember { mutableStateOf(false) }
    var gratuityMenuExpanded by remember { mutableStateOf(false) }
    var discountMenuExpanded by remember { mutableStateOf(false) }

    var showCustomTaxDialog by remember { mutableStateOf(false) }
    var showCustomGratuityDialog by remember { mutableStateOf(false) }
    var showCustomDiscountDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Global Settings") },
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
            Text("Tax, Gratuity, and Discount Settings", style = MaterialTheme.typography.titleLarge)

            Row(verticalAlignment = Alignment.CenterVertically) {
                ExposedDropdownMenuBox(
                    expanded = taxMenuExpanded,
                    onExpandedChange = { taxMenuExpanded = !taxMenuExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "${selectedTax.name} (${String.format("%.2f", selectedTax.rate * 100)}%)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Default Tax Rate") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = taxMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = taxMenuExpanded,
                        onDismissRequest = { taxMenuExpanded = false }
                    ) {
                        taxStates.forEach { taxState ->
                            DropdownMenuItem(
                                text = { Text("${taxState.name} (${String.format("%.2f", taxState.rate * 100)}%)") },
                                onClick = {
                                    app.selectedTaxState.value = taxState
                                    taxMenuExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Custom...") },
                            onClick = {
                                showCustomTaxDialog = true
                                taxMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                ExposedDropdownMenuBox(
                    expanded = gratuityMenuExpanded,
                    onExpandedChange = { gratuityMenuExpanded = !gratuityMenuExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "${String.format("%.1f", selectedGratuity * 100)}%",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Default Gratuity") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gratuityMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = gratuityMenuExpanded,
                        onDismissRequest = { gratuityMenuExpanded = false }
                    ) {
                        gratuityOptions.forEach { rate ->
                            DropdownMenuItem(
                                text = { Text("${String.format("%.1f", rate * 100)}%") },
                                onClick = {
                                    app.selectedGratuityRate.value = rate
                                    gratuityMenuExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Custom...") },
                            onClick = {
                                showCustomGratuityDialog = true
                                gratuityMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                ExposedDropdownMenuBox(
                    expanded = discountMenuExpanded,
                    onExpandedChange = { discountMenuExpanded = !discountMenuExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "${String.format("%.1f", selectedDiscount * 100)}%",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Default Discount") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = discountMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = discountMenuExpanded,
                        onDismissRequest = { discountMenuExpanded = false }
                    ) {
                        discountOptions.forEach { rate ->
                            DropdownMenuItem(
                                text = { Text("${String.format("%.1f", rate * 100)}%") },
                                onClick = {
                                    app.selectedDiscountRate.value = rate
                                    discountMenuExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Custom...") },
                            onClick = {
                                showCustomDiscountDialog = true
                                discountMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCustomTaxDialog) {
        CustomTaxDialog(
            onDismiss = { showCustomTaxDialog = false },
            onConfirm = { name, rate ->
                app.addCustomTaxState(name, rate)
                showCustomTaxDialog = false
            }
        )
    }

    if (showCustomGratuityDialog) {
        CustomGratuityDialog(
            onDismiss = { showCustomGratuityDialog = false },
            onConfirm = { rate ->
                app.addCustomGratuity(rate)
                showCustomGratuityDialog = false
            }
        )
    }

    if (showCustomDiscountDialog) {
        CustomDiscountDialog(
            onDismiss = { showCustomDiscountDialog = false },
            onConfirm = { rate ->
                app.addCustomDiscount(rate)
                showCustomDiscountDialog = false
            }
        )
    }
}

@Composable
fun CustomTaxDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Tax Rate") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("State Name (e.g., AZ)") }
                )
                TextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Rate (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(name, rate.toDoubleOrNull() ?: 0.0)
            }) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun CustomGratuityDialog(onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var rate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Gratuity Rate") },
        text = {
            TextField(
                value = rate,
                onValueChange = { rate = it },
                label = { Text("Rate (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(rate.toDoubleOrNull() ?: 0.0)
            }) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun CustomDiscountDialog(onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var rate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Discount Rate") },
        text = {
            TextField(
                value = rate,
                onValueChange = { rate = it },
                label = { Text("Rate (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(rate.toDoubleOrNull() ?: 0.0)
            }) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}