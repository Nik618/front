package com.example.a3d_printing_service_front

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a3d_printing_service_front.pojo.CreateOrderPojo
import com.example.a3d_printing_service_front.pojo.OrderPojo
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.IOException


class MainActivity : Activity() {

    private var imageView: ImageView? = null
    private var editTextTextMultiLine: EditText? = null
    private var recyclerView: RecyclerView? = null
    private var cardView: CardView? = null
    private val gson = Gson()
    private val retrofitCreator = RetrofitCreator()
    private var orders = mutableListOf<OrderPojo>()
    private lateinit var alertDialog: AlertDialog
    private lateinit var progressDialog: ProgressDialog

    @SuppressLint("MissingInflatedId", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cardView = findViewById(R.id.cardView)
        imageView = findViewById(R.id.imageView)
        editTextTextMultiLine = findViewById(R.id.editTextTextMultiLine)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(this)

        recyclerView?.adapter = RecycleViewOrdersAdapter(orders, createOnStateClickListener())
        alertDialog = AlertDialog.Builder(this@MainActivity).create()

        progressDialog = ProgressDialog(this, R.style.MyTheme)
        progressDialog.setCancelable(false)
        progressDialog.setProgressStyle(com.google.android.material.R.style.Base_Widget_AppCompat_ProgressBar)

        getOrders()
    }

    override fun onStart() {
        super.onStart()
    }

    fun loadImage(view: View) {
        println("orders.size3: " + orders.size)
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, imageReturnedIntent: Intent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent)
        var bitmap: Bitmap? = null
        when (requestCode) {
            1 -> if (resultCode == RESULT_OK) {
                val selectedImage = imageReturnedIntent.data
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                imageView!!.setImageBitmap(bitmap)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun createOrder(view: View) {
        progressDialog.show()
        val baos = ByteArrayOutputStream()
        if (editTextTextMultiLine!!.text.toString() == "") {
            alertDialog.setMessage("Description must not be empty!")
            alertDialog.show()
            return
        }
        imageView!!.drawable.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, baos)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = retrofitCreator.createOrder(
                    gson.toJson(
                        CreateOrderPojo(
                            description = editTextTextMultiLine!!.text.toString(),
                            photo = baos.toByteArray(),
                            user = Storage.user
                        )
                    )
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d("RESPONSE: ", response.body().toString())
                        getOrders()
                        println("New order successful create. Refreshing...")
                        alertDialog.setMessage("New order successful create!")
                        alertDialog.show()
                        editTextTextMultiLine?.setText("")
                        imageView?.setImageDrawable(null)
                        imageView?.setImageResource(R.drawable.empty_photo)
                    } else {
                        Log.e("RETROFIT ERROR: ", response.code().toString())
                        throw Exception("RETROFIT ERROR: ${response.code()}")
                    }
                }
            } catch (e: java.lang.Exception) {
                withContext(Dispatchers.Main) {
                    alertDialog.setMessage("Unable to create order: ${e.localizedMessage}")
                    alertDialog.show()
                }
            }
        }

    }

    private fun getOrders() {
        progressDialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val responseOrders = retrofitCreator.getOrders(Storage.user)?.orders
                withContext(Dispatchers.Main) {
                    println("responseOrders.size: " + responseOrders?.size)
                    orders.clear()
                    if (responseOrders != null)
                        for (i in responseOrders) orders.add(i)
                    orders.reverse()
                    recyclerView?.adapter =
                        RecycleViewOrdersAdapter(orders, createOnStateClickListener())
                    recyclerView?.refreshDrawableState()
                    progressDialog.dismiss()
                }
            } catch (e: java.lang.Exception) {
                withContext(Dispatchers.Main) {
                    alertDialog.setMessage("Unable to get orders: ${e.localizedMessage}")
                    alertDialog.show()
                }
            }
        }
    }

    private fun createOnStateClickListener() = object :
        RecycleViewOrdersAdapter.OnStateClickListener {

        override fun onStateClick(name: OrderPojo, position: Int) {
            val intent = Intent(this@MainActivity, OrderActivity::class.java)
            intent.putExtra("id", name.id)
            intent.putExtra("description", name.description)
            intent.putExtra("status", name.status)
            intent.putExtra("photo", name.photo)
            intent.putExtra("price", name.price)
            intent.putExtra("track", name.track)
            startActivity(intent)
        }
    }
}