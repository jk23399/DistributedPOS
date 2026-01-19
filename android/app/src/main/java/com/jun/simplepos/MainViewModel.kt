package com.jun.simplepos

import androidx.lifecycle.ViewModel
import com.jun.simplepos.network.RetrofitClient
import com.jun.simplepos.network.ServerMenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel : ViewModel() {
    private val _menuItems = MutableStateFlow<List<ServerMenuItem>>(emptyList())
    val menuItems: StateFlow<List<ServerMenuItem>> = _menuItems

    init {
        fetchMenus()
    }

    fun fetchMenus() {
        RetrofitClient.api.getMenus().enqueue(object : Callback<List<ServerMenuItem>> {
            override fun onResponse(call: Call<List<ServerMenuItem>>, response: Response<List<ServerMenuItem>>) {
                if (response.isSuccessful) {
                    _menuItems.value = response.body() ?: emptyList()
                }
            }

            override fun onFailure(call: Call<List<ServerMenuItem>>, t: Throwable) {
            }
        })
    }
}