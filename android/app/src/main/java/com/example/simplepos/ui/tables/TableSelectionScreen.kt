package com.jun.simplepos.ui.tables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jun.simplepos.PosApplication
import com.jun.simplepos.ui.floorplan.FloorPlanViewModel
import com.jun.simplepos.ui.floorplan.FloorPlanViewModelFactory
import com.jun.simplepos.ui.theme.PayButtonGreen
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableSelectionScreen(
    app: PosApplication,
    onTableSelected: (Int) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val viewModel: FloorPlanViewModel = viewModel(factory = FloorPlanViewModelFactory(app.database.tableInfoDao(), app.database.orderDao()))
    val tables by viewModel.tables.collectAsState()
    val openOrderTableIds by viewModel.openOrderTableIds.collectAsState()

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Select Table") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            tables.forEach { table ->
                val isOccupied = openOrderTableIds.contains(table.id)
                val cardColors = if (isOccupied) {
                    CardDefaults.cardColors(containerColor = PayButtonGreen.copy(alpha = 0.5f))
                } else {
                    CardDefaults.cardColors()
                }

                Card(
                    modifier = Modifier
                        .offset { IntOffset(table.offsetX.roundToInt(), table.offsetY.roundToInt()) }
                        .size(table.width.dp, table.height.dp)
                        .clickable { onTableSelected(table.id) },
                    colors = cardColors
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = table.name)
                    }
                }
            }
        }
    }
}
