package com.fp.funny.video.call

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity

class VideoPickerActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_VIDEO_PICK = 1
        var videoURL = ""
    }

    @SuppressLint("IntentReset")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "video/*"
        startActivityForResult(intent, REQUEST_VIDEO_PICK)
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VIDEO_PICK && resultCode == Activity.RESULT_OK) {
            val videoUri = data?.data
            if (videoUri != null) {
                // Save the selected video URI in SharedPreferences
                videoURL = videoUri.toString()
                saveVideoUri(videoUri.toString())
            }
        }

        // Finish the activity after handling the result
        finish()
    }

    private fun saveVideoUri(uri: String) {
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("SAVED_VIDEO_URI", uri).apply()
    }

}