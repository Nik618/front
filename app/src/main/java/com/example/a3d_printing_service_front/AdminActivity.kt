package com.example.a3d_printing_service_front

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a3d_printing_service_front.pojo.OrderPojo
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminActivity : AppCompatActivity() {

    private var imageView: ImageView? = null
    private var editTextTextMultiLine: EditText? = null
    private var recyclerView: RecyclerView? = null

    private val gson = Gson()
    private val retrofitCreator = RetrofitCreator()
    private var orders = mutableListOf<OrderPojo>()
    private lateinit var alertDialog: AlertDialog
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        recyclerView = findViewById(R.id.recyclerViewAdmin)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        alertDialog = AlertDialog.Builder(this@AdminActivity).create()

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
                val responseOrders = retrofitCreator.getOrders("")?.orders
                withContext(Dispatchers.Main) {
                    println("responseOrders.size: " + responseOrders?.size)
                    orders.clear()
                    if (responseOrders != null)
                        for (i in responseOrders) orders.add(i)
                    orders.reverse()
                    val stateClickListener: RecycleViewOrdersAdapter.OnStateClickListener = object :
                        RecycleViewOrdersAdapter.OnStateClickListener {

                        override fun onStateClick(name: OrderPojo, position: Int) {
                            val intent = Intent(this@AdminActivity, OrderAdminActivity::class.java)
                            println(intent.javaClass)
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
                    recyclerView?.adapter = RecycleViewOrdersAdapter(orders, stateClickListener)
                    recyclerView?.refreshDrawableState()
                    progressDialog.dismiss()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    alertDialog.setMessage("Unable to get orders: ${e.localizedMessage}")
                    alertDialog.show()
                }
            }
        }
    }

    fun toLogout(view: View) {
        val intent = Intent(this@AdminActivity, LoginActivity::class.java)
        startActivity(intent)
    }

    fun toRefresh(view: View) {
        getOrders()
    }
}