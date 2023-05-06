package com.example.a3d_printing_service_front

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a3d_printing_service_front.pojo.OrderPojo
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminActivity : AppCompatActivity() {

    private var recyclerView: RecyclerView? = null
    private val retrofitSender = RetrofitSender()
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
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        if (Storage.refreshOrdersFlag) {
            getOrders()
            Storage.refreshOrdersFlag = false
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getOrders() {
        progressDialog.show()
        retrofitSender.refreshTokens()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val responseOrders = retrofitSender.getOrders("")?.orders
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
                    alertDialog.setMessage("Невозможно получить список заказов: ${e.localizedMessage}")
                    alertDialog.show()
                }
            }
        }
    }

    fun toLogout(view: View) {
        val intent = Intent(this@AdminActivity, LoginActivity::class.java)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toRefresh(view: View) {
        getOrders()
    }
}