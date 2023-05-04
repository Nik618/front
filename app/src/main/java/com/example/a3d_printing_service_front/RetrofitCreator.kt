package com.example.a3d_printing_service_front

import android.util.Log
import com.example.a3d_printing_service_front.configs.UnsafeOkHttpClient
import com.example.a3d_printing_service_front.interfaces.RetrofitInterface
import com.example.a3d_printing_service_front.pojo.JwtResponsePojo
import com.example.a3d_printing_service_front.pojo.OrderPojo
import com.example.a3d_printing_service_front.pojo.OrdersPojo
import com.example.a3d_printing_service_front.pojo.TokenRequestPojo
import com.example.a3d_printing_service_front.pojo.yookassa.response.YooKassaResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import okhttp3.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.await
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class RetrofitCreator {

    var response: Response<ResponseBody>? = null
    var response2: String? = null
    val gson = Gson()

    private fun createRetrofitInterface(): RetrofitInterface {
        val okHttpClient: OkHttpClient = UnsafeOkHttpClient().getUnsafeOkHttpClient()!!
        val retrofit = Retrofit.Builder()
            .baseUrl("https://192.168.1.76:443/") // .baseUrl("https://feivur.ru/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(RetrofitInterface::class.java)
    }

    private fun createRetrofitInterfaceWithAccessToken(): RetrofitInterface {
        var okHttpClient: OkHttpClient =
            UnsafeOkHttpClient().getUnsafeOkHttpClient()!!.newBuilder().addInterceptor(
                Interceptor() { chain: Interceptor.Chain ->
                    val request = chain.request()
                    val requestBuilder = request.newBuilder()
                        .addHeader("Authorization", "Bearer ${Storage.accessToken}")
                    chain.proceed(requestBuilder.build())
                }
            ).build()
        okHttpClient = okHttpClient.newBuilder().apply {
            readTimeout(900000, TimeUnit.SECONDS)
            connectTimeout(900000, TimeUnit.SECONDS)
            writeTimeout(900000, TimeUnit.SECONDS)
        }.build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://192.168.1.76:443/") // .baseUrl("https://feivur.ru/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(RetrofitInterface::class.java)
    }

    private suspend fun withContext(response: Response<ResponseBody>) {
        withContext(Dispatchers.Main) {
            if (response.isSuccessful) {
                Log.d("RESPONSE: ", response.body().toString())
            } else {
                Log.e("RETROFIT ERROR: ", response.code().toString())
                throw Exception("RETROFIT ERROR: ${response.code()}")
            }
        }
    }

    suspend fun createOrder(jsonObjectString: String): Response<ResponseBody> {
        return createRetrofitInterfaceWithAccessToken().createOrder(
            RequestBody.create(
                MediaType.get("application/json; charset=utf-8"), jsonObjectString
            )
        )
    }

    fun getOrders(user: String): OrdersPojo? {
        return createRetrofitInterfaceWithAccessToken()
            .getOrders(user)
            .execute()
            .body()
    }

    fun token(tokenRequestPojo: TokenRequestPojo): JwtResponsePojo? {
        return createRetrofitInterfaceWithAccessToken()
            .token(tokenRequestPojo)
            .execute()
            .body()
    }

    fun getFile(id: Int): OrderPojo? {
        return createRetrofitInterfaceWithAccessToken()
            .getFile(id)
            .execute()
            .body()
    }

    fun setPrice(order: String): YooKassaResponse? {
        return createRetrofitInterfaceWithAccessToken()
            .setPrice(RequestBody.create(
                MediaType.get("application/json; charset=utf-8"), order
            ))
            .execute()
            .body()
    }

    fun delOrder(id: Int): OrderPojo? {
        return createRetrofitInterfaceWithAccessToken()
            .delOrder(id)
            .execute()
            .body()
    }

}