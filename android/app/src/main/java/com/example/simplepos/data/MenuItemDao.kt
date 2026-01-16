package com.jun.simplepos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MenuItem)

    @Update
    suspend fun update(item: MenuItem)

    @Delete
    suspend fun delete(item: MenuItem)

    @Query("SELECT * FROM menu_items ORDER BY name ASC")
    fun getAllMenuItems(): Flow<List<MenuItem>>

    @Query("SELECT * FROM menu_items WHERE id = :id")
    fun getMenuItemById(id: Int): Flow<MenuItem>

    @Query("SELECT DISTINCT category FROM menu_items ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>
}