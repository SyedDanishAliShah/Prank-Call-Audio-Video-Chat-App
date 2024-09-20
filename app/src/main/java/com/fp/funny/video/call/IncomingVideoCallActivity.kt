package com.fp.funny.video.call

import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fp.funny.video.call.serviceclass.FakeScheduledVideoCallService
import com.example.call.RemoteConfig.RemoteConfig.isReturningFromExternalActivity

class IncomingVideoCallActivity : AppCompatActivity() {

    private lateinit var celebrityFullImageInBackground: ImageView
    private lateinit var circleForIncomingVoiceCallCharacter: ImageView
    private lateinit var nameOfCharacter: TextView
    private lateinit var declineIcon: ImageView
    private lateinit var acceptIcon: ImageView
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var selectVideoLauncher: ActivityResultLauncher<Intent>
    private lateinit var videoUrlVideo: String
    private val CHANNEL_ID = "missed_call_channel"
    private val MISSED_CALL_NOTIFICATION_ID = 1001

    private val callTimeoutHandler = Handler(Looper.getMainLooper())  // Handler for managing timeout
    private val callTimeoutRunnable = Runnable {
        // Finish the activity if no action is taken within 20 seconds
        stopRingtone()
        Toast.makeText(this, "Missed call", Toast.LENGTH_SHORT).show()
        showMissedCallNotification()
        finish()  // End the activity
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        setContentView(R.layout.activity_incoming_video_call)

        // Start the 20-second timer
        callTimeoutHandler.postDelayed(callTimeoutRunnable, 20000)


        isReturningFromExternalActivity = true

        createNotificationChannel()
        showMissedCallNotificationService()


        celebrityFullImageInBackground = findViewById(R.id.celebrity_full_image_in_background_incoming_video_call)
        circleForIncomingVoiceCallCharacter = findViewById(R.id.circular_shape_for_prank_call_audio_characters_pics_incoming_video_call)
        nameOfCharacter = findViewById(R.id.name_of_the_character_tv_incoming_video_call)
        declineIcon = findViewById(R.id.decline_icon_incoming_video_call)
        acceptIcon = findViewById(R.id.accept_icon_incoming_video_call)
        val isFromService = intent.getBooleanExtra("IS_FROM_SERVICE", false)
        val isFromPrankVideo = intent.getBooleanExtra("IS_FROM_PRANK_VIDEO", false)
        val videoUrl = intent.getStringExtra("videoUrl")
        val videoUri = intent.getStringExtra("SAVED_VIDEO_URI")
        val nameOfCharacterOfVideoCallPrank = intent.getStringExtra("celebrityName")
        videoUrlVideo = intent.getStringExtra("videoUrl") ?: ""

        // Retrieve and apply the stored theme color
        if (isFromService) {
            val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            val themeColor = sharedPref.getInt("theme_color", Color.TRANSPARENT)
            celebrityFullImageInBackground.setBackgroundColor(themeColor)

        }

        selectVideoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let {
                        val nameOfCharacterOfVideoCall = intent.getStringExtra("celebrityName")

                        val intent = Intent(this, AcceptVideoCallActivity::class.java).apply {
                            putExtra("nameResId", nameOfCharacterOfVideoCall)
                            putExtra("videoUrl", videoUrl)
                            putExtra("SAVED_VIDEO_URI", videoUri)
                            putExtra("IS_FROM_SERVICE", isFromService)
                            putExtra("IS_FROM_PRANK_VIDEO", isFromPrankVideo)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        startActivity(intent)
                        finish()
                    }
                }
            }

        val imageUrlFakeChat = intent.getStringExtra("imageResId")
        if (!imageUrlFakeChat.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrlFakeChat)  // Load the image from the URL or file path
                .placeholder(R.drawable.ic_launcher_background)  // Optional placeholder while loading
                .into(celebrityFullImageInBackground)  // Set the image into the ImageView

            Glide.with(this)
                .load(imageUrlFakeChat)
                .placeholder(R.drawable.ic_launcher_background)
                .override(450, 250)
                .apply(RequestOptions.circleCropTransform())
                .into(circleForIncomingVoiceCallCharacter)
        }

        val imageUrlPrankVideo = intent.getStringExtra("imageUrl")
        if (!imageUrlPrankVideo.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrlPrankVideo)
                .placeholder(R.drawable.ic_launcher_background)
                .into(celebrityFullImageInBackground)

            Glide.with(this)
                .load(imageUrlPrankVideo)
                .placeholder(R.drawable.ic_launcher_background)
                .override(450, 250)
                .apply(RequestOptions.circleCropTransform())
                .into(circleForIncomingVoiceCallCharacter)
        }

        nameOfCharacter.text = nameOfCharacterOfVideoCallPrank

        val nameOfCharacterOfFakeChat = intent.getStringExtra("nameResId")

        val callerName = intent.getStringExtra("CALLER_NAME")

        // Prioritize the "celebrityName" extra if it's available
        nameOfCharacter.text = nameOfCharacterOfVideoCallPrank ?: nameOfCharacterOfFakeChat ?: callerName

        val ringtoneId = intent.getIntExtra("RINGTONE_ID", R.raw.first_ringtone)

        if (intent.getBooleanExtra("IS_FROM_SERVICE", false)) {
            playRingtone(ringtoneId)
        } else {
            playRingtone(ringtoneId)
        }

        val nameOfCharacterOfVoiceCall = intent.getStringExtra("nameResId")
        val imageUrlFakeChatVoice = intent.getStringExtra("imageResId")

        declineIcon.setOnClickListener {
            stopRingtone()  // Stop the ringtone in all cases
            callTimeoutHandler.removeCallbacks(callTimeoutRunnable)

            if (isFromService) {
                // If the screen was triggered by the service, stop the service and close the current activity
                val serviceIntent = Intent(this, FakeScheduledVideoCallService::class.java)
                stopService(serviceIntent)
                finishAffinity()  // Close the current activity and return to the previous one
            }
            else if (intent.getBooleanExtra("IS_FROM_FAKE_CHAT", false)) {
                // If the call was started from FakeChatActivity, navigate back to it
                Toast.makeText(this, "Call declined", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, FakeChatActivity::class.java)  // Navigate to FakeChatActivity
                intent.putExtra("imageResId", imageUrlFakeChat)
                intent.putExtra("nameResId", nameOfCharacterOfVoiceCall )
                startActivity(intent)
                finish()  // Close the current activity
            }
            else {
                // If the call was not from service or FakeChatActivity, just go back to MainActivity
                Toast.makeText(this, "Call declined", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }


        acceptIcon.setOnClickListener {
            stopRingtone()
            callTimeoutHandler.removeCallbacks(callTimeoutRunnable)
            Toast.makeText(this, "Call Accepted", Toast.LENGTH_LONG).show()

            val isFromFakeChat = intent.getBooleanExtra("IS_FROM_FAKE_CHAT", false)  // Retrieve the flag

            val intent = Intent(this, AcceptVideoCallActivity::class.java).apply {
                putExtra("videoUrl", videoUrlVideo)
                putExtra("SAVED_VIDEO_URI", videoUri)
                putExtra("IS_FROM_SERVICE", isFromService)
                putExtra("IS_FROM_FAKE_CHAT", isFromFakeChat)  // Set the flag based on the incoming intent
                putExtra("nameResId", nameOfCharacterOfVoiceCall)
                putExtra("imageResId", imageUrlFakeChatVoice)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
        callTimeoutHandler.removeCallbacks(callTimeoutRunnable)
    }

    private fun playRingtone(resId: Int) {
        // Release the existing MediaPlayer if it's playing
        mediaPlayer?.release()

        // Initialize the MediaPlayer with the ringtone
        mediaPlayer = MediaPlayer.create(this, resId).apply {
            isLooping = false
            setOnCompletionListener {
                playRingtone(resId) // Restart the ringtone when it completes
            }
            start()
        }
    }

    private fun stopRingtone() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        Toast.makeText(this, "Kindly click the decline icon for ending the call", Toast.LENGTH_SHORT).show()

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showMissedCallNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the intent that will open the app when the notification is clicked
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create the notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.app_icon_prank_call_round)  // Use an appropriate icon
            .setContentTitle("Missed Call")
            .setContentText("You had a missed fake video call")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)  // Notification won't be dismissed automatically
            .build()

        // Show the notification
        notificationManager.notify(MISSED_CALL_NOTIFICATION_ID, notification)
    }

    private fun showMissedCallNotificationService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the intent that will open the app when the notification is clicked
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create the notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.app_icon_prank_call_round)  // Use an appropriate icon
            .setContentTitle("Incoming Call")
            .setContentText("Incoming Fake Video Call")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)  // Notification won't be dismissed automatically
            .build()

        // Show the notification
        notificationManager.notify(MISSED_CALL_NOTIFICATION_ID, notification)
    }


}

