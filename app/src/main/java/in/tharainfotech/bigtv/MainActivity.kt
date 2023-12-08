package `in`.tharainfotech.bigtv

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isGone
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var btFullScreen: ImageView
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSource: MediaSource
    private lateinit var fb: FloatingActionButton
    private lateinit var insta: FloatingActionButton
    private lateinit var tw: FloatingActionButton
    private lateinit var wa: FloatingActionButton
    private lateinit var yt: FloatingActionButton
    private var flag: Boolean = false

    private var database = Firebase.database.reference.child("mappdata").child("btv")
    private var videoUrl: String = ""
    private lateinit var fbUrl: String
    private lateinit var instaUrl: String
    private lateinit var twUrl: String
    private lateinit var waUrl: String
    private lateinit var ytUrl: String

    data class StreamData (
        val videoUrl: String = "",
        val fbUrl:String = "",
        val instaUrl:String = "",
        val twUrl:String = "",
        val waUrl:String = "",
        val ytUrl:String = ""
    )


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fetchData()
        loadPlayer()
        // Assign variables

        btFullScreen = findViewById(R.id.bt_fullscreen)
        fb = findViewById(R.id.fb)
        insta = findViewById(R.id.insta)
        wa = findViewById(R.id.wa)
        tw = findViewById(R.id.tw)
        yt = findViewById(R.id.yt)


        //Make activity fullscreen
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        btFullScreen.setOnClickListener {
            if (flag) {
                btFullScreen.setImageDrawable(getDrawable(R.drawable.ic_fullscreen))
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                window.decorView.apply {
                    systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                }
                var ele: View = findViewById(R.id.root_el)
                ele.setBackgroundColor(Color.WHITE)

                var links:View = findViewById(R.id.links)
                links.isGone = false
                flag = false
            } else {
                btFullScreen.setImageDrawable(getDrawable(R.drawable.ic_fullscreen_exit))
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                window.decorView.apply {
                    systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // Hide the nav bar and status bar
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN )
                }
                var ele: View = findViewById(R.id.root_el)
                ele.setBackgroundColor(Color.BLACK)

                var links:View = findViewById(R.id.links)
                links.isGone = true
                flag = true
            }
        }
        wa.setOnClickListener() {
            val url: String = waUrl
            val i: Intent = Intent(Intent.ACTION_VIEW)
            i.setData(Uri.parse(url))
            startActivity(i)
        }

        fb.setOnClickListener() {
            val url: String = fbUrl
            val i: Intent = Intent(Intent.ACTION_VIEW)
            i.setData(Uri.parse(url))
            startActivity(i)
        }

        yt.setOnClickListener() {
            val url: String = ytUrl
            val i: Intent = Intent(Intent.ACTION_VIEW)
            i.setData(Uri.parse(url))
            startActivity(i)
        }

        tw.setOnClickListener() {
            val url: String = twUrl
            val i: Intent = Intent(Intent.ACTION_VIEW)
            i.setData(Uri.parse(url))
            startActivity(i)
        }

        insta.setOnClickListener() {
            val url: String = instaUrl
            val i: Intent = Intent(Intent.ACTION_VIEW)
            i.setData(Uri.parse(url))
            startActivity(i)
        }

    }


    private var playerListener = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Toast.makeText(this@MainActivity, "${error.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        exoPlayer.playWhenReady = true
        exoPlayer.play()
    }
    override fun onPause() {
        super.onPause()

        exoPlayer.pause()
        exoPlayer.playWhenReady = false
    }

    override fun onStop() {
        super.onStop()
        exoPlayer.pause()
        exoPlayer.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()

        exoPlayer.stop()
        exoPlayer.clearMediaItems()

    }

    private fun loadPlayer() {
        playerView = findViewById(R.id.player_view)
        //video url
        val videoUrl: Uri = Uri.parse(videoUrl)
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory (
            this,
            Util.getUserAgent(this, applicationInfo.name)
        )
        mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(
            MediaItem.fromUri(videoUrl)
        )
        //Initialize load control
        var loadControl: LoadControl = DefaultLoadControl()

        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer.addListener(playerListener)

        playerView.player = exoPlayer

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()

        exoPlayer.playWhenReady = true
        exoPlayer.play()
    }

    private fun fetchData() {
        Log.d("error---------------", "dat empty")
        database.get()
            .addOnSuccessListener { DataSnapshot ->
                if (DataSnapshot.exists()) {
                    val dat = DataSnapshot.getValue(StreamData::class.java)
                    Log.d("error---------------", dat.toString())
                    if (dat != null) {
                        videoUrl = dat.videoUrl
                        fbUrl = dat.fbUrl
                        instaUrl = dat.instaUrl
                        twUrl = dat.twUrl
                        waUrl = dat.waUrl
                        ytUrl = dat.ytUrl
                        loadPlayer()
                    } else {
                        Log.d("error---------------", "dat empty")
                    }
                } else {
                    Log.d("error---------------", "dat empty")
                }
            }
            .addOnFailureListener { it ->
                Log.d("error---------------", it.toString())
            }
    }
}

