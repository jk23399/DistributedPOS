package com.jun.simplepos.network

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class ServerMenuItem(
    val id: Int? = null,
    val name: String,
    val price: Double,
    val category: String? = null,
    val description: String? = null,
    val unit: String? = null,
    val station: String = "Kitchen"
)

data class ServerOrder(
    val id: Int? = null,
    val tableId: Int,
    val status: String = "PENDING",
    val totalPrice: Double = 0.0
)

data class ServerOrderItem(
    val orderId: Int,
    val menuId: Int,
    val menuName: String,
    val price: Double,
    val quantity: Int
)

data class ServerTableInfo(
    val id: Int? = null,
    val name: String,
    val offsetX: Float,
    val offsetY: Float,
    val width: Float,
    val height: Float,
    val version: Long = 0
)

interface PosApi {
    @GET("api/menus")
    fun getMenus(): Call<List<ServerMenuItem>>

    @GET("/api/tables/{id}")
    fun getTable(@Path("id") id: Int): Call<ServerTableInfo>

    @POST("/api/menus")
    fun saveMenu(@Body menu: ServerMenuItem): Call<ServerMenuItem>

    @PUT("/api/tables/{id}")
    fun updateTable(@Path("id") id: Int, @Body table: ServerTableInfo): Call<ServerTableInfo>

    @DELETE("/api/menus/{id}")
    fun deleteMenu(@Path("id") id: Int): Call<Void>

    @POST("/api/orders")
    fun createOrder(@Body order: ServerOrder): Call<ServerOrder>

    @POST("/api/orders/{orderId}/items")
    fun addOrderItems(@Path("orderId") orderId: Int, @Body items: List<ServerOrderItem>): Call<List<ServerOrderItem>>

    @POST("/api/tables")
    fun saveTable(@Body table: ServerTableInfo): Call<ServerTableInfo>

    @DELETE("/api/tables/{id}")
    fun deleteTable(@Path("id") id: Int): Call<Void>

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