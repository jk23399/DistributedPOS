package com.example.posserver.entity

import jakarta.persistence.*

@Entity
@Table(name = "tables")
data class TableInfo(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    val name: String,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val width: Float = 100f,
    val height: Float = 100f
)