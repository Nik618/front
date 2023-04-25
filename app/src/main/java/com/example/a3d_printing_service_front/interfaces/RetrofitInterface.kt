package com.example.a3d_printing_service_front.interfaces

import com.example.a3d_printing_service_front.pojo.OrdersPojo
import okhttp3.RequestBody

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface RetrofitInterface {

    @POST("login")
    suspend fun login(@Body requestBody: RequestBody): Response<ResponseBody>

    @POST("sign")
    suspend fun sign(@Body requestBody: RequestBody): Response<ResponseBody>

    @POST("create/order")
    suspend fun createOrder(@Body requestBody: RequestBody): Response<ResponseBody>

    @GET("get/orders")
    fun getOrders(): Call<OrdersPojo>

}