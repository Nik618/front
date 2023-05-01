package com.example.a3d_printing_service_front

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView

class OrderAdminActivity : AppCompatActivity() {
    private var orderTextViewPreviewAdmin: TextView? = null
    private var orderTextViewFullAdmin: TextView? = null
    private var orderImageViewAdmin: ImageView? = null

    private var id: Int? = null
    private var description: String? = null
    private var photo: ByteArray? = null
    private var status: String? = null
    private var price: String? = null
    private var track: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_admin)
        orderTextViewPreviewAdmin = findViewById(R.id.orderTextViewPreviewAdmin)
        orderTextViewFullAdmin = findViewById(R.id.orderTextViewFullAdmin)
        orderImageViewAdmin = findViewById(R.id.orderImageViewAdmin)
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

        orderTextViewPreviewAdmin?.text = "$id | $status\n\n" +
                "New order"
        orderTextViewFullAdmin?.text = "$description"
        orderImageViewAdmin?.setImageBitmap(BitmapFactory.decodeByteArray(photo, 0, photo!!.size))

    }

    override fun onStop() {
        super.onStop()
    }

    fun onClickAddPrice(view: View) {}
}