package com.example.posserver.entity

import jakarta.persistence.*

@Entity
@Table(name = "menu_items")
data class MenuItem(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    val name: String,
    val price: Double,
    val category: String,
    val description: String? = null,
    val unit: String? = null,
    val imageUrl: String? = null,
    val station: String = "Kitchen"
)