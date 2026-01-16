package com.jun.simplepos.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jun.simplepos.data.MenuItem
import com.jun.simplepos.data.MenuItemRepository
import com.jun.simplepos.data.TableInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MenuViewModel(private val repository: MenuItemRepository) : ViewModel() {

    val menuItems: StateFlow<List<MenuItem>> = repository.getAllMenuItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories: StateFlow<List<String>> = repository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val tables: StateFlow<List<TableInfo>> = repository.getAllTables()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insert(item: MenuItem) = viewModelScope.launch {
        repository.insert(item)
    }

    fun update(item: MenuItem) = viewModelScope.launch {
        repository.update(item)
    }

    fun delete(item: MenuItem) = viewModelScope.launch {
        repository.delete(item)
    }

    fun insert(tableInfo: TableInfo) = viewModelScope.launch {
        repository.insert(tableInfo)
    }

    fun update(tableInfo: TableInfo) = viewModelScope.launch {
        repository.update(tableInfo)
    }

    fun delete(tableInfo: TableInfo) = viewModelScope.launch {
        repository.delete(tableInfo)
    }
}

class MenuViewModelFactory(private val repository: MenuItemRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MenuViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}