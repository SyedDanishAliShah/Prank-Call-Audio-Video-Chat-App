package com.fp.funny.video.call

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.call.RemoteConfig.RemoteConfig
import com.example.call.RemoteConfig.RemoteConfig.isReturningFromExternalActivity
import com.example.call.RemoteConfig.RemoteConfig.isSplash
import com.fp.funny.video.call.adapters.PrankChatAdapter
import com.fp.funny.video.call.adapters.PrankVideoAdapter
import com.fp.funny.video.call.dataclasses.FakeCallItem
import com.fp.funny.video.call.dataclasses.PrankCallHistory
import com.fp.funny.video.call.in_app_purchase.PremiumActivity
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), PrankVideoAdapter.OnBackPressListener {

    private lateinit var scheduleACallCard: ImageView
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 102
    private lateinit var permissionDeniedDialog: AlertDialog
    private lateinit var premiumIcon : ImageView

    @SuppressLint("InflateParams")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the permission denied dialog
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_permissions_settings, null)
        permissionDeniedDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()


        val goToSettingsButton : ImageView = dialogView.findViewById(R.id.go_to_settings_button)
        goToSettingsButton.setOnClickListener {
            openAppNotificationSettings()
        }

        // Check for internet connection
        if (!isConnectedToInternet()) {
            showNoInternetDialog()
        }


        isSplash = false

        isReturningFromExternalActivity = true

    if (!isPrivacyPolicyAccepted()) {

    showDialogsAndCheckInternet()
}

        // Load cached data
        loadCachedData()

    if(isInternetAvailable()) {
    // Fetch fresh data from the API
    fetchAndCacheData()

    fetchAudioCallData()
}

    }

    private fun isPrivacyPolicyAccepted(): Boolean {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("privacy_policy_accepted", false)
    }

    private fun setPrivacyPolicyAccepted() {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("privacy_policy_accepted", true).apply()
    }

    private fun loadCachedData() {
        val sharedPreferences = getSharedPreferences("prank_data", Context.MODE_PRIVATE)

        // Load Video Chat data
        val videoChatDataJson = sharedPreferences.getString("video_chat_data", "")
        val videoChatData: List<FakeCallItem> = if (videoChatDataJson!!.isNotEmpty()) {
            Gson().fromJson(videoChatDataJson, object : TypeToken<List<FakeCallItem>>() {}.type)
        } else {
            emptyList()
        }

        // Load Audio Chat data
        val audioChatDataJson = sharedPreferences.getString("audio_chat_data", "")
        val audioChatData: List<FakeCallItem> = if (audioChatDataJson!!.isNotEmpty()) {
            Gson().fromJson(audioChatDataJson, object : TypeToken<List<FakeCallItem>>() {}.type)
        } else {
            emptyList()
        }

        // Load Text Chat data
        val textChatDataJson = sharedPreferences.getString("text_chat_data", "")
        val textChatData: List<FakeCallItem> = if (textChatDataJson!!.isNotEmpty()) {
            Gson().fromJson(textChatDataJson, object : TypeToken<List<FakeCallItem>>() {}.type)
        } else {
            emptyList()
        }

        // Update UI with cached data
        updateUI(videoChatData, textChatData, audioChatData)
    }

    private fun fetchAndCacheData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.prankVideoItems.getPrankVideoData()

                if (response.isSuccessful) {
                    val categories = response.body() ?: emptyList()

                    // Separate data by category
                    val videoChatData =
                        categories.find { it.FakeCallData.name == "Video Chat" }?.data.orEmpty()
                    val audioChatData =
                        categories.find { it.FakeCallData.name == "Audi Chat" }?.data.orEmpty()
                    val textChatData =
                        categories.find { it.FakeCallData.name == "Text Chat" }?.data.orEmpty()

                    // Cache the data in SharedPreferences
                    val sharedPreferences =
                        getSharedPreferences("prank_data", Context.MODE_PRIVATE)
                    sharedPreferences.edit().apply {
                        putString("video_chat_data", Gson().toJson(videoChatData))
                        putString("audio_chat_data", Gson().toJson(audioChatData))
                        putString("text_chat_data", Gson().toJson(textChatData))
                        apply()
                    }

                    // Update UI on the main thread
                    withContext(Dispatchers.Main) {
                        updateUI(videoChatData, textChatData, audioChatData)
                    }
                } else {
                    Log.e("MainActivity", "Error: ${response.message()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun updateUI(videoChatData: List<FakeCallItem>, textChatData: List<FakeCallItem>, audioCallData: List<FakeCallItem>) {
        val container = findViewById<LinearLayout>(R.id.root_view_wrapper)

        // If you must remove all views, you should re-find the RecyclerViews after removal
        container.removeAllViews()

        // Ensure you have a reference to the correct layout and view IDs
        val newLayout = LayoutInflater.from(this).inflate(R.layout.activity_main, container, false)
        container.addView(newLayout)

        scheduleACallCard = findViewById(R.id.schedule_a_call_card)
        scheduleACallCard.setOnClickListener {
            val intent = Intent(this, ScheduleACallActivity::class.java)
            startActivity(intent)
            finish()
        }

        premiumIcon = findViewById(R.id.premium_icon)

        premiumIcon.setOnClickListener {
            val intent = Intent(this, PremiumActivity::class.java)
            startActivity(intent)
            finish()
        }

        val recyclerViewPrankChat: RecyclerView = newLayout.findViewById(R.id.rv_prank_chat)
        recyclerViewPrankChat.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapterTextChat = PrankChatAdapter(textChatData)
        recyclerViewPrankChat.adapter = adapterTextChat

        val recyclerViewPrankVideo: RecyclerView = newLayout.findViewById(R.id.rv_prank_video)
        recyclerViewPrankVideo.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapterVideoChat = PrankVideoAdapter(videoChatData, this, this)
        recyclerViewPrankVideo.adapter = adapterVideoChat

        // Continue with other view updates
        audioCallData.forEach { item ->
            val view = layoutInflater.inflate(R.layout.items_audio_call, container, false)

            val imageView = view.findViewById<ImageView>(R.id.circle_of_character_of_audio_call_one)
            val textView = view.findViewById<TextView>(R.id.name_of_character_audio_call_one)
            val rectangleForPrankAudioCall = view.findViewById<ImageView>(R.id.rectangle_for_prank_audio_call_one)

            textView.text = item.name

            Glide.with(this@MainActivity)
                .load(item.image_url)
                .placeholder(R.drawable.rectangle_for_prank_audio_call)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView)

            container.addView(view)

            rectangleForPrankAudioCall.setOnClickListener {
                val characterPicResId = item.image_url
                val characterName = item.name

                val intent = Intent(this@MainActivity, IncomingPrankCallActivityPrankAudioCall::class.java)
                intent.putExtra("characterPicResId", characterPicResId)
                intent.putExtra("characterName", characterName)

                val prankCallHistory = PrankCallHistory(
                    imageResId = characterPicResId,
                    celebrityName = characterName,
                    callType = "audio"
                )

                GlobalScope.launch(Dispatchers.IO) {
                    (applicationContext as MyApplication).database.prankCallHistoryDao()
                        .insert(prankCallHistory)
                    withContext(Dispatchers.Main) {
                        startActivity(intent)
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // Show dialog if permission was denied
                        // Dismiss the dialog if notification permission is granted
                        permissionDeniedDialog.show()
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        // Inflate the dialog layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.exit_dialog, null)

        // Create the dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Find and set up the views in the dialog
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok)

        btnCancel.setOnClickListener {
            dialog.dismiss() // Dismiss the dialog
        }

        btnOk.setOnClickListener {
            // Close the app
            finishAffinity() // Close the app completely
        }

        // Show the dialog
        dialog.show()
    }
    private fun openLink(url: String?) {
        if (!url.isNullOrEmpty()) {
            var fixedUrl = url
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                fixedUrl = "http://$url" // Add a default scheme if missing
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fixedUrl))
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "No application can handle this request.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "URL is empty!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPrivacyPolicyDialog(onComplete: () -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_privacy_policy, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val checkBox = dialogView.findViewById<ImageView>(R.id.check_box_privacy_policy_dialog)
        val continueButton = dialogView.findViewById<ImageView>(R.id.privacy_policy_dialogue_button_rectangle)
        val tvPrivacy = dialogView.findViewById<TextView>(R.id.i_have_read_and_accept_privacy_policy_tv_privacy_policy_dialog)

        var isCheckBoxSelected = false
        var isContinueButtonSelected = false

        tvPrivacy.setOnClickListener {

            isReturningFromExternalActivity = true
            openLink(RemoteConfig.privacypolicylink)
        }

        checkBox.setOnClickListener {
            isCheckBoxSelected = !isCheckBoxSelected
            checkBox.setImageResource(
                if (isCheckBoxSelected)
                    R.drawable.checked_icon_privacy_policy_dialogue
                else
                    R.drawable.check_box_privacy_policy_dialog
            )
            continueButton.setImageResource(
                if (isCheckBoxSelected)
                    R.drawable.privacy_privacy_dialogue_selected_rectangle
                else
                    R.drawable.privacy_policy_dialogue_button_rectangle
            )
        }

        continueButton.setOnClickListener {
            if (isCheckBoxSelected) {
                isContinueButtonSelected = true
                if (isCheckBoxSelected && isContinueButtonSelected) {
                    setPrivacyPolicyAccepted() // Update the flag
                    dialog.dismiss()
                    onComplete()
                }
            } else {
                Toast.makeText(this, "Please accept the privacy policy to continue.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun fetchAudioCallData() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Retrieve cached data from SharedPreferences
            val sharedPreferences = getSharedPreferences("prank_data", Context.MODE_PRIVATE)
            val cachedAudioChatDataJson = sharedPreferences.getString("audio_chat_data", "")
            val cachedAudioCallData: List<FakeCallItem> = if (cachedAudioChatDataJson!!.isNotEmpty()) {
                Gson().fromJson(cachedAudioChatDataJson, object : TypeToken<List<FakeCallItem>>() {}.type)
            } else {
                emptyList()
            }

            // Retrieve cached data for video chat and text chat as well
            val cachedVideoChatDataJson = sharedPreferences.getString("video_chat_data", "")
            val cachedVideoChatData: List<FakeCallItem> = if (cachedVideoChatDataJson!!.isNotEmpty()) {
                Gson().fromJson(cachedVideoChatDataJson, object : TypeToken<List<FakeCallItem>>() {}.type)
            } else {
                emptyList()
            }

            val cachedTextChatDataJson = sharedPreferences.getString("text_chat_data", "")
            val cachedTextChatData: List<FakeCallItem> = if (cachedTextChatDataJson!!.isNotEmpty()) {
                Gson().fromJson(cachedTextChatDataJson, object : TypeToken<List<FakeCallItem>>() {}.type)
            } else {
                emptyList()
            }

            // If cached data exists, update the UI immediately
            if (cachedAudioCallData.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    updateUI(cachedVideoChatData, cachedTextChatData, cachedAudioCallData)
                }
            }

            try {
                // Fetch fresh data from the API
                val response = RetrofitClient.prankVideoItems.getPrankVideoData()

                if (response.isSuccessful) {
                    val categories = response.body() ?: emptyList()

                    // Find the "Audio Call" category
                    val audioCallData = categories.find { it.FakeCallData.name == "Audi Chat" }?.data.orEmpty()

                    // Cache the fresh data in SharedPreferences
                    sharedPreferences.edit().apply {
                        putString("audio_chat_data", Gson().toJson(audioCallData))
                        apply()
                    }

                    // Update the UI with fresh data
                    withContext(Dispatchers.Main) {
                        updateUI(cachedVideoChatData, cachedTextChatData, audioCallData)
                    }
                } else {
                    Log.e("AudioCallActivity", "Error: ${response.message()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun requestNotificationPermission(onComplete: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
            onComplete()
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    // Call this function to show dialogs and check internet
    private fun showDialogsAndCheckInternet() {
        // First show the notification permission dialog
        requestNotificationPermission {
            // Once notification dialog is interacted with, show privacy policy dialog
            showPrivacyPolicyDialog {
                // After both dialogs are interacted with, check internet and show toast
                //checkInternetAndShowToast()
            }
        }
    }

    /*private fun checkInternetAndShowToast() {
        if (!isInternetAvailable()) {
            Toast.makeText(this, "Data cannot be fetched. Kindly check your internet connection", Toast.LENGTH_LONG).show()
        }
    }*/

    @SuppressLint("InflateParams")
    override fun onBackPressHandled() {
        // Create and configure the dialog
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Check for internet connection
    private fun isConnectedToInternet(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    // Show the non-cancelable dialog if no internet connection
    @SuppressLint("InflateParams")
    private fun showNoInternetDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_no_internet_permission)
        dialog.setCancelable(false)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tryAgainButton : ImageView = dialog.findViewById(R.id.try_again_button_no_internet_connection_dialog)
        tryAgainButton.setOnClickListener {
            if (isInternetConnected()) {
                // Dismiss the dialog and show a toast
                Toast.makeText(this, "Internet Connected", Toast.LENGTH_SHORT).show()
                dialog.dismiss() // dismiss the no-internet dialog
            } else {
                // Show a toast asking to check the internet connection
                Toast.makeText(this, "Kindly check your internet connection", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun isInternetConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openAppNotificationSettings() {
        // Open the specific app notification settings
        val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Check if notification permission is granted
        if (isNotificationPermissionGranted()) {
            // If permission is granted, dismiss the dialog
            permissionDeniedDialog.dismiss()
        }
    }

    private fun isNotificationPermissionGranted(): Boolean {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return manager.areNotificationsEnabled()
    }


}




