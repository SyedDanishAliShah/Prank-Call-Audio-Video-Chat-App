package com.example.call.ads

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import androidx.annotation.Keep
import com.example.call.RemoteConfig.RemoteConfig.interstitialAdId
import com.example.call.RemoteConfig.RemoteConfig.interstitialAdsCounter
import com.fp.funny.video.call.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Keep
object InterstitialAd {

    @JvmField
    var mAdIsLoaded: Boolean = false
    var mIsIntestritialShowing: Boolean =
        false              /*ye check check karey ga appopen violation like agr intes show ho raha ho us case me open ad na aye*/
    var mIntestAdIsLoading: Boolean = false
    var clickCount = 0
    var progressDialog: Dialog? = null

    @JvmField
    var timeofadshown: Long? = System.currentTimeMillis()

    var interstitialAdsShown = 0

    val mutableStateFlow = MutableStateFlow("AD")

    var wasInterstitialAdShown = false


    suspend fun addloaded(entry: String) {

        mutableStateFlow.emit(entry)

    }

    @JvmField
    var mInterstitialAd: InterstitialAd? = null

    @JvmStatic
    fun loadAdInterstitialAdmob(context: Context) {

        try {
            /*if (totalnumsofinterstital  == interstitialAdsShown ){

                Log.d("Interstitial","Ads Are Equal to Ads Count $interstitialAdsShown")
                return

            }
            else
            {*/
            Log.d("Interstitial", "Ads Are Nottt Equal to Ads Count $interstitialAdsShown")
            val adRequest = AdRequest.Builder().build()
            try {

                if (mInterstitialAd == null) {

                    if (mIntestAdIsLoading) {
                        Log.d(
                            "TAG_Interstial",
                            "InterstitialAd check already loaded no need to load again"
                        )
                        return
                    }
                    if (interstitialAdId.isEmpty()) {
                        interstitialAdId = context.getString(R.string.interstitial_id)
                    }

                    mIntestAdIsLoading = true
                    InterstitialAd.load(
                        context, interstitialAdId, adRequest,
                        object : InterstitialAdLoadCallback() {
                            override fun onAdFailedToLoad(adError: LoadAdError) {

                                CoroutineScope(Dispatchers.IO).launch {
                                    addloaded("inter failed")
                                }
                                Log.d(
                                    "TAG_1_ad",
                                    "InterstitialAd check onAdFailedToLoad: $adError "
                                )
                                mInterstitialAd = null
                                mAdIsLoaded = false
                                mIntestAdIsLoading = false
                            }

                            override fun onAdLoaded(interstitialAd: InterstitialAd) {

                                //  mutableStateFlow.emit("interstital loaded")

                                CoroutineScope(Dispatchers.IO).launch {
                                    addloaded("inter load")
                                }
                                Log.d("TAG_1_ad", "InterstitialAd check onAdLoaded: ")
                                mInterstitialAd = interstitialAd
                                mAdIsLoaded = true
                                mIntestAdIsLoading = false
                                dialog_count?.dismiss()
                                //                        Utils.showMessage("Interstitial Ad Loaded", context)
                            }
                        }
                    )
                }
            } catch (e: Exception) {

            }
            // }
        } catch (e: Exception) {


        }

    }


    @JvmStatic
    fun showInterstitialAdNormal(
        onoff: Int,
        activity: Activity,
        listner: () -> Unit
    ) {


        if (onoff == 1) {
//            val istimepassed= timeofadshown?.let {
//                Log.d("Time Passed",it.toString())
//                isTimePassed(it, timetoshowinterstitalafter.toLong())
//            }
//            Log.d("Time Passed","${istimepassed.toString()},${timeofadshown?.toString()}")
            if (mInterstitialAd != null && mAdIsLoaded) {

                try {
                    showDialog_countad(activity)
                } catch (e: Exception) {
                }

                Handler(Looper.getMainLooper()).post {

                    mInterstitialAd?.show(activity)
                    mInterstitialAd?.fullScreenContentCallback =

                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                //  log("Ad was dismissed.")

                                timeofadshown = System.currentTimeMillis()
                                Log.d("Time Passed", "timeofadshown = $timeofadshown")
                                Log.d("TAG_1_ad", "onAdDismissedFullScreenContent: ")
                                try {
                                    dialog_count?.dismiss()
                                } catch (e: Exception) {
                                }

                                mInterstitialAd = null
                                mAdIsLoaded = false
                                loadAdInterstitialAdmob(activity)
                                mIsIntestritialShowing = false
                                listner.invoke()


                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                //   log("Ad failed to show ${p0.message}")
                                try {
                                    dialog_count?.dismiss()
                                } catch (e: Exception) {
                                }

                                Log.d("TAG_1_ad", "onAdFailedToShowFullScreenContent: ")
                                mInterstitialAd = null
                                mAdIsLoaded = false
                                listner.invoke()
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                //  log("Ad showed fullscreen content.")
                                interstitialAdsShown++
                                mIsIntestritialShowing = true
                                dialog_count?.dismiss()

                                Log.d("TAG_1_ad", "onAdShowedFullScreenContent: ")

                            }
                        }
                }

            } else {

                loadAdInterstitialAdmob(activity)
                listner.invoke()
            }


        }


    }


    //    fun isTimePassed(startTimeMillis: Long?, durationMillis: Long?): Boolean {
//        Log.d("Time Passed 123", "timeofadshown = $startTimeMillis")
//        val currentTimeMillis = System.currentTimeMillis()
//        val elapsedTimeMillis = currentTimeMillis - startTimeMillis!!
//        return elapsedTimeMillis >= durationMillis!!
//    }
    @JvmStatic
    fun showInterstitialAdWithClickCount(
        activity: Activity,
        listner: () -> Unit,


        ) {


        // if(!isPurchase){

        clickCount += 1
        Log.d("TAG_1_ad", "Click Count Check: ${clickCount}")

//            val istimepassed= timeofadshown?.let {
//                isTimePassed(it,timetoshowinterstitalafter.toLong())
//            }
        if (interstitialAdsCounter > 0) {

            if (clickCount % interstitialAdsCounter == 0) {

                Log.d("TAG_1_ad", "Click Count $clickCount remote value ${interstitialAdsCounter}")
                if (mInterstitialAd != null && mAdIsLoaded) {

                    try {
                        showDialog_countad(activity)
                    } catch (e: Exception) {
                    }

                    Handler(Looper.getMainLooper()).post {

                        mInterstitialAd?.show(activity)
                        mInterstitialAd?.fullScreenContentCallback =

                            object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent()
                                    // timeofadshown = System.currentTimeMillis()
                                    showLoadingDialog(activity)
                                    Log.d("TAG_1_ad", "onAdDismissedFullScreenContent: ")
                                    dialog_count?.dismiss()  // Move this above
                                    mInterstitialAd = null
                                    mAdIsLoaded = false
                                    loadAdInterstitialAdmob(activity)
                                    mIsIntestritialShowing = false
                                    listner.invoke()
                                }


                                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                    super.onAdFailedToShowFullScreenContent(p0)
                                    //   log("Ad failed to show ${p0.message}")
                                    showLoadingDialog(activity)
                                    Log.d("TAG_1_ad", "onAdFailedToShowFullScreenContent: ")
                                    try {
                                        dialog_count?.dismiss()
                                    } catch (e: Exception) {
                                    }

                                    mInterstitialAd = null
                                    mAdIsLoaded = false
                                    wasInterstitialAdShown = false
                                    listner.invoke()
                                }

                                override fun onAdShowedFullScreenContent() {
                                    super.onAdShowedFullScreenContent()
                                    //  log("Ad showed fullscreen content.")
                                    showLoadingDialog(activity)
                                    interstitialAdsShown++
                                    mIsIntestritialShowing = true
                                    wasInterstitialAdShown = true
                                    dialog_count?.dismiss()

                                    Log.d("TAG_1_ad", "onAdShowedFullScreenContent: ")

                                }
                            }
                    }

                } else {
                    listner.invoke()
                    loadAdInterstitialAdmob(activity)
                    clickCount -= 1

                }
                // }
                //    }


            } else {
                listner.invoke()
            }


        }
    }


    var dialog_count: Dialog? = null
    fun showDialog_countad(activity: Activity) {
        dialog_count =
            Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
        dialog_count?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog_count?.setCancelable(false)
        dialog_count?.setContentView(R.layout.dialog_progress)
        dialog_count?.show()

    }


    @SuppressLint("InflateParams")
    @JvmStatic
    fun showLoadingDialog(activity: Activity) {
        if (activity.isFinishing || activity.isDestroyed) return

        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_video_interstitial_ad)
        dialog.show()
    }

        var dialog_nocount: Dialog? = null
        fun showDialog_nocountad(activity: Activity) {
            dialog_nocount =
                Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
            dialog_nocount?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog_nocount?.setCancelable(false)
            dialog_nocount?.setContentView(R.layout.dialog_progress)
            dialog_nocount?.show()
        }
    }


