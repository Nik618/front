package com.example.a3d_printing_service_front.interfaces

import com.example.a3d_printing_service_front.pojo.JwtResponsePojo
import com.example.a3d_printing_service_front.pojo.OrderPojo
import com.example.a3d_printing_service_front.pojo.OrdersPojo
import com.example.a3d_printing_service_front.pojo.TokenRequestPojo
import com.example.a3d_printing_service_front.pojo.yookassa.response.YooKassaResponse
import okhttp3.RequestBody

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface RetrofitInterface {

    @POST("login")
    suspend fun login(@Body requestBody: RequestBody): Response<ResponseBody>

    @POST("token")
    fun token(@Body tokenRequestPojo: TokenRequestPojo): Call<JwtResponsePojo>

    @POST("sign")
    suspend fun sign(@Body requestBody: RequestBody): Response<ResponseBody>

    @POST("create/order")
    suspend fun createOrder(@Body requestBody: RequestBody): Response<ResponseBody>

    @GET("get/orders")
    fun getOrders(@Query("user") user: String): Call<OrdersPojo>

    @POST("set/price")
    fun setPrice(@Body requestBody: RequestBody): Call<YooKassaResponse>

    @GET("get/file")
    fun getFile(@Query("id") id: Int): Call<OrderPojo>

    @GET("del/order")
    fun delOrder(@Query("id") id: Int): Call<OrderPojo>

}