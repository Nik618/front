package com.example.a3d_printing_service_front

import android.app.ProgressDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ImageActivity : AppCompatActivity() {
    private var mImageView: ImageView? = null
    private var retrofitCreator = RetrofitCreator()
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progressDialog = ProgressDialog(this, R.style.MyTheme)
        progressDialog.setCancelable(false)
        progressDialog.setProgressStyle(com.google.android.material.R.style.Base_Widget_AppCompat_ProgressBar)

        mImageView = findViewById(R.id.my_image_view)

        val id = intent.extras?.getInt("order_id")

        progressDialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            val imageData = retrofitCreator.getPhoto(id!!)?.photo
            withContext(Dispatchers.Main) {
                mImageView?.setImageBitmap(
                    BitmapFactory.decodeByteArray(
                        imageData,
                        0,
                        imageData!!.size
                    )
                )
                progressDialog.dismiss()
            }
        }

    }
}