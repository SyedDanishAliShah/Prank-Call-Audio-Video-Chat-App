package com.fp.funny.video.call

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class IncomingPrankCallActivityPrankAudioCall : AppCompatActivity() {

    private lateinit var celebrityFullImageInBackground : ImageView
    private lateinit var circleForIncomingVoiceCallCharacter : ImageView
    private lateinit var nameOfCharacter : TextView
    private lateinit var declineIcon : ImageView
    private lateinit var acceptIcon : ImageView
    private var mediaPlayer: MediaPlayer? = null
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
        setContentView(R.layout.activity_incoming_voice_call)

        // Start the 20-second timer
        callTimeoutHandler.postDelayed(callTimeoutRunnable, 20000)

        createNotificationChannel()
        showMissedCallNotificationService()

        celebrityFullImageInBackground = findViewById(R.id.celebrity_full_image_in_background)
        circleForIncomingVoiceCallCharacter = findViewById(R.id.circular_shape_for_prank_call_audio_characters_pics)
        nameOfCharacter = findViewById(R.id.name_of_the_character_tv)
        declineIcon = findViewById(R.id.decline_icon)
        acceptIcon = findViewById(R.id.accept_icon)

        // Retrieve data from the Intent
        val characterPicResId = intent.getStringExtra("characterPicResId")
        val characterNameText = intent.getStringExtra("characterName")

        if(!characterPicResId.isNullOrEmpty()){
            Glide.with(this)
                .load(characterPicResId)
                .placeholder(R.drawable.ic_launcher_background)
                .into(celebrityFullImageInBackground)

            Glide.with(this)
                .load(characterPicResId)
                .placeholder(R.drawable.ic_launcher_background)
                .override(450, 250)
                .apply(RequestOptions.circleCropTransform())
                .into(circleForIncomingVoiceCallCharacter)
        }


        if (characterNameText != null){
            nameOfCharacter.text = characterNameText
        }

        val ringtoneId = intent.getIntExtra("RINGTONE_ID", R.raw.first_ringtone) // Default ringtone if none provided

        // Play the default ringtone
        playRingtone(ringtoneId)

        declineIcon.setOnClickListener {
            stopRingtone()
            callTimeoutHandler.removeCallbacks(callTimeoutRunnable)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        acceptIcon.setOnClickListener {
            val intent = Intent(this, AcceptCallActivity::class.java)
            intent.putExtra("characterPicResId", characterPicResId)
                .putExtra("characterName", characterNameText)
            startActivity(intent)
            finish()
            callTimeoutHandler.removeCallbacks(callTimeoutRunnable)
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
            .setContentText("You had a missed fake voice call")
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
            .setContentText("Incoming Fake Voice Call")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)  // Notification won't be dismissed automatically
            .build()

        // Show the notification
        notificationManager.notify(MISSED_CALL_NOTIFICATION_ID, notification)
    }
}