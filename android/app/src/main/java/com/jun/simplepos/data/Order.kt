package com.jun.simplepos.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tableId: Int,
    var status: String = "OPEN",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

data class FullOrder(
    @Embedded val order: Order,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val items: List<OrderItem>
)