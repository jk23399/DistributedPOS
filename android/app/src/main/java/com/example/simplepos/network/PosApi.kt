package com.jun.simplepos.network

import com.jun.simplepos.data.MenuItem
import com.jun.simplepos.data.Order
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class ServerOrderItem(
    val orderId: Int,
    val menuId: Int,
    val menuName: String,
    val price: Double,
    val quantity: Int
)

interface PosApi {
    @GET("api/menus")
    fun getMenus(): Call<List<MenuItem>>

    @POST("/api/menus")
    fun saveMenu(@Body menu: MenuItem): Call<MenuItem>

    @DELETE("/api/menus/{id}")
    fun deleteMenu(@Path("id") id: Int): Call<Void>

    @POST("/api/orders")
    fun createOrder(@Body order: Order): Call<Order>

    @POST("/api/orders/{orderId}/items")
    fun addOrderItems(@Path("orderId") orderId: Int, @Body items: List<ServerOrderItem>): Call<List<ServerOrderItem>>
}

object RetrofitClient {
    private const val BASE_URL = "http://127.0.0.1:8080/"

    val api: PosApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PosApi::class.java)
    }
}