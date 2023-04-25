package com.example.a3d_printing_service_front

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.a3d_printing_service_front.configs.UnsafeOkHttpClient
import com.example.a3d_printing_service_front.interfaces.RetrofitInterface
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

class SignUpActivity : AppCompatActivity() {

    var buttonSignUp: Button? = null
    var buttonBack: Button? = null

    override fun onStart() {
        super.onStart()
        buttonSignUp = findViewById(R.id.buttonReg2)
        buttonBack = findViewById(R.id.buttonBack)

        buttonBack!!.setOnClickListener() {
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        buttonSignUp!!.setOnClickListener() {
            print("!")
            val okHttpClient: OkHttpClient = UnsafeOkHttpClient().getUnsafeOkHttpClient()!!
            val retrofit = Retrofit.Builder()
                .baseUrl("https://feivur.ru/")
                .client(okHttpClient)
                .build()
            val retrofitInterface = retrofit.create(RetrofitInterface::class.java)
            val jsonObject = JSONObject()
            val name: EditText = findViewById(R.id.editTextTextPersonName2)
            val login: EditText = findViewById(R.id.editTextTextEmailAddress2)
            val password: EditText = findViewById(R.id.editTextTextPassword2)
            val info: TextView = findViewById(R.id.textView2)
            jsonObject.put("name", name.text)
            jsonObject.put("login", login.text)
            jsonObject.put("password", password.text)

            val jsonObjectString = jsonObject.toString()

            // Create RequestBody ( We're not using any converter, like GsonConverter, MoshiConverter e.t.c, that's why we use RequestBody )
            val requestBody = RequestBody.create(
                MediaType.get("application/json; charset=utf-8"),
                jsonObjectString
            )

            CoroutineScope(Dispatchers.IO).launch {
                // Do the POST request and get response
                val response = retrofitInterface.sign(requestBody)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {

                        // Convert raw JSON to pretty JSON using GSON library
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        val prettyJson = gson.toJson(
                            JsonParser().parse(
                                response.body()
                                    ?.string() // About this thread blocking annotation : https://github.com/square/retrofit/issues/3255
                            )
                        )

                        Log.d("Pretty Printed JSON :", prettyJson)
                        setContentView(R.layout.activity_login)

                    } else {

                        Log.e("RETROFIT_ERROR", response.code().toString())
                        info.text = "missing sign up"
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
    }


}