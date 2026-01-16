package com.example.posserver.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
data class Order(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    val tableId: Int,
    val orderTime: LocalDateTime = LocalDateTime.now(),
    var status: String = "PENDING",
    var totalPrice: Double = 0.0
)