package com.example.simplepos.data

data class TableInfo(
    val id: Int? = null,
    val name: String,
    val offsetX: Float,
    val offsetY: Float,
    val width: Float,
    val height: Float
)

data class MenuItem(
    val id: Int? = null,
    val name: String,
    val price: Double,
    val category: String? = null,
    val description: String? = null
)

data class Order(
    val id: Int? = null,
    val tableId: Int,
    val status: String = "PENDING",
    val totalPrice: Double = 0.0
)

data class OrderItem(
    val id: Int? = null,
    val orderId: Int? = null,
    val menuId: Int,
    val menuName: String,
    val price: Double,
    val quantity: Int
)