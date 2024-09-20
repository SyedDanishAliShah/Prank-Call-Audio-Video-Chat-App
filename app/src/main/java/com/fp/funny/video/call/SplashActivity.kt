package com.fp.funny.video.call

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.fp.funny.video.call.Ads.AdManager
import com.fp.funny.video.call.Ads.AdManager.loadAppOpenAd
import com.fp.funny.video.call.adapters.ViewPagerAdapter
import com.example.call.ads.InterstitialAd.loadAdInterstitialAdmob
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var progressDialog: Dialog
    private var isAdShowing = false // Flag to track if the ad is currently showing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Fetch fresh data from the API
        fetchAndCacheData()

        val progressBar: ProgressBar = findViewById(R.id.splash_screen_progress_loader)

        // Load the interstitial ad and App Open ad
        loadAdInterstitialAdmob(this)
        val appOpenAdManager = MyApplication().AppOpenAdManager()
         appOpenAdManager.loadAd(applicationContext)
           loadAppOpenAd(this)

        fun animateProgressBar(progressBar: ProgressBar, toProgress: Int, duration: Long) {
            val animation = ObjectAnimator.ofInt(progressBar, "progress", toProgress)
            animation.duration = duration
            animation.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    // Show the dialog with spinner when animation ends
                    showLoadingDialog()
                    // Display the app open ad first
                    showAppOpenAd()
                }

                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationRepeat(animation: Animator) {}
            })
            animation.start()
        }

        animateProgressBar(progressBar, 100, 5000)
    }

    /*private fun checkInternetConnection() {
        // Function to check the internet connection status
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        hasInternetConnection = networkInfo != null && networkInfo.isConnected

        if (!hasInternetConnection) {
            Toast.makeText(this, "Please check your internet connection.", Toast.LENGTH_LONG).show()
            lifecycleScope.launch {
                delay(5000)
                proceedToMainActivity()
            }
        }
    }*/

    @SuppressLint("SuspiciousIndentation")
    private fun showAppOpenAd() {
        if (isAdShowing) return // Prevent multiple ads from showing

        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)

        isAdShowing = true // Set the flag to true when ad is about to show

        AdManager.showAppOpenAd(this, object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isAdShowing = false // Reset the flag when the ad is dismissed
                hideLoadingDialog()
                if (isFirstRun) {
                    showOnboardingScreens()
                    sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
                } else {
                    proceedToMainActivity()
                }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                isAdShowing = false // Reset the flag when ad fails to show
                showLoadingDialog()
                Toast.makeText(this@SplashActivity, "The internet might be slow. Please go back and try again.", Toast.LENGTH_SHORT).show()
                proceedToMainActivity()
            }
        })
    }

    private fun fetchAndCacheData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.prankVideoItems.getPrankVideoData()

                if (response.isSuccessful) {
                    val categories = response.body() ?: emptyList()
                    val videoChatData = categories.find { it.FakeCallData.name == "Video Chat" }?.data.orEmpty()
                    val audioChatData = categories.find { it.FakeCallData.name == "Audi Chat" }?.data.orEmpty()
                    val textChatData = categories.find { it.FakeCallData.name == "Text Chat" }?.data.orEmpty()

                    val sharedPreferences = getSharedPreferences("prank_data", Context.MODE_PRIVATE)
                    sharedPreferences.edit().apply {
                        putString("video_chat_data", Gson().toJson(videoChatData))
                        putString("audio_chat_data", Gson().toJson(audioChatData))
                        putString("text_chat_data", Gson().toJson(textChatData))
                        apply()
                    }
                } else {
                    Log.e("MainActivity", "Error: ${response.message()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showOnboardingScreens() {
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager_on_boarding)
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(0, true)
        sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
    }

    @SuppressLint("InflateParams")
    private fun showLoadingDialog() {
        if (isFinishing || isDestroyed) return

        val dialogView = layoutInflater.inflate(R.layout.dialog_progress, null)
        progressDialog = Dialog(this, androidx.constraintlayout.widget.R.style.AlertDialog_AppCompat)
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        progressDialog.setContentView(dialogView)
        progressDialog.setCancelable(true)
        progressDialog.setOnCancelListener {
            onBackPressed()
        }
        progressDialog.show()
    }

    private fun hideLoadingDialog() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    private fun proceedToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        if (progressDialog.isShowing) {
            hideLoadingDialog()
        }
        finish() // Close the SplashActivity
    }



}








