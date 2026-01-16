package com.jun.simplepos.ui.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jun.simplepos.data.FullOrder
import com.jun.simplepos.data.OrderDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class OrderDetailViewModel(
    private val orderDao: OrderDao,
    private val orderId: Int
) : ViewModel() {

    val order: StateFlow<FullOrder?> = orderDao.getOrderById(orderId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}

class OrderDetailViewModelFactory(
    private val orderDao: OrderDao,
    private val orderId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderDetailViewModel(orderDao, orderId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}