package com.jun.simplepos.ui.floorplan

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jun.simplepos.PosApplication
import com.jun.simplepos.data.TableInfo
import kotlin.math.roundToInt
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorPlanEditorScreen(
    app: PosApplication,
    onNavigateBack: () -> Unit
) {
    val viewModel: FloorPlanViewModel = viewModel(factory = FloorPlanViewModelFactory(app.database.tableInfoDao(), app.database.orderDao()))
    val tablesFromDb by viewModel.tables.collectAsState()

    var internalTables by remember { mutableStateOf<Map<Int, TableInfo>>(emptyMap()) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTable by remember { mutableStateOf<TableInfo?>(null) }

    LaunchedEffect(tablesFromDb) {
        val newTables = tablesFromDb.filter { it.id !in internalTables.keys }
        internalTables = internalTables + newTables.associateBy { it.id }
    }

    if (showEditDialog && selectedTable != null) {
        EditTableDialog(
            table = selectedTable!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedName ->
                val updatedTable = selectedTable!!.copy(name = updatedName)
                internalTables = internalTables + (updatedTable.id to updatedTable)
                viewModel.updateTable(updatedTable)
                showEditDialog = false
            },
            onDelete = {
                viewModel.deleteTable(selectedTable!!.id)
                showEditDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Floor Plan Editor") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        internalTables.values.forEach { viewModel.updateTable(it) }
                        onNavigateBack()
                    }) {
                        Text("Save")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.addTable("New Table")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Table")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            internalTables.forEach { (tableId, table) ->
                key(tableId) {
                    DraggableAndResizableTable(
                        table = table,
                        onUpdate = { updatedTable ->
                            internalTables = internalTables + (updatedTable.id to updatedTable)
                        },
                        onLongPress = {
                            selectedTable = internalTables[table.id]
                            showEditDialog = true
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraggableAndResizableTable(
    table: TableInfo,
    onUpdate: (TableInfo) -> Unit,
    onLongPress: () -> Unit
) {
    val gridSize = 20f

    var offsetX by remember { mutableStateOf(table.offsetX) }
    var offsetY by remember { mutableStateOf(table.offsetY) }
    var width by remember { mutableStateOf(table.width) }
    var height by remember { mutableStateOf(table.height) }
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(width.dp, height.dp)
            .pointerInput(table.id) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    },
                    onDragEnd = {
                        offsetX = (offsetX / gridSize).roundToInt() * gridSize
                        offsetY = (offsetY / gridSize).roundToInt() * gridSize

                        val snappedTable = table.copy(
                            offsetX = offsetX,
                            offsetY = offsetY,
                            width = width,
                            height = height
                        )
                        onUpdate(snappedTable)
                        isDragging = false
                    }
                )
            }
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        if (!isDragging) {
                            onLongPress()
                        }
                    }
                )
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = table.name)
            }
        }
    }
}

@Composable
fun EditTableDialog(
    table: TableInfo,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit
) {
    var tableName by remember { mutableStateOf(table.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Table") },
        text = {
            Column {
                OutlinedTextField(
                    value = tableName,
                    onValueChange = { tableName = it },
                    label = { Text("Table Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete Table")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(tableName) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}