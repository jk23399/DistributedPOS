package com.jun.simplepos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Transaction
    @Query("SELECT * FROM orders WHERE tableId = :tableId AND status = 'OPEN' LIMIT 1")
    fun getOpenOrderForTable(tableId: Int): Flow<FullOrder?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItem>)

    @Update
    suspend fun updateOrder(order: Order)

    @Update
    suspend fun updateOrderItem(orderItem: OrderItem)

    @Delete
    suspend fun deleteOrderItem(orderItem: OrderItem)

    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderByIdSuspend(orderId: Int): Order?

    @Query("SELECT DISTINCT tableId FROM orders WHERE status = 'OPEN'")
    fun getOpenOrderTableIds(): Flow<List<Int>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderById(orderId: Int): Flow<FullOrder?>

    @Query("SELECT * FROM orders WHERE status = 'PAID' ORDER BY completedAt DESC")
    @Transaction
    fun getPaidOrders(): Flow<List<FullOrder>>

    @Query("SELECT * FROM orders WHERE status = 'PAID' AND completedAt >= :startTime AND completedAt <= :endTime ORDER BY completedAt DESC")
    @Transaction
    fun getPaidOrdersByDateRange(startTime: Long, endTime: Long): Flow<List<FullOrder>>

    @Query("DELETE FROM orders WHERE status = 'PAID' AND completedAt < :threeYearsAgo")
    suspend fun deleteOldOrders(threeYearsAgo: Long): Int

// data/OrderDao.kt

    @Transaction
    suspend fun addItemsToOpenOrder(tableId: Int, items: List<OrderItem>): Long { // [수정] Long 반환
        val openOrder = getOrderByIdSuspend(getOpenOrderIdForTable(tableId) ?: 0)

        val orderId = if (openOrder == null) {
            insertOrder(Order(tableId = tableId, status = "OPEN"))
        } else {
            openOrder.id.toLong()
        }

        items.forEach { newItem ->
            val existingItems = getSameMenuOrderItems(orderId.toInt(), newItem.menuItemId ?: 0)

            val exactMatch = existingItems.find { existing ->
                existing.status == "ORDERED" &&
                        existing.priceAtOrder == newItem.priceAtOrder &&
                        existing.selectedOptions == newItem.selectedOptions
            }

            if (exactMatch != null) {
                val updatedItem = exactMatch.copy(quantity = exactMatch.quantity + newItem.quantity)
                updateOrderItem(updatedItem)
            } else {
                insertOrderItems(listOf(newItem.copy(orderId = orderId.toInt())))
            }
        }
        return orderId
    }

    @Query("SELECT id FROM orders WHERE tableId = :tableId AND status = 'OPEN' LIMIT 1")
    suspend fun getOpenOrderIdForTable(tableId: Int): Int?

    @Query("SELECT * FROM order_items WHERE orderId = :orderId AND menuItemId = :menuItemId AND status != 'CANCELED' LIMIT 1")
    suspend fun getOrderItem(orderId: Int, menuItemId: Int): OrderItem?

    @Query("SELECT * FROM order_items WHERE orderId = :orderId AND menuItemId = :menuItemId")
    suspend fun getSameMenuOrderItems(orderId: Int, menuItemId: Int): List<OrderItem>

    @androidx.room.Transaction
    @Query("SELECT * FROM orders WHERE status = 'PAID' AND completedAt BETWEEN :startDate AND :endDate ORDER BY completedAt DESC")
    fun getCompletedOrdersInRange(startDate: Long, endDate: Long): kotlinx.coroutines.flow.Flow<List<FullOrder>>

    @Query("SELECT name FROM tables WHERE id = :tableId")
    suspend fun getTableName(tableId: Int): String?
}