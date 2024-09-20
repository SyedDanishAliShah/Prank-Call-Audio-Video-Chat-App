package com.fp.funny.video.call

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fp.funny.video.call.serviceclass.FakeScheduledCallService

class AcceptCallActivity : AppCompatActivity() {

    private lateinit var celebrityBackgroundFullImageAcceptCall: ImageView
    private lateinit var celebrityRoundedImageAcceptCall: ImageView
    private lateinit var declineIcon: ImageView
    private lateinit var nameOfTheCharacterTv: TextView
    private lateinit var callEndedTv: TextView
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var secondsElapsed = 0
    private var isRunning = false
    private lateinit var runnable: Runnable
    private lateinit var timerTv: TextView
    private lateinit var declineTv : TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accept_audio_call)

        celebrityBackgroundFullImageAcceptCall =
            findViewById(R.id.celebrity_full_image_in_background_accept_call)
        declineIcon = findViewById(R.id.decline_icon_accept_call)
        nameOfTheCharacterTv = findViewById(R.id.name_of_the_character_tv_accept_call)
        callEndedTv = findViewById(R.id.call_ended_tv)
        timerTv = findViewById(R.id.timer_text_tv)
        declineTv = findViewById(R.id.decline_tv_accept_call)
        celebrityRoundedImageAcceptCall = findViewById(R.id.celebrity_image_in_small_rounded_form)

        val celebrityFullImage = intent.getStringExtra("imageResId")
        val characterPicResId = intent.getStringExtra("characterPicResId")
        val nameOfCharacterOfVoiceCall = intent.getStringExtra("nameResId")

        if (characterPicResId != null) {
            Glide.with(this)
                .load(characterPicResId)  // Load the image from the URL or file path
                .placeholder(R.drawable.ic_launcher_background)  // Optional placeholder while loading
                .into(celebrityBackgroundFullImageAcceptCall)  // Set the image into the ImageView

            Glide.with(this)
                .load(characterPicResId)
                .placeholder(R.drawable.ic_launcher_background)
                .apply(RequestOptions.circleCropTransform()) // Applies the circle crop transformation
                .into(celebrityRoundedImageAcceptCall)
        }


        if (celebrityFullImage != null) {
            Glide.with(this)
                .load(celebrityFullImage)  // Load the image from the URL or file path
                .placeholder(R.drawable.ic_launcher_background)  // Optional placeholder while loading
                .into(celebrityBackgroundFullImageAcceptCall)  // Set the image into the ImageView

            Glide.with(this)
                .load(celebrityFullImage)
                .placeholder(R.drawable.ic_launcher_background)
                .apply(RequestOptions.circleCropTransform()) // Applies the circle crop transformation
                .into(celebrityRoundedImageAcceptCall)
        }


        val nameOfTheCharacter = intent.getStringExtra("nameResId")

        if (nameOfTheCharacter != null) {

            nameOfTheCharacterTv.text = nameOfTheCharacter
        }

        val nameOfCharacterOfVideoCall = intent.getStringExtra("celebrityName")

        if (nameOfCharacterOfVideoCall != null) {
            nameOfTheCharacterTv.text = nameOfCharacterOfVideoCall
        }

        val isFromService = intent.getBooleanExtra("IS_FROM_SERVICE", false)


        // Retrieve and apply the stored theme color
        if (isFromService) {
            val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            val themeColor = sharedPref.getInt("theme_color", Color.TRANSPARENT)
            celebrityBackgroundFullImageAcceptCall.setBackgroundColor(themeColor)
            celebrityRoundedImageAcceptCall.visibility = View.INVISIBLE
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

        // Start the timer
        startTimer()

        declineIcon.setOnClickListener {
            if (isFromService) {
                stopTimer()
                // If the screen was triggered by the service, stop the service and close the app
                val serviceIntent = Intent(this, FakeScheduledCallService::class.java)
                stopService(serviceIntent)
                finish()
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
                callEndedTv.visibility = TextView.VISIBLE
                declineIcon.visibility = Button.GONE
                declineTv.visibility = TextView.GONE
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
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
            // Stop the timer if the activity is destroyed
            stopTimer()
        }

    @SuppressLint("MissingSuperCall")
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
            Toast.makeText(this, "Kindly click the decline icon for ending the call", Toast.LENGTH_SHORT).show()
        }
}

