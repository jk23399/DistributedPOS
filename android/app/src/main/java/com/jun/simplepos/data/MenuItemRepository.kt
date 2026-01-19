package com.jun.simplepos.data

import kotlinx.coroutines.flow.Flow

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
        tableInfoDao.insert(tableInfo)
    }

    suspend fun update(tableInfo: TableInfo) {
        tableInfoDao.update(tableInfo)
    }

    suspend fun delete(tableInfo: TableInfo) {
        tableInfoDao.delete(tableInfo)
    }
}