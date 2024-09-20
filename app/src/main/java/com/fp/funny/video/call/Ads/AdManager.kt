package com.fp.funny.video.call.Ads

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.call.RemoteConfig.RemoteConfig.appOpenAdId
import com.fp.funny.video.call.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

object AdManager {

    @SuppressLint("StaticFieldLeak")
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private var interstitialAd: InterstitialAd? = null
    private var appOpenAd: AppOpenAd? = null

    // Callback interface for ad load status
    interface AdLoadCallback {
        fun onAdLoaded()
        fun onAdDismissed()
        fun onAdFailedToLoad()
    }

    private var adLoadCallback: AdLoadCallback? = null

    fun initialize(context: Context) {
        remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    // Load ads after fetching remote config
                    loadAppOpenAd(context)
                    loadInterstitialAd(context)
                }
            }
    }

    fun setAdLoadCallback(callback: AdLoadCallback) {
        adLoadCallback = callback
    }

    fun loadInterstitialAd(context: Context) {

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, context.getString(R.string.interstitial_id), adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                adLoadCallback?.onAdLoaded() // Notify ad loaded
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        // Notify that the ad was dismissed
                        adLoadCallback?.onAdDismissed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        //Toast.makeText(context, "The internet might be slow", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAdShowedFullScreenContent() {
                        // Handle when the ad is shown
                    }
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Toast.makeText(context, "The internet might be slow", Toast.LENGTH_SHORT).show()
            }
        })
    }


    fun showInterstitialAd(activity: Activity) {
        interstitialAd?.show(activity)
    }

    fun loadAppOpenAd(context: Context) {
        if (appOpenAdId.isEmpty()) {
            appOpenAdId = context.getString(R.string.appopen_id)
        }
        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(context, appOpenAdId, adRequest, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.d("App Open Error", loadAdError.toString())
                Toast.makeText(context, "The internet might be slow", Toast.LENGTH_SHORT).show()
            }
        })
    }



    fun showAppOpenAd(activity: Activity, callback: FullScreenContentCallback? = null) {
        appOpenAd?.let { ad ->
            ad.fullScreenContentCallback = callback
            ad.show(activity)
        } ?: run {
            // Ad is not loaded, trigger the callback manually if available
            val dummyAdError = AdError(0, "App Open Ad not loaded", "AdManager")
            callback?.onAdFailedToShowFullScreenContent(dummyAdError)
        }
    }

}
