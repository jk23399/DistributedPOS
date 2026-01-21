package com.jun.simplepos.ui.floorplan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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

@Composable
fun FloorPlanScreen(app: PosApplication, onTableClick: (Int) -> Unit) {
    val viewModel: FloorPlanViewModel = viewModel(factory = FloorPlanViewModelFactory(app.repository, app.database.orderDao()))
    val tables by viewModel.tables.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        tables.forEach { table ->
            Table(
                tableInfo = table,
                onDragEnd = {
                    viewModel.updateTable(it)
                },
                onClick = { onTableClick(table.id) }
            )
        }

        FloatingActionButton(
            onClick = { viewModel.addTable("New Table") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Table")
        }
    }
}

@Composable
fun Table(
    tableInfo: TableInfo,
    onDragEnd: (TableInfo) -> Unit,
    onClick: () -> Unit
) {
    var offsetX by remember { mutableStateOf(tableInfo.offsetX) }
    var offsetY by remember { mutableStateOf(tableInfo.offsetY) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(tableInfo.id) { // Use table id as key to reset pointer input when table changes
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    },
                    onDragEnd = {
                        onDragEnd(tableInfo.copy(offsetX = offsetX, offsetY = offsetY))
                    }
                )
            }
    ) {
        Card(
            modifier = Modifier.clickable(onClick = onClick),
            shape = RoundedCornerShape(8.dp),
        ) {
            Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                Text(tableInfo.name)
            }
        }
    }
}
