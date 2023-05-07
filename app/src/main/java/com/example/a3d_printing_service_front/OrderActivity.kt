package com.example.a3d_printing_service_front

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.a3d_printing_service_front.pojo.OrderPojo
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
    private val retrofitSender = RetrofitSender()
    private var fabCancel: FloatingActionButton? = null
    private var layoutTrack: LinearLayout? = null
    private var trackView: WebView? = null

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
        layoutTrack = findViewById(R.id.layoutTrack)
        trackView = findViewById(R.id.track)
        trackView!!.settings.javaScriptEnabled = true

        alertDialog = AlertDialog.Builder(this@OrderActivity).create()
        progressDialog = ProgressDialog(this, R.style.MyTheme)
        progressDialog.setCancelable(false)
        progressDialog.setProgressStyle(com.google.android.material.R.style.Base_Widget_AppCompat_ProgressBar)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "SetJavaScriptEnabled")
    override fun onStart() {
        super.onStart()
        retrofitSender.refreshTokens()
        progressDialog.show()
        id = intent.getIntExtra("id", -1)
        description = intent.getStringExtra("description")
        photo = intent.getByteArrayExtra("photo")
        price = intent.getStringExtra("price")
        track = intent.getStringExtra("track")
        status = intent.getStringExtra("status")
        paymentAddress = intent.getStringExtra("paymentAddress")
        CoroutineScope(Dispatchers.IO).launch {
            val thisOrderStatus = retrofitSender.getStatus(id!!)?.result
            withContext(Dispatchers.Main) {
                status = thisOrderStatus
                progressDialog.dismiss()

                if (status == "NEW" || status == "WAITING TO PAYMENT") {
                    fabCancel?.visibility = View.VISIBLE
                }

                if (status == "PRINTING") {
                    playerView?.visibility = View.VISIBLE
                    val dlc = DefaultLoadControl.Builder().setBufferDurationsMs(100, 100, 100, 100).build()
                    player = ExoPlayer.Builder(this@OrderActivity)
                        .setMediaSourceFactory(DefaultMediaSourceFactory(this@OrderActivity).setLiveTargetOffsetMs(0))
                        .setLoadControl(dlc).build()
                    retrofitSender.refreshTokens()
                    CoroutineScope(Dispatchers.IO).launch {
                        println("\n\n\n$id\n\n")
                        val response = retrofitSender.getVideo(id!!)
                        withContext(Dispatchers.Main) {
                            val ms = RtspMediaSource.Factory().createMediaSource(MediaItem.fromUri(response?.result!!))
                            player?.setMediaSource(ms)
                            player?.prepare()
                            player?.play()
                            playerView?.player = player
                        }
                    }
                } else playerView?.visibility = View.GONE

                if (status == "IN DELIVERY") {
                    progressDialog.show()
                    CoroutineScope(Dispatchers.IO).launch {
                        val trackResponse = retrofitSender.getTrack(id!!)

                        withContext(Dispatchers.Main) {
                            if (trackResponse?.status == "success") {
                                trackView!!.loadUrl("https://645761c30eaef4382032820b--harmonious-genie-4f1c9e.netlify.app?${trackResponse.result}")
                                progressDialog.dismiss()
                            }
                        }
                    }
                    orderTextViewFull?.visibility = View.GONE
                    layoutTrack?.visibility = View.VISIBLE
                }

                if (status == "DONE") {
                    layoutTrack?.visibility = View.GONE
                }

                orderTextViewPreview?.text = "№$id | " +
                        when (status) {
                            "NEW" -> "НОВЫЙ\n" +
                                    "\nАдминистратор должен ознакомиться с заказом и назначить цену - пожалуйста, подождите\n"
                            "WAITING TO PAYMENT" -> "ОЖИДАЕТ ПОСТУПЛЕНИЯ СРЕДСТВ\n" +
                                    "\nВнесите оплату - печать начнётся после подтверждения оплаты администратором"
                            "PRINTING" -> "ПЕЧАТАЕТСЯ\n" +
                                    "\nВаш заказ печатается - ниже вы можете наблюдать за процессом печати"
                            "PREPARE TO DELIVERY" -> "ГОТОВИТСЯ К ДОСТАВКЕ\n" +
                                    "\nВаш заказ напечатан и в данный момент передаётся в доставку"
                            "IN DELIVERY" -> "В ПУТИ\n" +
                                    "\nВаш заказ в пути, его можно отследить по трек-номеру ниже. Не забудьте подтвердить получение"
                            "DONE" -> "ПОЛУЧЕН"
                            else -> {}
                        }
                orderTextViewFull?.text = "Описание заказа:\n\n$description"

                if (photo?.size != 0)
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
                    webView?.visibility = View.VISIBLE
                } else webView?.visibility = View.GONE
            }
        }


    }

    override fun onStop() {
        super.onStop()
        player?.stop()
        player?.release()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toDelOrder(view: View) {

        val builder = AlertDialog.Builder(this)

        builder.setTitle("Подтверждение")
        builder.setMessage("Вы действительно хотите удалить заказ? Если вы уже провели оплату, средства будут возвращены")

        builder.setPositiveButton("YES") { dialog, _ ->
            CoroutineScope(Dispatchers.IO).launch {
                retrofitSender.delOrder(id!!)
                withContext(Dispatchers.Main) {
                    Storage.refreshOrdersFlag = true
                    val intent = Intent(this@OrderActivity, MainActivity::class.java)
                    startActivity(intent)
                    alertDialog.setMessage("Заказ был удалён. Если вы уже провели оплату, средства будут возвращены")
                    alertDialog.show()
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun onClickApprove(view: View) {
        progressDialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            val response = retrofitSender.approveReceiving(OrderPojo(id = id))
            withContext(Dispatchers.Main) {
                if (response?.status == "success") {
                    alertDialog.setMessage("Подтверждение получения заказа отправлено администратору. Спасибо за заказ!")
                    alertDialog.show()
                    progressDialog.dismiss()
                    retrofitSender.refreshTokens()
                    CoroutineScope(Dispatchers.IO).launch {
                        val thisOrder = retrofitSender.getOrder(id!!)
                        withContext(Dispatchers.Main) {
                            Storage.refreshOrdersFlag = true
                            status = thisOrder?.status
                            progressDialog.dismiss()
                        }
                    }
                } else {
                    alertDialog.setMessage("Что-то пошло не так. Пожалуйста, попробуйте позже")
                    alertDialog.show()
                }
                onStart()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toRefresh(view: View) {
        progressDialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            val thisOrder = retrofitSender.getOrder(id!!)
            withContext(Dispatchers.Main) {
                if (status != thisOrder?.status) Storage.refreshOrdersFlag = true
                status = thisOrder?.status
                price = thisOrder?.price
                track = thisOrder?.track
                paymentAddress = thisOrder?.paymentAddress

                onStart()
            }
        }
    }

    fun showImage(view: View) {
        val intent = Intent(this@OrderActivity, ImageActivity::class.java)
        intent.putExtra("order_id", id)
        startActivity(intent)
    }

}