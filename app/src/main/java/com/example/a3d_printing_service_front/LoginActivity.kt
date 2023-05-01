package com.example.a3d_printing_service_front

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.a3d_printing_service_front.configs.UnsafeOkHttpClient
import com.example.a3d_printing_service_front.interfaces.RetrofitInterface
import com.example.a3d_printing_service_front.pojo.JwtPojo
import com.example.a3d_printing_service_front.pojo.JwtResponsePojo
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import java.util.*
import javax.net.ssl.*


class LoginActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //val intent = Intent(Intent.ACTION_VIEW, Uri.parse("rtsp://feivur.ru:8554/test"))
        //startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    fun onClickLogin(view: View) {
        val okHttpClient: OkHttpClient = UnsafeOkHttpClient().getUnsafeOkHttpClient()!!
        val retrofit = Retrofit.Builder()
            .baseUrl("https://feivur.ru/")
            .client(okHttpClient)
            .build()
        val retrofitInterface = retrofit.create(RetrofitInterface::class.java)
        val jsonObject = JSONObject()
        val login: EditText = findViewById(R.id.editTextTextEmailAddress)
        val password: EditText = findViewById(R.id.editTextTextPassword)
        val info: TextView = findViewById(R.id.textView)
        jsonObject.put("login", login.text)
        jsonObject.put("password", password.text)

        val jsonObjectString = jsonObject.toString()

        // Create RequestBody ( We're not using any converter, like GsonConverter, MoshiConverter e.t.c, that's why we use RequestBody )
        val requestBody =
            RequestBody.create(MediaType.get("application/json; charset=utf-8"), jsonObjectString)


            CoroutineScope(Dispatchers.IO).launch {

                try {
                    val response = retrofitInterface.login(requestBody)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {

                            // Convert raw JSON to pretty JSON using GSON library
                            val gson = GsonBuilder().setPrettyPrinting().create()
                            val prettyJson = gson.toJson(
                                JsonParser().parse(
                                    response.body()
                                        ?.string()
                                )
                            )

                            Log.d("Pretty Printed JSON :", prettyJson)

                            val jwtResponsePojo =
                                gson.fromJson(prettyJson, JwtResponsePojo::class.java)

                            Storage.accessToken = jwtResponsePojo.accessToken!!
                            Storage.refreshToken = jwtResponsePojo.refreshToken!!
                            Storage.user = login.text.toString()

                            println("------------ Decode JWT ------------")
                            val splitString: List<String> = jwtResponsePojo.accessToken.split(".")
                            val base64EncodedBody = splitString[1]
                            val body = String(Base64.getUrlDecoder().decode(base64EncodedBody))
                            println("JWT Body : $body")
                            val jwtPojo = gson.fromJson(body, JwtPojo::class.java)

                            if (jwtPojo.roles[0] == "USER") {
                                println("USER")
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                            }

                            if (jwtPojo.roles[0] == "ADMIN") {
                                println("ADMIN")
                                val intent = Intent(this@LoginActivity, AdminActivity::class.java)
                                startActivity(intent)
                            }

                        } else {
                            Log.e("RETROFIT_ERROR", response.code().toString())
                            info.text = "missing pair login/password"
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        info.text = "technical work in progress - please wait"
                    }
                }
            }
    }

    @SuppressLint("SetTextI18n")
    fun onClickSignUp(view: View) {
        val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
        startActivity(intent)
    }

}