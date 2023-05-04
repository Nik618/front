package com.example.a3d_printing_service_front

import android.app.AlertDialog
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

    private lateinit var alertDialog: AlertDialog

    override fun onStart() {
        super.onStart()
        buttonSignUp = findViewById(R.id.buttonReg2)
        alertDialog = AlertDialog.Builder(this@SignUpActivity).create()
        buttonSignUp!!.setOnClickListener() {
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


            if (login.text.isEmpty() || login.text.length > 20) {
                alertDialog.setMessage("login must be more than 20 characters or empty")
                alertDialog.show()
                alertDialog = AlertDialog.Builder(this@SignUpActivity).create()
                return@setOnClickListener
            }

            if (password.text.length < 8 || password.text.length > 30) {
                alertDialog.setMessage("login must be less 8 and more than 30 characters")
                alertDialog.show()
                alertDialog = AlertDialog.Builder(this@SignUpActivity).create()
                return@setOnClickListener
            }


            jsonObject.put("name", name.text)
            jsonObject.put("login", login.text)
            jsonObject.put("password", password.text)

            val jsonObjectString = jsonObject.toString()

            val requestBody = RequestBody.create(
                MediaType.get("application/json; charset=utf-8"),
                jsonObjectString
            )

            CoroutineScope(Dispatchers.IO).launch {
                val response = retrofitInterface.sign(requestBody)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val gson = GsonBuilder().setPrettyPrinting().create() //TODO вылет при регистрации
                        val prettyJson = gson.toJson(
                            JsonParser().parse(
                                response.body()
                                    ?.string()
                            )
                        )

                        Log.d("Pretty Printed JSON :", prettyJson)
                        setContentView(R.layout.activity_login)

                    } else {
                        Log.e("RETROFIT_ERROR", response.code().toString())
                        alertDialog.setMessage("technical works, please wait")
                        alertDialog.show()
                        alertDialog = AlertDialog.Builder(this@SignUpActivity).create()
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