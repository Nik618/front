package com.example.a3d_printing_service_front

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.a3d_printing_service_front.configs.UnsafeOkHttpClient
import com.example.a3d_printing_service_front.interfaces.RetrofitInterface
import com.example.a3d_printing_service_front.pojo.*
import com.example.a3d_printing_service_front.pojo.yookassa.response.YooKassaResponse
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

class RetrofitSender {

    var response: Response<ResponseBody>? = null
    var response2: String? = null
    val gson = Gson()

    private fun createRetrofitInterface(): RetrofitInterface {
        val okHttpClient: OkHttpClient = UnsafeOkHttpClient().getUnsafeOkHttpClient()!!
        val retrofit = Retrofit.Builder()
            .baseUrl("https://feivur.ru/") // .baseUrl("https://192.168.1.76:443/")
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
            .baseUrl("https://feivur.ru/") // .baseUrl("https://192.168.1.76:443/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(RetrofitInterface::class.java)
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

    fun delOrder(id: Int): ResultPojo? {
        return createRetrofitInterfaceWithAccessToken()
            .delOrder(id)
            .execute()
            .body()
    }

    fun startVideo(startVideoPojo: StartVideoPojo): ResultPojo? {
        return createRetrofitInterfaceWithAccessToken()
            .startVideo(startVideoPojo)
            .execute()
            .body()
    }

    fun stopVideo(id: Int): ResultPojo? {
        return createRetrofitInterfaceWithAccessToken()
            .stopVideo(id)
            .execute()
            .body()
    }

    fun prepareToDelivery(orderPojo: OrderPojo): ResultPojo? {
        return createRetrofitInterfaceWithAccessToken()
            .prepareToDelivery(orderPojo)
            .execute()
            .body()
    }

    fun getVideo(id: Int): ResultPojo? {
        return createRetrofitInterfaceWithAccessToken()
            .getVideo(id)
            .execute()
            .body()
    }

    fun getOrder(id: Int): OrderPojo? {
        return createRetrofitInterfaceWithAccessToken()
            .getOrder(id)
            .execute()
            .body()
    }

    fun getPhoto(id: Int): OrderPojo? {
        return createRetrofitInterfaceWithAccessToken()
            .getPhoto(id)
            .execute()
            .body()
    }

    fun getStatus(id: Int): ResultPojo? {
        return createRetrofitInterfaceWithAccessToken()
            .getStatus(id)
            .execute()
            .body()
    }

    fun approveReceiving(orderPojo: OrderPojo): ResultPojo? {
        return createRetrofitInterfaceWithAccessToken()
            .approveReceiving(orderPojo)
            .execute()
            .body()
    }

    fun getTrack(id: Int): ResultPojo? {
        return createRetrofitInterfaceWithAccessToken()
            .getTrack(id)
            .execute()
            .body()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshTokens() {
        val splitString: List<String> = Storage.accessToken.split(".")
        val base64EncodedBody = splitString[1]
        val body = String(Base64.getUrlDecoder().decode(base64EncodedBody))
        if (gson.fromJson(body, JwtPojo::class.java).exp.toInt() < (Date().time / 1000).toInt())
            CoroutineScope(Dispatchers.IO).launch {
                val jwtResponsePojo = token(TokenRequestPojo(refreshToken = Storage.refreshToken))
                withContext(Dispatchers.Main) {
                    Storage.accessToken = jwtResponsePojo?.accessToken!!
                    println("Токен обновлён!")
                }
            }
    }


}