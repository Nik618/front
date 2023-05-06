package com.example.a3d_printing_service_front.interfaces

import com.example.a3d_printing_service_front.pojo.*
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
    fun delOrder(@Query("id") id: Int): Call<ResultPojo>

    @POST("start/video")
    fun startVideo(@Body startVideoPojo: StartVideoPojo): Call<ResultPojo>

    @GET("stop/video")
    fun stopVideo(@Query("id") id: Int): Call<ResultPojo>

    @POST("prepare/delivery")
    fun prepareToDelivery(@Body orderPojo: OrderPojo): Call<ResultPojo>

    @GET("get/video")
    fun getVideo(@Query("id") id: Int): Call<ResultPojo>

    @GET("get/order")
    fun getOrder(@Query("id") id: Int): Call<OrderPojo>

    @GET("get/photo")
    fun getPhoto(@Query("id") id: Int): Call<OrderPojo>

    @GET("get/status")
    fun getStatus(@Query("id") id: Int): Call<ResultPojo>

    @POST("approve/receiving")
    fun approveReceiving(@Body orderPojo: OrderPojo): Call<ResultPojo>


}