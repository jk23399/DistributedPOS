package com.jun.simplepos.ui.floorplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jun.simplepos.data.MenuItemRepository
import com.jun.simplepos.data.OrderDao
import com.jun.simplepos.data.TableInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FloorPlanViewModel(
    private val repository: MenuItemRepository,
    private val orderDao: OrderDao
) : ViewModel() {

    val tables: StateFlow<List<TableInfo>> = repository.getAllTables()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val openOrderTableIds: StateFlow<Set<Int>> = orderDao.getOpenOrderTableIds()
        .map { it.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    fun addTable(name: String) {
        viewModelScope.launch {
            repository.insert(TableInfo(name = name))
        }
    }

    fun updateTable(tableInfo: TableInfo) {
        viewModelScope.launch {
            repository.update(tableInfo)
        }
    }

    fun deleteTable(tableId: Int) {
        viewModelScope.launch {
            tables.value.find { it.id == tableId }?.let {
                repository.delete(it)
            }
        }
    }
}

class FloorPlanViewModelFactory(
    private val repository: MenuItemRepository,
    private val orderDao: OrderDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FloorPlanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FloorPlanViewModel(repository, orderDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}