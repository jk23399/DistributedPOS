/**
 * This ViewModel manages the UI state for the POS application.
 * It connects to the backend server to fetch menu items asynchronously
 * and exposes the data to the UI using StateFlow.
 */

package com.example.simplepos

import androidx.lifecycle.ViewModel
import com.jun.simplepos.data.MenuItem
import com.jun.simplepos.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel : ViewModel() {
    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems

    init {
        fetchMenus()
    }

    fun fetchMenus() {
        RetrofitClient.api.getMenus().enqueue(object : Callback<List<MenuItem>> {
            override fun onResponse(call: Call<List<MenuItem>>, response: Response<List<MenuItem>>) {
                if (response.isSuccessful) {
                    _menuItems.value = response.body() ?: emptyList()
                }
            }

            override fun onFailure(call: Call<List<MenuItem>>, t: Throwable) {
            }
        })
    }
}