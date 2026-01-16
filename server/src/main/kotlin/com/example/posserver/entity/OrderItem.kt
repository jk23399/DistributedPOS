package com.example.posserver.entity

import jakarta.persistence.*

@Entity
@Table(name = "order_items")
data class OrderItem(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    val orderId: Int,
    val menuId: Int,
    val menuName: String,
    val price: Double,
    var quantity: Int = 1
)