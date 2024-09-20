package com.fp.funny.video.call

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.room.Room
import com.fp.funny.video.call.databaseclass.AppDataBasePrankCallHistory
import com.example.call.RemoteConfig.RemoteConfig
import com.example.call.RemoteConfig.RemoteConfig.isReturningFromExternalActivity
import com.example.call.RemoteConfig.RemoteConfig.isSplash
import com.example.call.ads.InterstitialAd.wasInterstitialAdShown
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

class MyApplication : Application(), Application.ActivityLifecycleCallbacks, LifecycleObserver {

    private var currentActivity: Activity? = null


    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }

    private val AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"

    private lateinit var appOpenAdManager: AppOpenAdManager
    lateinit var database: AppDataBasePrankCallHistory
    lateinit var videoCache: Cache

    inner class AppOpenAdManager {
        private var appOpenAd: AppOpenAd? = null
        private var isLoadingAd = false
        var isShowingAd = false
        private var loadTime: Long? = null

        /** Request an ad. */
        fun loadAd(context: Context) {
            if (isLoadingAd || isAdAvailable()) {
                return
            }

            isLoadingAd = true
            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                context, AD_UNIT_ID, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object : AppOpenAd.AppOpenAdLoadCallback() {

                    override fun onAdLoaded(ad: AppOpenAd) {
                        Log.d(LOG_TAG, "Ad was loaded.")
                        appOpenAd = ad
                        isLoadingAd = false
                        loadTime = Date().time
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d(LOG_TAG, loadAdError.message)
                        isLoadingAd = false
                    }
                })
        }

        /** Check if ad exists and can be shown. */
        fun isAdAvailable(): Boolean {
            return appOpenAd != null
        }

        /** Shows the ad if one isn't already showing. */
        fun showAdIfAvailable(
            activity: Activity,
            onShowAdCompleteListener: OnShowAdCompleteListener
        ) {
            if (isShowingAd) {
                Log.d(LOG_TAG, "The app open ad is already showing.")
                return
            }

            if (!isAdAvailable()) {
                Log.d(LOG_TAG, "The app open ad is not ready yet.")
                onShowAdCompleteListener.onShowAdComplete()
                loadAd(activity)
                return
            }

            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    Log.d(LOG_TAG, "Ad dismissed fullscreen content.")
                    appOpenAd = null
                    isShowingAd = false
                    isSplash = false

                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d(LOG_TAG, adError.message)
                    appOpenAd = null
                    isShowingAd = false
                    isSplash = false

                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(LOG_TAG, "Ad showed fullscreen content.")
                    isShowingAd = true
                     isSplash = true
                }
            }
            isShowingAd = true
            appOpenAd?.show(activity)
        }

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        Log.d(LOG_TAG, isSplash.toString())
        if (wasInterstitialAdShown) {
            // Reset the flag so the app open ad can be shown next time
            wasInterstitialAdShown = true
            Log.d("MyApplication", "Skipping app open ad due to recent interstitial ad.")
            return
        }
        currentActivity?.let { activity ->
            if ( !isReturningFromExternalActivity) {
                // Handle splash screen logic
                if (!appOpenAdManager.isShowingAd) {
                    if (!isSplash)
                    {
                        appOpenAdManager.showAdIfAvailable(
                            activity,
                            object : OnShowAdCompleteListener {
                                override fun onShowAdComplete() {
                                    Log.d("MyApplication", "Splash screen ad has been shown or failed to show.")

                                }
                            }
                        )
                    }

                }
            } else {
                isReturningFromExternalActivity = false
            }
        }
    }





    override fun onCreate() {
        super.onCreate()
        RemoteConfig.getDataFromRemoteConfiguration(this)
        registerActivityLifecycleCallbacks(this)
        InAppPurchases.setupBillingClient(this)

        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            MobileAds.initialize(this@MyApplication) {}
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        appOpenAdManager = AppOpenAdManager()
        database = Room.databaseBuilder(this, AppDataBasePrankCallHistory::class.java, "prank_call_db")
                .build()
        val cacheDir = File(cacheDir, "video_cache")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        videoCache = SimpleCache(cacheDir, NoOpCacheEvictor())
    }

    companion object {
        private const val LOG_TAG = "AppOpenAdManager"
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }


    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }


    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {

    }
}






