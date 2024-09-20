package com.fp.funny.video.call

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.fp.funny.video.call.serviceclass.FakeScheduledVideoCallService
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import java.io.File

class AcceptVideoCallActivity : AppCompatActivity() {

    private lateinit var celebrityBackgroundVideoAcceptCall: PlayerView
    private lateinit var timerTv: TextView
    private lateinit var videoCallSelfVideoContainer: PreviewView
    private lateinit var switchCameraIcon: ImageView
    private lateinit var declineIcon: ImageView
    private lateinit var videoCameraIcon: ImageView
    private lateinit var micIcon: ImageView

    private var cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    private var cameraProvider: ProcessCameraProvider? = null

    private var handler: Handler = Handler(Looper.getMainLooper())
    private var secondsElapsed = 0
    private var isRunning = false
    private lateinit var runnable: Runnable

    private val REQUEST_CODE_PERMISSIONS = 10
    private val REQUIRED_PERMISSIONS =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

    private var isCameraOn = true // Add this flag to keep track of the camera preview state
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var videoCache: Cache

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accept_video_call)

        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)


        exoPlayer = ExoPlayer.Builder(this).build()
        val playerView = findViewById<PlayerView>(R.id.character_video_incoming_video_call)
        playerView.player = exoPlayer

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    exoPlayer.seekTo(0) // Restart the video
                    exoPlayer.playWhenReady = true
                }
            }
        })

        // Set up caching
        setupCache()

        timerTv = findViewById(R.id.timer_text_tv_accept_video_call)
        videoCallSelfVideoContainer = findViewById(R.id.container_for_front_and_back_cam_for_video_call_screen)
        switchCameraIcon = findViewById(R.id.switch_camera_icon_accept_video_call)
        declineIcon = findViewById(R.id.decline_icon_accept_video_call)
        videoCameraIcon = findViewById(R.id.camera_icon_accept_video_call)
        micIcon = findViewById(R.id.mic_image_accept_video_call)
        val isFromService = intent.getBooleanExtra("IS_FROM_SERVICE", false)
        val celebrityFullImage = intent.getStringExtra("imageResId")
        val nameOfCharacterOfVoiceCall = intent.getStringExtra("nameResId")


        switchCameraIcon.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }
            startCamera()
        }

        videoCameraIcon.setOnClickListener {
            if (isCameraOn) {
                // Turn off the camera preview
                videoCallSelfVideoContainer.visibility = View.VISIBLE
                isCameraOn = false
            } else {
                // Turn on the camera preview
                videoCallSelfVideoContainer.visibility = View.INVISIBLE
                startCamera()
                isCameraOn = true
            }
        }


        declineIcon.setOnClickListener {
            if (isFromService){
                stopTimer()
                // If the screen was triggered by the service, stop the service and close the app
                val serviceIntent = Intent(this, FakeScheduledVideoCallService::class.java)
                stopService(serviceIntent)
                finishAffinity()
            }
            else if (intent.getBooleanExtra("IS_FROM_FAKE_CHAT", false)) {
                // If the call was started from FakeChatActivity, navigate back to it
                val intent = Intent(this, FakeChatActivity::class.java)  // Navigate to FakeChatActivity
                intent.putExtra("imageResId", celebrityFullImage)
                intent.putExtra("nameResId", nameOfCharacterOfVoiceCall )
                startActivity(intent)
                finish()  // Close the current activity
            }

            else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        runnable = object : Runnable {
            override fun run() {
                if (isRunning) {
                    secondsElapsed++
                    updateTimerTextView()
                    handler.postDelayed(this, 1000)
                }
            }
        }

        startTimer()
        handleVideoPlayback()

        // Get the video URI from the intent and play it
        val video = intent.getStringExtra("VIDEO_URI")
        video?.let {
            val videoUri = Uri.parse(it)
            grantUriPermission(packageName, videoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val mediaItem = MediaItem.fromUri(videoUri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true

        }


    }

    @SuppressLint("WrongConstant")
    private fun handleVideoPlayback() {
        if (!isInternetConnected()) {
            // Show a toast if the internet is off
            Toast.makeText(this, "Kindly check your internet connection", Toast.LENGTH_SHORT).show()
            return
        }

        val videoUriString = intent.getStringExtra("SAVED_VIDEO_URI")
        val fromService = intent.getBooleanExtra("IS_FROM_SERVICE", false)

        if (fromService && videoUriString != null) {
            val videoUri: Uri = if (videoUriString.startsWith("content:")) {
                Uri.parse(videoUriString)
            } else {
                val videoFile = File(videoUriString)
                FileProvider.getUriForFile(this, "${packageName}.fileprovider", videoFile)
            }

            val mediaItem = MediaItem.fromUri(videoUri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true

            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        exoPlayer.seekTo(0)
                        exoPlayer.playWhenReady = true
                    }
                }
            })
        } else {
            // Handle cases where the video is not from a service or there is no URI provided
        }
    }

    private fun isInternetConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }


    @SuppressLint("CutPasteId")
    private fun setupCache() {
        val app = application as MyApplication
        videoCache = app.videoCache

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(videoCache)
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
            .setCacheReadDataSourceFactory(DefaultDataSource.Factory(this))
        celebrityBackgroundVideoAcceptCall = findViewById(R.id.character_video_incoming_video_call)

        val videoUrl = intent.getStringExtra("videoUrl")

        // Use the videoUrl to play the video in a VideoView or other video player
        val videoView: PlayerView = findViewById(R.id.character_video_incoming_video_call)
        if (videoUrl != null) {
            val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
            val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(mediaItem)
            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }


    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(videoCallSelfVideoContainer.surfaceProvider)
        }

        try {
            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(
                this, cameraSelector, preview
            )
        } catch (exc: Exception) {
            // Handle exception
        }
    }

    private fun startTimer() {
        isRunning = true
        handler.post(runnable)
    }

    private fun stopTimer() {
        isRunning = false
        handler.removeCallbacks(runnable)
    }



    @SuppressLint("DefaultLocale")
    private fun updateTimerTextView() {
        val minutes = (secondsElapsed / 60)
        val seconds = (secondsElapsed % 60)
        val formattedTime = String.format("%02d:%02d", minutes, seconds)
        timerTv.text = formattedTime
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        exoPlayer.release()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {

                startCamera()
            }
            }
        }


    @SuppressLint("MissingSuperCall")
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
            Toast.makeText(this, "Kindly click the decline icon for ending the call", Toast.LENGTH_SHORT).show()

    }


}




