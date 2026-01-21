package com.jun.simplepos.data

import com.jun.simplepos.network.RetrofitClient
import com.jun.simplepos.network.ServerTableInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MenuItemRepository(private val menuItemDao: MenuItemDao, private val tableInfoDao: TableInfoDao) {

    fun getAllMenuItems(): Flow<List<MenuItem>> = menuItemDao.getAllMenuItems()

    fun getAllCategories(): Flow<List<String>> = menuItemDao.getAllCategories()

    suspend fun insert(item: MenuItem) {
        menuItemDao.insert(item)
    }

    suspend fun update(item: MenuItem) {
        menuItemDao.update(item)
    }

    suspend fun delete(item: MenuItem) {
        menuItemDao.delete(item)
    }

    fun getAllTables(): Flow<List<TableInfo>> = tableInfoDao.getAllTables()

    suspend fun insert(tableInfo: TableInfo) {
        val generatedId = tableInfoDao.insert(tableInfo)
        try {
            withContext(Dispatchers.IO) {
                val serverTable = ServerTableInfo(
                    id = null,
                    name = tableInfo.name,
                    offsetX = tableInfo.offsetX,
                    offsetY = tableInfo.offsetY,
                    width = tableInfo.width,
                    height = tableInfo.height,
                    version = 0
                )
                val response = RetrofitClient.api.saveTable(serverTable).execute()
                response.body()?.let { serverResponse ->
                    val updatedLocal = tableInfo.copy(
                        id = generatedId.toInt(),
                        version = serverResponse.version
                    )
                    tableInfoDao.update(updatedLocal)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun update(tableInfo: TableInfo) {
        tableInfoDao.update(tableInfo)
        try {
            withContext(Dispatchers.IO) {
                val serverTable = ServerTableInfo(
                    name = tableInfo.name,
                    offsetX = tableInfo.offsetX,
                    offsetY = tableInfo.offsetY,
                    width = tableInfo.width,
                    height = tableInfo.height,
                    version = tableInfo.version
                )
                tableInfo.id.let { id ->
                    val response = RetrofitClient.api.updateTable(id, serverTable).execute()
                    if (response.code() == 409) {
                        val latest = RetrofitClient.api.getTable(id).execute().body()
                        latest?.let {
                            val updatedLocal = tableInfo.copy(
                                name = it.name,
                                offsetX = it.offsetX,
                                offsetY = it.offsetY,
                                width = it.width,
                                height = it.height,
                                version = it.version
                            )
                            tableInfoDao.update(updatedLocal)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun delete(tableInfo: TableInfo) {
        tableInfoDao.delete(tableInfo)
        try {
            withContext(Dispatchers.IO) {
                tableInfo.id?.let { RetrofitClient.api.deleteTable(it).execute() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}