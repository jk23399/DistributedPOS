package com.jun.simplepos.ui.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jun.simplepos.PosApplication
import com.jun.simplepos.data.MenuItem
import com.jun.simplepos.data.ModifierGroup
import com.jun.simplepos.data.ModifierOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemsScreen(
    app: PosApplication,
    onNavigateBack: () -> Unit
) {
    val viewModel: MenuViewModel = viewModel(factory = MenuViewModelFactory(app.repository))

    var showAddMenuDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<MenuItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Items") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        MenuSection(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding),
            onAddItemClick = { showAddMenuDialog = true }
        ) { itemToEdit = it }
    }

    if (showAddMenuDialog) {
        MenuItemDialog(
            viewModel = viewModel,
            onDismiss = { showAddMenuDialog = false },
            onConfirm = {
                viewModel.insert(it)
                showAddMenuDialog = false
            }
        )
    }

    itemToEdit?.let {
        MenuItemDialog(
            item = it,
            viewModel = viewModel,
            onDismiss = { itemToEdit = null },
            onConfirm = { menuItem ->
                viewModel.update(menuItem)
                itemToEdit = null
            },
            onDelete = { menuItem ->
                viewModel.delete(menuItem)
                itemToEdit = null
            }
        )
    }
}

@Composable
fun MenuSection(
    viewModel: MenuViewModel,
    modifier: Modifier = Modifier,
    onAddItemClick: () -> Unit,
    onEditItem: (MenuItem) -> Unit
) {
    val menuItems by viewModel.menuItems.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("All Menu Items", style = MaterialTheme.typography.titleLarge)
            Button(onClick = onAddItemClick) { Text("Add Item") }
        }
        menuItems.forEach { item ->
            ListItem(
                headlineContent = { Text(item.name) },
                supportingContent = { Text("${item.category} | ${item.description ?: ""}") },
                trailingContent = {
                    val unitText = if (item.unit?.isNotBlank() == true) "/ ${item.unit}" else ""
                    Text("$${item.price}$unitText")
                },
                modifier = Modifier.clickable { onEditItem(item) }
            )
            Divider()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuItemDialog(
    item: MenuItem? = null,
    viewModel: MenuViewModel,
    onDismiss: () -> Unit,
    onConfirm: (MenuItem) -> Unit,
    onDelete: ((MenuItem) -> Unit)? = null
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var price by remember { mutableStateOf(item?.price?.toString() ?: "") }
    var description by remember { mutableStateOf(item?.description ?: "") }
    var unit by remember { mutableStateOf(item?.unit ?: "") }

    // Convert current list to string for display: "Group:Op1,Op2=1.0;Group2:..."
    val initialOptionsString = item?.modifierGroups?.joinToString(";") { group ->
        "${group.title}:${group.options.joinToString(",") { opt ->
            if (opt.price > 0) "${opt.name}=${opt.price}" else opt.name
        }}"
    } ?: ""

    var optionsText by remember { mutableStateOf(initialOptionsString) }

    var categoryText by remember { mutableStateOf(item?.category ?: "") }
    val categories by viewModel.categories.collectAsState()
    var categoryExpanded by remember { mutableStateOf(false) }

    var stationText by remember { mutableStateOf(item?.station ?: "Kitchen") }
    val stations = listOf("Kitchen", "Sushi Bar", "Both")
    var stationExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        title = { Text(if (item == null) "Add New Menu Item" else "Edit Menu Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                TextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                    TextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        value = categoryText,
                        onValueChange = { categoryText = it },
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    )
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categories.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    categoryText = selectionOption
                                    categoryExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(expanded = stationExpanded, onExpandedChange = { stationExpanded = !stationExpanded }) {
                    TextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        value = stationText,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Station") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stationExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    )
                    ExposedDropdownMenu(expanded = stationExpanded, onDismissRequest = { stationExpanded = false }) {
                        stations.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    stationText = selectionOption
                                    stationExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }

                TextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                TextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit (e.g., each, 2pcs)") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = optionsText,
                    onValueChange = { optionsText = it },
                    label = { Text("Options (Format: Group:Opt1,Opt2=1.0;Group2:Opt3)") },
                    placeholder = { Text("Ex: Entree:Chicken,Beef=2.0;Side:Soup,Salad") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val priceDouble = price.toDoubleOrNull() ?: 0.0
                val finalUnit = if (unit.isBlank()) "1" else unit

                val parsedGroups = if (optionsText.isBlank()) emptyList() else {
                    optionsText.split(";").mapNotNull { groupStr ->
                        val parts = groupStr.split(":")
                        if (parts.size >= 2) {
                            val titlePart = parts[0].trim()
                            val maxSelection = if (titlePart.contains("[") && titlePart.contains("]")) {
                                titlePart.substringAfter("[").substringBefore("]").toIntOrNull() ?: 1
                            } else {
                                1
                            }
                            val title = titlePart.substringBefore("[")

                            val opts = parts[1].split(",").map { optStr ->
                                val optParts = optStr.split("=")
                                val optName = optParts[0].trim()
                                val optPrice = optParts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                                ModifierOption(optName, optPrice)
                            }

                            ModifierGroup(title, true, maxSelection, opts)
                        } else null
                    }
                }

                val finalItem = if (item == null) {
                    MenuItem(
                        name = name,
                        price = priceDouble,
                        category = categoryText,
                        description = description,
                        unit = finalUnit,
                        station = stationText,
                        modifierGroups = parsedGroups
                    )
                } else {
                    item.copy(
                        name = name,
                        price = priceDouble,
                        category = categoryText,
                        description = description,
                        unit = finalUnit,
                        station = stationText,
                        modifierGroups = parsedGroups
                    )
                }
                onConfirm(finalItem)
            }) {
                Text(if (item == null) "Add" else "Save")
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null && item != null) {
                    TextButton(onClick = {
                        onDelete(item)
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}