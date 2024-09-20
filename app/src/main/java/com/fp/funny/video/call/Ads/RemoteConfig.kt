package com.example.call.RemoteConfig

import android.content.Context
import android.util.Log
import com.fp.funny.video.call.BuildConfig
import com.fp.funny.video.call.R
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.json.JSONObject

object RemoteConfig {
    //Dont Call From Remote
     var isReturningFromExternalActivity = false

    var isSplash = true

    ////

    var interstitialAdId = ""
    var nativeAdId = ""
    var bannerAdId = ""
    var TopbannerAdId = ""
    var BottombannerAdId = ""
    var appOpenAdId = ""
    var rewardedAdId = ""
    var rewardedInterstitialAdId = ""

    var interstitialAdsCounter = 3


    var prankvideocallinteronoff = 1

    var privacypolicylink = "https://calculatorfunprime.blogspot.com/2024/09/har-apps-studio-respects-and-protects.html"




    var timetoshowinterstitalafter = 20000


    fun getDataFromRemoteConfiguration(mContext: Context) {
        val mFirebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        if (!BuildConfig.DEBUG) {

            Log.e("remoteConfig", "getDataFromRemoteConfiguration: Release Part Running")

            val configBuilder =
                FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(0)

            val cacheInterval: Long = 0
            configBuilder.minimumFetchIntervalInSeconds = cacheInterval
            mFirebaseRemoteConfig.setConfigSettingsAsync(configBuilder.build())
            mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    Log.e("remoteConfig", "Check onComplete Listener $task")
                    if (task.isSuccessful) {
                        mFirebaseRemoteConfig.activate()

                        Log.e("remoteConfig", "Check onComplete activate $task")

                        try {

                            val PrankCallAdsJson = mFirebaseRemoteConfig.getString("PrankCallAdsJson")
                            val PrankCallJsonObject_ = JSONObject(PrankCallAdsJson)



                            //Json1 values
                            try {





                                try {
                                    appOpenAdId = PrankCallJsonObject_.getString("app_open_id")
                                    Log.e("remoteConfig", "onComplete: APP_OPEN_AD_ID $appOpenAdId")
                                } catch (e: Exception) {

                                    appOpenAdId=mContext.getString(R.string.appopen_id)
                                    Log.e(
                                        "remoteConfig",
                                        "onComplete: APP_OPEN_AD_ID Catch:  $appOpenAdId"
                                    )
                                }


                                try {
                                    bannerAdId = PrankCallJsonObject_.getString("bannerAdId")
                                    Log.e("remoteConfig", "onComplete: banner Ad Id $bannerAdId")
                                } catch (e: Exception) {
                                    bannerAdId=mContext.getString(R.string.banner_id)
                                }
                                try {
                                    TopbannerAdId = PrankCallJsonObject_.getString("TopbannerAdId")
                                    Log.e("remoteConfig", "onComplete: Collapsible banner Ad Id $TopbannerAdId")
                                } catch (e: Exception) {
                                    TopbannerAdId=mContext.getString(R.string.banner_id)
                                }
                                try {
                                    BottombannerAdId = PrankCallJsonObject_.getString("BottombannerAdId")
                                    Log.e("remoteConfig", "onComplete: Collapsible banner Ad Id $BottombannerAdId")
                                } catch (e: Exception) {
                                    BottombannerAdId=mContext.getString(R.string.banner_id)
                                }

                                try {
                                    nativeAdId = PrankCallJsonObject_.getString("nativeAdId")
                                    Log.e("remoteConfig", "onComplete: Native  $nativeAdId")
                                } catch (e: Exception) {
                                    nativeAdId=mContext.getString(R.string.native_id)
                                }

                                try {
                                    interstitialAdId = PrankCallJsonObject_.getString("interstitialAdId")
                                    Log.e(
                                        "remoteConfig",
                                        "onComplete: Interstitial $interstitialAdId"
                                    )
                                } catch (e: Exception) {
                                    interstitialAdId=mContext.getString(R.string.interstitial_id)
                                }
                                try {
                                    prankvideocallinteronoff = PrankCallJsonObject_.getInt("prankvideocallinteronoff")
                                    Log.e(
                                        "remoteConfig",
                                        "onComplete: prankvideocallinteronoff $prankvideocallinteronoff"
                                    )
                                } catch (e: Exception) {

                                }

                                try {
                                    privacypolicylink = PrankCallJsonObject_.getString("privacypolicylink")

                                } catch (e: Exception) {

                                }




                            } catch (e: Exception) {
                            }
                        } catch (e: Exception) {
                            Log.e("Remote Error",e.toString())
                        }



                    }
                }
        } else {



            appOpenAdId = mContext.getString(R.string.appopen_id)
            Log.e("remoteConfig", "onComplete: appOpenId Ad Id $appOpenAdId")

            bannerAdId = mContext.getString(R.string.banner_id)
            Log.e("remoteConfig", "onComplete: banner Ad Id $bannerAdId")

            nativeAdId = mContext.getString(R.string.native_id)
            Log.e("remoteConfig", "onComplete: Native  $nativeAdId")

            interstitialAdId = mContext.getString(R.string.interstitial_id)
            Log.e("remoteConfig", "onComplete: intestrial  $interstitialAdId")

            privacypolicylink = "https://calculatorfunprime.blogspot.com/2024/09/har-apps-studio-respects-and-protects.html"



        }

    }
}