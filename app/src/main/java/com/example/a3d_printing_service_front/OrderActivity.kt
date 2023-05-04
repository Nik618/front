package com.example.a3d_printing_service_front

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class OrderActivity : AppCompatActivity() {

    private var orderTextViewPreview: TextView? = null
    private var orderTextViewFull: TextView? = null
    private var orderImageView: ImageView? = null
    private var webView: WebView? = null
    private val rtspUri = Uri.parse("rtsp://feivur.ru:8554/test1")
    private var playerView: StyledPlayerView? = null
    private var player: ExoPlayer? = null
    private var id: Int? = null
    private var description: String? = null
    private var photo: ByteArray? = null
    private var status: String? = null
    private var price: String? = null
    private var track: String? = null
    private var paymentAddress: String? = null
    private var deepLink: Uri? = null
    private val retrofitCreator = RetrofitCreator()
    private var fabCancel: FloatingActionButton? = null

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
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(
                this
            ) { pendingDynamicLinkData ->
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }
                println("!!!")
                println(deepLink)
            }
            .addOnFailureListener(
                this
            ) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }
    }

    @SuppressLint("SetTextI18n", "SetJavaScriptEnabled")
    override fun onStart() {
        super.onStart()
//
//        if (status == "NEW" || status == "WAITING TO PAYMENT") {
//            fabCancel?.visibility = View.VISIBLE
//        } else fabCancel?.visibility = View.GONE

        if (status == "PRINTING") {
            val dlc = DefaultLoadControl.Builder().setBufferDurationsMs(100, 100, 100, 100).build()
            player = ExoPlayer.Builder(this)
                .setMediaSourceFactory(DefaultMediaSourceFactory(this).setLiveTargetOffsetMs(0))
                .setLoadControl(dlc).build()
            val ms = RtspMediaSource.Factory().createMediaSource(MediaItem.fromUri(rtspUri))
            player?.setMediaSource(ms)
            player?.prepare()
            player?.play()
            playerView?.player = player
        } else playerView?.visibility = View.GONE

        id = intent.getIntExtra("id", -1)
        description = intent.getStringExtra("description")
        photo = intent.getByteArrayExtra("photo")
        status = intent.getStringExtra("status")
        price = intent.getStringExtra("price")
        track = intent.getStringExtra("track")
        paymentAddress = intent.getStringExtra("paymentAddress")

        orderTextViewPreview?.text = "$id | $status\n\n" +
                when (status) {
                    "NEW" -> "Order pending, please wait"
                    "WAITING TO PAYMENT" -> "Pay and wait for confirmation"
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
                    val intent = Intent(this@OrderActivity, MainActivity::class.java)
                    startActivity(intent)
                    alertDialog.setMessage("Order successfully deleted! If order was paid, money will be returned.")
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

}