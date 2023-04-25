package com.example.a3d_printing_service_front

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a3d_printing_service_front.configs.UnsafeOkHttpClient
import com.example.a3d_printing_service_front.interfaces.RetrofitInterface
import com.example.a3d_printing_service_front.pojo.CreateOrderPojo
import com.example.a3d_printing_service_front.pojo.OrderPojo
import com.example.a3d_printing_service_front.pojo.OrdersPojo
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import java.io.ByteArrayOutputStream
import java.io.IOException


class MainActivity : Activity() {
    private val rtspUri = Uri.parse("rtsp://feivur.ru:8554/test1")

    private var playerView: StyledPlayerView? = null
    private var player: ExoPlayer? = null
    private var imageView: ImageView? = null
    private var imageView2: ImageView? = null
    private var editTextTextMultiLine: EditText? = null
    private var textView: TextView? = null
    private var recyclerView: RecyclerView? = null

    private val gson = Gson()
    private val retrofitCreator = RetrofitCreator()
    private var orders = mutableListOf<OrderPojo>()

    @SuppressLint("MissingInflatedId", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        playerView = findViewById(R.id.playerView)
        imageView = findViewById(R.id.imageView)
        imageView2 = findViewById(R.id.imageView2)
        editTextTextMultiLine = findViewById(R.id.editTextTextMultiLine)
        textView = findViewById(R.id.textView3)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        recyclerView?.adapter = RecycleViewOrdersAdapter(orders)

    }

    override fun onStart() {
        super.onStart()
        player = ExoPlayer.Builder(this).build()
        player?.setMediaItem(MediaItem.fromUri(rtspUri))
        player?.prepare()
        playerView?.player = player
        getOrders()
    }

    override fun onStop() {
        super.onStop()
        player?.stop()
        player?.release()
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
        try {
            val baos = ByteArrayOutputStream()
            if (editTextTextMultiLine!!.text.toString() == "") {
                textView!!.text = "Description is empty!"
                return
            }
            if (imageView!!.drawable == null) {
                textView!!.text = "Image is empty!"
                return
            }
            imageView!!.drawable.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, baos)
            retrofitCreator.createOrder(
                gson.toJson(
                    CreateOrderPojo(
                        description = editTextTextMultiLine!!.text.toString(),
                        photo = baos.toByteArray()
                    )
                )
            )
            println("Новый ордер успешно создан. Перезагружаем список ордеров...")
            getOrders()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            textView!!.text = "Неизвестная ошибка"
        }
    }

    private fun getOrders() {
        CoroutineScope(Dispatchers.IO).launch {
            val responseOrders = retrofitCreator.getOrders()?.orders
            withContext(Dispatchers.Main) {
                println("responseOrders.size: " + responseOrders?.size)
                orders.clear()
                for (i in responseOrders!!) orders.add(i)
            }
        }
    }
}