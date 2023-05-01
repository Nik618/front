package com.example.a3d_printing_service_front

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView


class OrderActivity : AppCompatActivity() {

    private var orderTextViewPreview: TextView? = null
    private var orderTextViewFull: TextView? = null
    private var orderImageView: ImageView? = null

    private val rtspUri = Uri.parse("rtsp://feivur.ru:8554/test1")
    private var playerView: StyledPlayerView? = null
    private var player: ExoPlayer? = null

    private var id: Int? = null
    private var description: String? = null
    private var photo: ByteArray? = null
    private var status: String? = null
    private var price: String? = null
    private var track: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)
        playerView = findViewById(R.id.playerView)
        orderTextViewPreview = findViewById(R.id.orderTextViewPreview)
        orderTextViewFull = findViewById(R.id.orderTextViewFull)
        orderImageView = findViewById(R.id.orderImageView)
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()
        val dlc = DefaultLoadControl.Builder().setBufferDurationsMs(100, 100, 100, 100).build()
        player = ExoPlayer.Builder(this).setMediaSourceFactory(DefaultMediaSourceFactory(this).setLiveTargetOffsetMs(0)).setLoadControl(dlc).build()
        val ms = RtspMediaSource.Factory().createMediaSource(MediaItem.fromUri(rtspUri))
        player?.setMediaSource(ms)
        player?.prepare()
        player?.play()
        playerView?.player = player

        id = intent.getIntExtra("id", -1)
        description = intent.getStringExtra("description")
        photo = intent.getByteArrayExtra("photo")
        status = intent.getStringExtra("status")
        price = intent.getStringExtra("price")
        track = intent.getStringExtra("track")

        orderTextViewPreview?.text = "$id | $status\n\n" +
                "Order pending, please wait"
        orderTextViewFull?.text = "$description"
        orderImageView?.setImageBitmap(BitmapFactory.decodeByteArray(photo, 0, photo!!.size))

    }

    override fun onStop() {
        super.onStop()
        player?.stop()
        player?.release()
    }

}