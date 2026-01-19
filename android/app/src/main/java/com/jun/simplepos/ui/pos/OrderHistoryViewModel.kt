package com.jun.simplepos.ui.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jun.simplepos.data.FullOrder
import com.jun.simplepos.data.OrderDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class OrderHistoryViewModel(
    private val orderDao: OrderDao
) : ViewModel() {

    private val _dateRange = MutableStateFlow<Pair<Long?, Long?>>(Pair(null, null))

    val paidOrders: StateFlow<List<FullOrder>> = _dateRange.flatMapLatest { (start, end) ->
        if (start != null && end != null) {
            orderDao.getPaidOrdersByDateRange(start, end)
        } else {
            orderDao.getPaidOrders()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setDateRange(startDate: Long?, endDate: Long?) {
        _dateRange.value = Pair(startDate, endDate)
    }

    fun clearDateFilter() {
        _dateRange.value = Pair(null, null)
    }
}

class OrderHistoryViewModelFactory(
    private val orderDao: OrderDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderHistoryViewModel(orderDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}