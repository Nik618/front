package com.example.a3d_printing_service_front

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import com.example.a3d_printing_service_front.pojo.CreateOrderPojo
import com.example.a3d_printing_service_front.pojo.JwtPojo
import com.example.a3d_printing_service_front.pojo.TokenRequestPojo
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*


class CreateOrderActivity : Activity() {
    private var imageView: ImageView? = null
    private var imageViewModel: ImageView? = null
    private var editTextTextMultiLine: EditText? = null
    private val gson = Gson()
    private val retrofitSender = RetrofitSender()
    private lateinit var alertDialog: AlertDialog
    private lateinit var progressDialog: ProgressDialog

    private var fileData: ByteArray? = null
    private var extension: String? = null
    private var mimeType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_order)

        imageView = findViewById(R.id.imageView)
        imageViewModel = findViewById(R.id.imageViewModel)
        editTextTextMultiLine = findViewById(R.id.editTextTextMultiLine)
        alertDialog = AlertDialog.Builder(this@CreateOrderActivity).create()
        progressDialog = ProgressDialog(this, R.style.MyTheme)
        progressDialog.setCancelable(false)
        progressDialog.setProgressStyle(com.google.android.material.R.style.Base_Widget_AppCompat_ProgressBar)
    }

    fun loadImage(view: View) {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, 1)
    }

    @SuppressLint("Recycle")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        var bitmap: Bitmap? = null
        when (requestCode) {
            1 -> if (resultCode == RESULT_OK) {
                val selectedImage = data.data
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                imageView!!.setImageBitmap(bitmap)
            }
            2 -> {
                val uri = data.data!!
                val inputStream = contentResolver.openInputStream(uri)
                fileData = inputStream!!.readBytes()
                println(fileData!!.size)

                try {
                    contentResolver.query(uri, null, null, null, null)
                        .use { returnCursor ->
                            if (returnCursor != null && returnCursor.moveToFirst()) {
                                val mimeIndex: Int =
                                    returnCursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
                                extension = MimeTypeMap.getSingleton()
                                    .getExtensionFromMimeType(returnCursor.getString(mimeIndex))
                                println(extension)
                                mimeType = returnCursor.getString(mimeIndex)
                                println(mimeType)
                            }
                        }
                } catch (ignore: java.lang.Exception) {
                    println(ignore.message)
                    alertDialog.setMessage("Невозможно загрузить фотографию - формат не поддерживается или файл слишком большой")
                    alertDialog.show()
                }
                imageViewModel?.setImageResource(R.drawable.loaded_model)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    fun createOrder(view: View) {
        Storage.refreshOrdersFlag = true
        progressDialog.show()
        val baos = ByteArrayOutputStream()
        if (editTextTextMultiLine!!.text.toString() == "") {
            alertDialog.setMessage("Описание заказа не может быть пустым")
            alertDialog.show()
            progressDialog.dismiss()
            return
        }
        if (fileData == null) {
            alertDialog.setMessage("Необходимо загрузить файл модели")
            alertDialog.show()
            progressDialog.dismiss()
            return
        }
        imageView!!.drawable.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, baos)

        retrofitSender.refreshTokens()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = retrofitSender.createOrder(
                    gson.toJson(
                        CreateOrderPojo(
                            description = editTextTextMultiLine!!.text.toString(),
                            photo = baos.toByteArray(),
                            file = fileData,
                            extension = extension,
                            mimeType = mimeType,
                            user = Storage.user
                        )
                    )
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d("RESPONSE: ", response.body().toString())
                        println("New order successful create")
                        progressDialog.dismiss()
//                        alertDialog.setMessage("Order successful create!")
//                        alertDialog.show()
                        editTextTextMultiLine?.setText("")
                        imageView?.setImageDrawable(null)
                        imageView?.setImageResource(R.drawable.empty_photo)
                        val intent = Intent(this@CreateOrderActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.e("RETROFIT ERROR: ", response.code().toString())
                        throw Exception("RETROFIT ERROR: ${response.code()}")
                    }
                }
            } catch (e: java.lang.Exception) {
                withContext(Dispatchers.Main) {
                    alertDialog.setMessage("Unable to create order: ${e.localizedMessage}")
                    alertDialog.show()
                    progressDialog.dismiss()
                }
            }
        }

    }

    fun loadModel(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(Intent.createChooser(intent, "Выберите файл для загрузки "), 2)
    }

    private fun getMimeType(uri: Uri): String {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val cr = this.contentResolver
            cr.getType(uri)!!
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(
                uri
                    .toString()
            )
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.lowercase(Locale.getDefault())
            )!!
        }
    }


}