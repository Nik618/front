package com.example.a3d_printing_service_front

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class OrderActivity : AppCompatActivity() {

    private var orderTextViewPreview: TextView? = null
    private var orderTextViewFull: TextView? = null
    private var orderImageView: ImageView? = null
    private var webView: WebView? = null
    private var playerView: StyledPlayerView? = null
    private var player: ExoPlayer? = null
    private var id: Int? = null
    private var description: String? = null
    private var photo: ByteArray? = null
    private var status: String? = null
    private var price: String? = null
    private var track: String? = null
    private var paymentAddress: String? = null
    private val retrofitCreator = RetrofitCreator()
    private var fabCancel: FloatingActionButton? = null

    private lateinit var progressDialog: ProgressDialog
    private lateinit var alertDialog: AlertDialog

    @SuppressLint("MissingInflatedId", "SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)
        playerView = findViewById(R.id.playerView)
        orderTextViewPreview = findViewById(R.id.orderTextViewPreview)
        orderTextViewFull = findViewById(R.id.orderTextViewFull)
        orderImageView = findViewById(R.id.orderImageView)
        fabCancel = findViewById(R.id.fabCancel)
        alertDialog = AlertDialog.Builder(this@OrderActivity).create()
        progressDialog = ProgressDialog(this, R.style.MyTheme)
        progressDialog.setCancelable(false)
        progressDialog.setProgressStyle(com.google.android.material.R.style.Base_Widget_AppCompat_ProgressBar)
    }

    @SuppressLint("SetTextI18n", "SetJavaScriptEnabled")
    override fun onStart() {
        super.onStart()
        progressDialog.show()
        id = intent.getIntExtra("id", -1)
        description = intent.getStringExtra("description")
        photo = intent.getByteArrayExtra("photo")
        price = intent.getStringExtra("price")
        track = intent.getStringExtra("track")
        status = intent.getStringExtra("status")
        paymentAddress = intent.getStringExtra("paymentAddress")
        CoroutineScope(Dispatchers.IO).launch {
            val thisOrderStatus = retrofitCreator.getStatus(id!!)?.result
            withContext(Dispatchers.Main) {
                status = thisOrderStatus
                progressDialog.dismiss()

                if (status == "NEW" || status == "WAITING TO PAYMENT") {
                    fabCancel?.visibility = View.VISIBLE
                } else fabCancel?.visibility = View.GONE

                if (status == "PRINTING") {
                    playerView?.visibility = View.VISIBLE
                    val dlc = DefaultLoadControl.Builder().setBufferDurationsMs(100, 100, 100, 100).build()
                    player = ExoPlayer.Builder(this@OrderActivity)
                        .setMediaSourceFactory(DefaultMediaSourceFactory(this@OrderActivity).setLiveTargetOffsetMs(0))
                        .setLoadControl(dlc).build()
                    CoroutineScope(Dispatchers.IO).launch {
                        println("\n\n\n$id\n\n")
                        val response = retrofitCreator.getVideo(id!!)
                        withContext(Dispatchers.Main) {
                            val ms = RtspMediaSource.Factory().createMediaSource(MediaItem.fromUri(response?.result!!))
                            player?.setMediaSource(ms)
                            player?.prepare()
                            player?.play()
                            playerView?.player = player
                        }
                    }
                } else playerView?.visibility = View.GONE

                //if



                orderTextViewPreview?.text = "$id | $status\n\n" +
                        when (status) {
                            "NEW" -> "Заказ обрабатывается, пожалуйста, подождите"
                            "WAITING TO PAYMENT" -> "Внесите оплату. Печать начнётся после подтверждения администратора"
                            "PRINTING" -> "Ваш заказ печатается"
                            "PREPARE TO DELIVERY" -> "Ваш заказ готов и передаётся в доставку"
                            "IN DELIVERY" -> "Ваш заказ в пути!"
                            else -> {}
                        }
                orderTextViewFull?.text = "$description"
                orderImageView?.setImageBitmap(BitmapFactory.decodeByteArray(photo, 0, photo!!.size))

                webView =
                    findViewById<WebView?>(R.id.webView).apply { settings.javaScriptEnabled = true }

                if (status == "WAITING TO PAYMENT") {
                    val webViewClient: WebViewClient = object : WebViewClient() {
                        @Deprecated("Deprecated in Java")
                        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                            view.loadUrl(url)
                            return true
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest
                        ): Boolean {
                            view.loadUrl(request.url.toString())
                            return true
                        }
                    }
                    webView!!.webViewClient = webViewClient
                    webView?.loadUrl(paymentAddress!!)


                    orderTextViewFull?.visibility = View.GONE
                } else webView?.visibility = View.GONE


            }
        }


    }

    override fun onStop() {
        super.onStop()
        player?.stop()
        player?.release()
    }

    fun toDelOrder(view: View) {

        val builder = AlertDialog.Builder(this)

        builder.setTitle("Confirm")
        builder.setMessage("Delete order? If order was paid, money will be returned")

        builder.setPositiveButton("YES") { dialog, _ ->
            CoroutineScope(Dispatchers.IO).launch {
                retrofitCreator.delOrder(id!!)
                withContext(Dispatchers.Main) {
                    Storage.refreshOrdersFlag = true
                    val intent = Intent(this@OrderActivity, MainActivity::class.java)
                    startActivity(intent)
                    alertDialog.setMessage("Order successfully deleted! If order was paid, money will be returned.")
                    alertDialog.show()
                    alertDialog = AlertDialog.Builder(this@OrderActivity).create()
                }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton(
            "NO"
        ) { dialog, _ ->
            dialog.dismiss()
            return@setNegativeButton
        }

        val alert = builder.create()
        alert.show()

    }

    fun toLogout(view: View) {
        val intent = Intent(this@OrderActivity, LoginActivity::class.java)
        startActivity(intent)
    }

    fun onClickApprove(view: View) {}

    fun toRefresh(view: View) {
        progressDialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            val thisOrder = retrofitCreator.getOrder(id!!)
            withContext(Dispatchers.Main) {
                if (status != thisOrder?.status) Storage.refreshOrdersFlag = true
                //id = thisOrder?.id
                //description = thisOrder?.description
                //photo = thisOrder?.photo
                status = thisOrder?.status
                price = thisOrder?.price
                track = thisOrder?.track
                paymentAddress = thisOrder?.paymentAddress
                progressDialog.dismiss()
            }
        }
    }

}