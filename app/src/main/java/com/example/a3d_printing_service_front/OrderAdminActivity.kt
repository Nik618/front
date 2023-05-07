package com.example.a3d_printing_service_front

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
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
    private val retrofitSender = RetrofitSender()
    private var id: Int? = null
    private var description: String? = null
    private var photo: ByteArray? = null
    private var status: String? = null
    private var price: String? = null
    private var track: String? = null
    private var paymentAddress: String? = null
    private var address: String? = null
    private var fileData: ByteArray? = null
    private var editTextPrice: EditText? = null
    private var buttonSetPrice: Button? = null
    private var priceLayout: LinearLayout? = null
    private var acceptPayLayout: LinearLayout? = null
    private var stopVideoLayout: LinearLayout? = null
    private var prepareToDeliveryLayout: LinearLayout? = null
    private var editTrack: EditText? = null

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
        editCameraId = findViewById(R.id.editCameraId)
        editPathId = findViewById(R.id.editPathId)
        editTrack = findViewById(R.id.editTrack)

        progressDialog = ProgressDialog(this, R.style.MyTheme)
        progressDialog.setCancelable(false)
        progressDialog.setProgressStyle(com.google.android.material.R.style.Base_Widget_AppCompat_ProgressBar)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()
        retrofitSender.refreshTokens()
        progressDialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            id = intent.getIntExtra("id", -1)
            description = intent.getStringExtra("description")
            photo = intent.getByteArrayExtra("photo")
            price = intent.getStringExtra("price")
            track = intent.getStringExtra("track")
            status = intent.getStringExtra("status")
            address = intent.getStringExtra("address")
            paymentAddress = intent.getStringExtra("paymentAddress")
            val thisOrderStatus = retrofitSender.getStatus(id!!)?.result
            withContext(Dispatchers.Main) {
                status = thisOrderStatus
                progressDialog.dismiss()
                alertDialog = AlertDialog.Builder(this@OrderAdminActivity).create()
                orderTextViewPreviewAdmin?.text = "№$id | " + when (status) {
                    "NEW" -> "НОВЫЙ"
                    "WAITING TO PAYMENT" -> "ОЖИДАЕТ ПОСТУПЛЕНИЯ СРЕДСТВ"
                    "PRINTING" -> "ПЕЧАТАЕТСЯ"
                    "PREPARE TO DELIVERY" -> "ГОТОВИТСЯ К ДОСТАВКЕ"
                    "IN DELIVERY" -> "В ПУТИ"
                    "DONE" -> "ПОЛУЧЕН"
                    else -> {}
                }
                "\n\nАдрес для доставки:\n$address"
                orderTextViewFullAdmin?.text = "Описание заказа:\n\n$description"
                if (photo?.size != 0)
                    orderImageViewAdmin?.setImageBitmap(
                        BitmapFactory.decodeByteArray(
                            photo,
                            0,
                            photo!!.size
                        )
                    )

                when (status) {
                    "NEW" -> {
                        priceLayout?.visibility = View.VISIBLE
                        acceptPayLayout?.visibility = View.GONE
                        stopVideoLayout?.visibility = View.GONE
                        prepareToDeliveryLayout?.visibility = View.GONE
                    }
                    "WAITING TO PAYMENT" -> {
                        priceLayout?.visibility = View.GONE
                        acceptPayLayout?.visibility = View.VISIBLE
                        stopVideoLayout?.visibility = View.GONE
                        prepareToDeliveryLayout?.visibility = View.GONE
                    }
                    "PRINTING" -> {
                        priceLayout?.visibility = View.GONE
                        acceptPayLayout?.visibility = View.GONE
                        stopVideoLayout?.visibility = View.VISIBLE
                        prepareToDeliveryLayout?.visibility = View.GONE
                    }
                    "PREPARE TO DELIVERY" -> {
                        priceLayout?.visibility = View.GONE
                        acceptPayLayout?.visibility = View.GONE
                        stopVideoLayout?.visibility = View.GONE
                        prepareToDeliveryLayout?.visibility = View.VISIBLE
                    }
                    "IN DELIVERY" -> {
                        priceLayout?.visibility = View.GONE
                        acceptPayLayout?.visibility = View.GONE
                        stopVideoLayout?.visibility = View.GONE
                        prepareToDeliveryLayout?.visibility = View.GONE
                    }
                    "DONE" -> {
                        priceLayout?.visibility = View.GONE
                        acceptPayLayout?.visibility = View.GONE
                        stopVideoLayout?.visibility = View.GONE
                        prepareToDeliveryLayout?.visibility = View.GONE
                        alertDialog.setMessage("Клиент подтвердил получение посылки, заказ завершён!")
                        alertDialog.show()
                    }
                }
            }
        }


    }

    override fun onStop() {
        super.onStop()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    fun onClickAddPrice(view: View) {
        progressDialog.show()
        Storage.refreshOrdersFlag = true
        val price = editTextPrice?.text.toString()
        if (!editTextPrice?.text.toString().matches(Regex("-?\\d+"))) {
            progressDialog.dismiss()
            alertDialog.setMessage("Цена указана некорректно")
            alertDialog.show()
            alertDialog = AlertDialog.Builder(this@OrderAdminActivity).create()
            return
        }

        if (Integer.parseInt(price) in 1..99999) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = retrofitSender.setPrice(
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
//                        status = "WAITING TO PAYMENT"
//                        orderTextViewPreviewAdmin?.text = "$id | $status"
                        onStart()
                        priceLayout?.visibility = View.GONE
                        acceptPayLayout?.visibility = View.VISIBLE
                        alertDialog.setMessage("Цена успешно установлена. Необходимо подтвердить получение средств после оплаты заказа клиентом")
                        alertDialog.show()
                    }
                } catch (e: java.lang.Exception) {
                    withContext(Dispatchers.Main) {
                        alertDialog.setMessage("Невозможно установить цену: ${e.localizedMessage}")
                        alertDialog.show()
                        alertDialog = AlertDialog.Builder(this@OrderAdminActivity).create()
                        progressDialog.dismiss()
                    }
                }
            }
        } else {
            alertDialog.setMessage("Цена указана некорректно")
            alertDialog.show()
            alertDialog = AlertDialog.Builder(this@OrderAdminActivity).create()
            progressDialog.dismiss()
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun onDownloadModel(view: View) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = retrofitSender.getFile(id!!)
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
                    alertDialog.setMessage("Невозможно создать оффер: ${e.localizedMessage}")
                    alertDialog.show()
                    alertDialog = AlertDialog.Builder(this@OrderAdminActivity).create()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    fun onStartVideo(view: View) {
        progressDialog.show()
        Storage.refreshOrdersFlag = true
        CoroutineScope(Dispatchers.IO).launch {
            val response = retrofitSender.startVideo(
                StartVideoPojo(
                    orderId = id,
                    cameraId = editCameraId?.text.toString(),
                    pathId = editPathId?.text.toString()
                )
            )
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                if (response?.status == "success") {
//                    status = "PRINTING"
//                    orderTextViewPreviewAdmin?.text = "$id | $status"
                    onStart()
                    acceptPayLayout?.visibility = View.GONE
                    stopVideoLayout?.visibility = View.VISIBLE
                    alertDialog.setMessage("Видео-трансляция успешно запущена! Данные отправлены клиенту")
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

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    fun onStopVideo(view: View) {
        progressDialog.show()
        Storage.refreshOrdersFlag = true
        CoroutineScope(Dispatchers.IO).launch {
            val response = retrofitSender.stopVideo(id!!)
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                if (response?.status == "success") {
//                    status = "PREPARE TO DELIVERY"
//                    orderTextViewPreviewAdmin?.text = "№$id | " +
//                            when (status) {
//                                "NEW" -> "НОВЫЙ"
//                                "WAITING TO PAYMENT" -> "ОЖИДАЕТ ПОСТУПЛЕНИЯ СРЕДСТВ"
//                                "PRINTING" -> "ПЕЧАТАЕТСЯ"
//                                "PREPARE TO DELIVERY" -> "ГОТОВИТСЯ К ДОСТАВКЕ"
//                                "IN DELIVERY" -> "В ПУТИ"
//                                "DONE" -> "ПОЛУЧЕН"
//                                else -> {}
//                            }
                    onStart()
                    alertDialog.setMessage("Статус заказа изменён. Необходимо отправить заказ клиенту и передать ему трек-номер для отслеживания посылки")
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun onTrack(view: View) {
        progressDialog.show()
        Storage.refreshOrdersFlag = true
        CoroutineScope(Dispatchers.IO).launch {
            val response = retrofitSender.prepareToDelivery(
                OrderPojo(
                    id = id,
                    track = editTrack?.text.toString()
                )
            )
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                if (response?.status == "success") {
//                    status = "IN DELIVERY"
//                    orderTextViewPreviewAdmin?.text = "$id | $status"
                    onStart()
                    alertDialog.setMessage("Статус заказа изменён. Ожидается подтверждение получения заказа со стороны клиента")
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun toRefresh(view: View) {
        progressDialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            val thisOrder = retrofitSender.getOrder(id!!)
            withContext(Dispatchers.Main) {
                if (status != thisOrder?.status) {
                    Storage.refreshOrdersFlag = true
                    status = thisOrder?.status
                    price = thisOrder?.price
                    track = thisOrder?.track
                    paymentAddress = thisOrder?.paymentAddress
                }
                progressDialog.dismiss()
                onStart()
            }
        }
    }

    fun showImage(view: View) {
        val intent = Intent(this@OrderAdminActivity, ImageActivity::class.java)
        intent.putExtra("order_id", id)
        startActivity(intent)
    }
}