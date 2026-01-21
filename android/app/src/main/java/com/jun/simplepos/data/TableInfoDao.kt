package com.jun.simplepos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TableInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tableInfo: TableInfo): Long

    @Update
    suspend fun update(tableInfo: TableInfo)

    @Delete
    suspend fun delete(tableInfo: TableInfo)

    @Query("SELECT * FROM tables ORDER BY name ASC")
    fun getAllTables(): Flow<List<TableInfo>>

    @Query("DELETE FROM tables WHERE id = :tableId")
    suspend fun deleteTable(tableId: Int)
}