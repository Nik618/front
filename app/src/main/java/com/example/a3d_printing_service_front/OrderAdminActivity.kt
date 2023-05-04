package com.example.a3d_printing_service_front

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.example.a3d_printing_service_front.pojo.OrderPojo
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
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()

        id = intent.getIntExtra("id", -1)
        description = intent.getStringExtra("description")
        photo = intent.getByteArrayExtra("photo")
        status = intent.getStringExtra("status")
        price = intent.getStringExtra("price")
        track = intent.getStringExtra("track")
        paymentAddress = intent.getStringExtra("paymentAddress")
        alertDialog = AlertDialog.Builder(this@OrderAdminActivity).create()
        orderTextViewPreviewAdmin?.text = "$id | $status"
        orderTextViewFullAdmin?.text = "$description"
        orderImageViewAdmin?.setImageBitmap(BitmapFactory.decodeByteArray(photo, 0, photo!!.size))

        if (status != "NEW") {
            //buttonSetPrice?.visibility = View.GONE
            priceLayout?.visibility = View.GONE
            priceLayout?.weightSum = 15F
        }

    }

    override fun onStop() {
        super.onStop()
    }

    @SuppressLint("SetTextI18n")
    fun onClickAddPrice(view: View) {
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
                            println(response)
                            status = "WAITING TO PAYMENT"
                            orderTextViewPreviewAdmin?.text = "$id | $status"
                            priceLayout?.visibility = View.GONE
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
}