package com.jun.simplepos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jun.simplepos.ui.floorplan.FloorPlanEditorScreen
import com.jun.simplepos.ui.menu.MenuItemsScreen
import com.jun.simplepos.ui.menu.SettingsMenuScreen
import com.jun.simplepos.ui.menu.GlobalSettingsScreen
import com.jun.simplepos.ui.pos.PosScreen
import com.jun.simplepos.ui.tables.TableSelectionScreen
import com.jun.simplepos.ui.theme.SimplePOSTheme
import com.jun.simplepos.ui.pos.OrderHistoryScreen
import com.jun.simplepos.ui.pos.OrderDetailScreen
import com.jun.simplepos.ui.receipt.ReceiptPreviewScreen
import com.jun.simplepos.ui.menu.BusinessProfileScreen
import com.jun.simplepos.ui.menu.PrinterSettingsScreen
import com.jun.simplepos.ui.sales.SalesScreen
import android.util.Log
import com.example.simplepos.data.MenuItem
import com.example.simplepos.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.api.getMenus().enqueue(object : Callback<List<MenuItem>> {
            override fun onResponse(call: Call<List<MenuItem>>, response: Response<List<MenuItem>>) {
                if (response.isSuccessful) {
                    Log.d("POS_TEST", "Connection Success. Menu count: ${response.body()?.size}")
                } else {
                    Log.d("POS_TEST", "Response Failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<MenuItem>>, t: Throwable) {
                Log.d("POS_TEST", "Communication Error: ${t.message}")
            }
        })

        setContent {
            SimplePOSTheme(darkTheme = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PosApp()
                }
            }
        }
    }
}

@Composable
fun PosApp() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as PosApplication

    NavHost(navController = navController, startDestination = "tables") {
        composable("tables") {
            TableSelectionScreen(
                app = application,
                onTableSelected = { tableId -> navController.navigate("pos/$tableId") },
                onNavigateToSettings = { navController.navigate("settings_menu") }
            )
        }
        composable(
            route = "pos/{tableId}",
            arguments = listOf(navArgument("tableId") { type = NavType.IntType })
        ) { backStackEntry ->
            val tableId = backStackEntry.arguments?.getInt("tableId") ?: 0
            PosScreen(
                onNavigateToMenu = { navController.navigate("settings_menu") },
                onNavigateBack = { navController.navigateUp() },
                app = application,
                tableId = tableId
            )
        }

        composable("settings_menu") {
            SettingsMenuScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToGlobalSettings = { navController.navigate("global_settings") },
                onNavigateToBusinessProfile = { navController.navigate("business_profile") },
                onNavigateToPrinterSettings = { navController.navigate("printer_settings") },
                onNavigateToMenuItems = { navController.navigate("menu_items") },
                onNavigateToOrderHistory = { navController.navigate("order_history") },
                onNavigateToReceiptPreview = { navController.navigate("receipt_preview") },
                onNavigateToFloorPlanEditor = { navController.navigate("floor_plan_editor") },
                onNavigateToSalesReport = { navController.navigate("sales_report") }
            )
        }

        composable("order_history") {
            OrderHistoryScreen(
                app = application,
                onNavigateBack = { navController.navigateUp() },
                onOrderClick = { orderId ->
                    navController.navigate("order_detail/$orderId")
                }
            )
        }

        composable("sales_report") {
            SalesScreen(
                app = application,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(
            route = "order_detail/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
            OrderDetailScreen(
                app = application,
                orderId = orderId,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable("global_settings") {
            GlobalSettingsScreen(
                app = application,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable("menu_items") {
            MenuItemsScreen(
                app = application,
                onNavigateBack = { navController.navigateUp() }
            )
        }
        composable("floor_plan_editor") {
            FloorPlanEditorScreen(
                app = application,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable("receipt_preview") {
            ReceiptPreviewScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable("business_profile") {
            BusinessProfileScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable("printer_settings") {
            PrinterSettingsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}