/**
 * The main entry point for the POS UI.
 * It initializes MainViewModel to observe real-time menu data from the server
 * and renders the list of items on the screen.
 */
package com.jun.simplepos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PosApp(viewModel: MainViewModel = viewModel()) {
    val menuItems by viewModel.menuItems.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Server Connection Test")
        Text(text = "Total Items: ${menuItems.size}")

        LazyColumn {
            items(menuItems) { menu ->
                Text(text = "${menu.name} : ${menu.price}")
            }
        }
    }
}