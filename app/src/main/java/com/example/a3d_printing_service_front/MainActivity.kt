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

    private var recyclerView: RecyclerView? = null
    private val retrofitCreator = RetrofitCreator()
    private var orders = mutableListOf<OrderPojo>()
    private lateinit var alertDialog: AlertDialog
    private lateinit var progressDialog: ProgressDialog

    @SuppressLint("MissingInflatedId", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(this)

        recyclerView?.adapter = RecycleViewOrdersAdapter(orders, createOnStateClickListener())
        alertDialog = AlertDialog.Builder(this@MainActivity).create()

        progressDialog = ProgressDialog(this, R.style.MyTheme)
        progressDialog.setCancelable(false)
        progressDialog.setProgressStyle(com.google.android.material.R.style.Base_Widget_AppCompat_ProgressBar)

        //getOrders()
    }

    override fun onStart() {
        super.onStart()
        if (Storage.refreshOrdersFlag) {
            getOrders()
            Storage.refreshOrdersFlag = false
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
                    alertDialog = AlertDialog.Builder(this@MainActivity).create()
                    progressDialog.dismiss()
                }
            }
        }
    }

    private fun createOnStateClickListener() = object :
        RecycleViewOrdersAdapter.OnStateClickListener {

        override fun onStateClick(name: OrderPojo, position: Int) {
            val intent = Intent(this@MainActivity, OrderActivity::class.java)
            println(name.status)
            intent.putExtra("id", name.id)
            intent.putExtra("description", name.description)
            intent.putExtra("status", name.status)
            intent.putExtra("photo", name.photo)
            intent.putExtra("price", name.price)
            intent.putExtra("track", name.track)
            intent.putExtra("paymentAddress", name.paymentAddress)
            startActivity(intent)
        }
    }

    fun toCreateOrder(view: View) {
        val intent = Intent(this@MainActivity, CreateOrderActivity::class.java)
        startActivity(intent)
    }

    fun toLogout(view: View) {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
    }

    fun toRefresh(view: View) {
        getOrders()
    }
}