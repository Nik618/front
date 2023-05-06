package com.example.a3d_printing_service_front

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.a3d_printing_service_front.pojo.OrderPojo
import com.example.a3d_printing_service_front.pojo.StartVideoPojo
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException


class OrderAdminActivity : Activity() {
    private var orderTextViewPreviewAdmin: TextView? = null
    private var orderTextViewFullAdmin: TextView? = null
    private var orderImageViewAdmin: ImageView? = null
    private val retrofitCreator = RetrofitCreator()
    private var id: Int? = null
    private var description: String? = null
    private var photo: ByteArray? = null
    private var status: String? = null
    private var price: String? = null
    private var track: String? = null
    private var paymentAddress: String? = null
    private var fileData: ByteArray? = null
    private var editTextPrice: EditText? = null
    private var buttonSetPrice: Button? = null
    private var priceLayout: LinearLayout? = null
    private var acceptPayLayout: LinearLayout? = null
    private var stopVideoLayout: LinearLayout? = null
    private var prepareToDeliveryLayout: LinearLayout? = null
    private var layoutTrack: LinearLayout? = null

    private var editCameraId: EditText? = null
    private var editPathId: EditText? = null

    private lateinit var progressDialog: ProgressDialog
    private lateinit var alertDialog: AlertDialog



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_admin)
        orderTextViewPreviewAdmin = findViewById(R.id.orderTextViewPreviewAdmin)
        orderTextViewFullAdmin = findViewById(R.id.orderTextViewFullAdmin)
        orderImageViewAdmin = findViewById(R.id.orderImageViewAdmin)
        editTextPrice = findViewById(R.id.editTextPrice)
        buttonSetPrice = findViewById(R.id.buttonSetPrice)
        priceLayout = findViewById(R.id.priceLayout)
        acceptPayLayout = findViewById(R.id.acceptPayLayout)
        stopVideoLayout = findViewById(R.id.stopVideoLayout)
        prepareToDeliveryLayout = findViewById(R.id.prepareToDeliveryLayout)
        layoutTrack = findViewById(R.id.layoutTrack)
        editCameraId = findViewById(R.id.editCameraId)
        editPathId = findViewById(R.id.editPathId)

        progressDialog = ProgressDialog(this, R.style.MyTheme)
        progressDialog.setCancelable(false)
        progressDialog.setProgressStyle(com.google.android.material.R.style.Base_Widget_AppCompat_ProgressBar)

//        id = intent.getIntExtra("id", -1)
//        description = intent.getStringExtra("description")
//        photo = intent.getByteArrayExtra("photo")
//        status = intent.getStringExtra("status")
//        price = intent.getStringExtra("price")
//        track = intent.getStringExtra("track")
//        paymentAddress = intent.getStringExtra("paymentAddress")
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()

        progressDialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            id = intent.getIntExtra("id", -1)
            description = intent.getStringExtra("description")
            photo = intent.getByteArrayExtra("photo")
            price = intent.getStringExtra("price")
            track = intent.getStringExtra("track")
            status = intent.getStringExtra("status")
            paymentAddress = intent.getStringExtra("paymentAddress")
            val thisOrderStatus = retrofitCreator.getStatus(id!!)?.result
            withContext(Dispatchers.Main) {
                status = thisOrderStatus
                progressDialog.dismiss()
                alertDialog = AlertDialog.Builder(this@OrderAdminActivity).create()
                orderTextViewPreviewAdmin?.text = "$id | $status"
                orderTextViewFullAdmin?.text = "$description"
                orderImageViewAdmin?.setImageBitmap(BitmapFactory.decodeByteArray(photo, 0, photo!!.size))

                when (status) {
                    "NEW" -> {
                        priceLayout?.visibility = View.VISIBLE
                        acceptPayLayout?.visibility = View.GONE
                        stopVideoLayout?.visibility = View.GONE
                        prepareToDeliveryLayout?.visibility = View.GONE
                        layoutTrack?.visibility = View.GONE
                    }
                    "WAITING TO PAYMENT" -> {
                        priceLayout?.visibility = View.GONE
                        acceptPayLayout?.visibility = View.VISIBLE
                        stopVideoLayout?.visibility = View.GONE
                        prepareToDeliveryLayout?.visibility = View.GONE
                        layoutTrack?.visibility = View.GONE
                    }
                    "PRINTING" -> {
                        priceLayout?.visibility = View.GONE
                        acceptPayLayout?.visibility = View.GONE
                        stopVideoLayout?.visibility = View.VISIBLE
                        prepareToDeliveryLayout?.visibility = View.GONE
                        layoutTrack?.visibility = View.GONE
                    }
                    "PREPARE TO DELIVERY" -> {
                        priceLayout?.visibility = View.GONE
                        acceptPayLayout?.visibility = View.GONE
                        stopVideoLayout?.visibility = View.GONE
                        prepareToDeliveryLayout?.visibility = View.VISIBLE
                        layoutTrack?.visibility = View.GONE
                    }
                    "IN DELIVERY" -> {
                        priceLayout?.visibility = View.GONE
                        acceptPayLayout?.visibility = View.GONE
                        stopVideoLayout?.visibility = View.GONE
                        prepareToDeliveryLayout?.visibility = View.GONE
                        layoutTrack?.visibility = View.VISIBLE
                    }
                }
            }
        }




    }

    override fun onStop() {
        super.onStop()
    }

    @SuppressLint("SetTextI18n")
    fun onClickAddPrice(view: View) {
        progressDialog.show()
        Storage.refreshOrdersFlag = true
        val price = editTextPrice?.text.toString()
        if (editTextPrice?.text.toString().matches(Regex("-?\\d+")))
            if (Integer.parseInt(price) in 1..99999) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = retrofitCreator.setPrice(
                            Gson().toJson(
                                OrderPojo(
                                    id = id,
                                    price = editTextPrice?.text.toString()
                                )
                            )
                        )
                        withContext(Dispatchers.Main) {
                            progressDialog.dismiss()
                            println(response)
                            status = "WAITING TO PAYMENT"
                            orderTextViewPreviewAdmin?.text = "$id | $status"
                            priceLayout?.visibility = View.GONE
                            acceptPayLayout?.visibility = View.VISIBLE
                            alertDialog.setMessage("Setting price successful!\nOrder status is WAITING FOR PAYMENT")
                            alertDialog.show()
                        }
                    } catch (e: java.lang.Exception) {
                        withContext(Dispatchers.Main) {
                            alertDialog.setMessage("Unable to create order: ${e.localizedMessage}")
                            alertDialog.show()
                            alertDialog = AlertDialog.Builder(this@OrderAdminActivity).create()
                        }
                    }
                }
            } else {
                println("!")
                alertDialog.setMessage("Price is incorrect")
                alertDialog.show()
                alertDialog = AlertDialog.Builder(this@OrderAdminActivity).create()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val uri: Uri? = data.data
            try {
                val output = contentResolver.openOutputStream(uri!!)!!
                output.write(fileData)
                output.flush()
                output.close()
            } catch (e: IOException) {
                println(e.message)
            }
        }
    }

    fun onDownloadModel(view: View) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = retrofitCreator.getFile(id!!)
                withContext(Dispatchers.Main) {

                    fileData = response!!.file
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    println(response.mimeType)
                    intent.type = response.mimeType

                    intent.putExtra(
                        Intent.EXTRA_TITLE,
                        "model-order-$id.${response.extension}"
                    )

                    startActivityForResult(intent, 1)
                }
            } catch (e: java.lang.Exception) {
                withContext(Dispatchers.Main) {
                    alertDialog.setMessage("Unable to create order: ${e.localizedMessage}")
                    alertDialog.show()
                    alertDialog = AlertDialog.Builder(this@OrderAdminActivity).create()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun onStartVideo(view: View) {
        progressDialog.show()
        Storage.refreshOrdersFlag = true
        CoroutineScope(Dispatchers.IO).launch {
            val response = retrofitCreator.startVideo(StartVideoPojo(orderId = id, cameraId = editCameraId?.text.toString(), pathId = editPathId?.text.toString()))
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                if (response?.status == "success") {
                    status = "PRINTING"
                    orderTextViewPreviewAdmin?.text = "$id | $status"
                    acceptPayLayout?.visibility = View.GONE
                    stopVideoLayout?.visibility = View.VISIBLE
                    alertDialog.setMessage("Success start video!")
                    alertDialog.show()
                    alertDialog = AlertDialog.Builder(this@OrderAdminActivity).create()
                } else {
                    alertDialog.setMessage(response?.errorMesssage)
                    alertDialog.show()
                    alertDialog = AlertDialog.Builder(this@OrderAdminActivity).create()
                }
            }
        }
    }

    fun onStopVideo(view: View) {
        progressDialog.show()
        Storage.refreshOrdersFlag = true
        CoroutineScope(Dispatchers.IO).launch {
            val response = retrofitCreator.stopVideo(id!!)
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                if (response?.status == "success") {
                    status = "PREPARE TO DELIVERY"
                    orderTextViewPreviewAdmin?.text = "$id | $status"
                    alertDialog.setMessage("Printing done! Order preparing to delivery")
                    stopVideoLayout?.visibility = View.GONE
                    prepareToDeliveryLayout?.visibility = View.VISIBLE
                    alertDialog.show()
                    alertDialog = AlertDialog.Builder(this@OrderAdminActivity).create()
                } else {
                    alertDialog.setMessage(response?.errorMesssage)
                    alertDialog.show()
                    alertDialog = AlertDialog.Builder(this@OrderAdminActivity).create()
                }
            }
        }
    }

    fun onTrack(view: View) {
        progressDialog.show()
        Storage.refreshOrdersFlag = true
        CoroutineScope(Dispatchers.IO).launch {
            val response = retrofitCreator.prepareToDelivery(OrderPojo(id = id))
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                if (response?.status == "success") {
                    status = "IN DELIVERY"
                    orderTextViewPreviewAdmin?.text = "$id | $status"
                    alertDialog.setMessage("Order in delivery, waiting to client approve")
                    prepareToDeliveryLayout?.visibility = View.GONE
                    alertDialog.show()
                    alertDialog = AlertDialog.Builder(this@OrderAdminActivity).create()
                } else {
                    alertDialog.setMessage(response?.errorMesssage)
                    alertDialog.show()
                    alertDialog = AlertDialog.Builder(this@OrderAdminActivity).create()
                }
            }
        }
    }

    fun toLogout(view: View) {
        val intent = Intent(this@OrderAdminActivity, LoginActivity::class.java)
        startActivity(intent)
    }

    fun toRefresh(view: View) {
        progressDialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            val thisOrder = retrofitCreator.getOrder(id!!)
            withContext(Dispatchers.Main) {
                if (status != thisOrder?.status) {
                    Storage.refreshOrdersFlag = true
                    status = thisOrder?.status
                    price = thisOrder?.price
                    track = thisOrder?.track
                    paymentAddress = thisOrder?.paymentAddress
                }
                progressDialog.dismiss()
            }
        }
    }

    fun showImage(view: View) {
        val intent = Intent(this@OrderAdminActivity, ImageActivity::class.java)
        intent.putExtra("order_id", id)
        startActivity(intent)
    }
}