package com.jun.simplepos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tables")
data class TableInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val width: Float = 100f, // default width
    val height: Float = 100f, // default height
    val version: Long = 0 // Optimistic locking version field.
)