package com.fp.funny.video.call.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fp.funny.video.call.IncomingVideoCallActivity
import com.fp.funny.video.call.MyApplication
import com.fp.funny.video.call.dataclasses.FakeCallItem
import com.fp.funny.video.call.dataclasses.PrankCallHistory
import com.example.call.ads.InterstitialAd
import com.fp.funny.video.call.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PrankVideoAdapter(
    private val prankVideoList: List<FakeCallItem> = emptyList(),
    private val context: Context,
    private val listener: OnBackPressListener
) : RecyclerView.Adapter<PrankVideoAdapter.PrankVideoViewHolder>() {

    private lateinit var progressDialog: Dialog
    private var adLoadedCallback: (() -> Unit)? = null



    inner class PrankVideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imageView_prank_video)
        val name: TextView = itemView.findViewById(R.id.celebrity_name_tv)
        val phone: TextView = itemView.findViewById(R.id.celebrity_name_number)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrankVideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_prank_video, parent, false)
        return PrankVideoViewHolder(view)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onBindViewHolder(holder: PrankVideoViewHolder, position: Int) {
        val prankVideoData = prankVideoList[position]
        val imageResId = prankVideoData.image_url
        val nameOfCharacter = prankVideoData.name
        val videoUrl = prankVideoData.video_url

        holder.name.text = nameOfCharacter

        Glide.with(holder.image.context)
            .load(imageResId)
            .placeholder(R.drawable.rectangle_for_placeholder_prank_video_characters)
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Caches the full-size image and any resized versions
            .into(holder.image)

        holder.phone.text = prankVideoData.phone

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context as Activity

            // Show the loading dialog
            // Save the prank call history
            val prankCallHistory = PrankCallHistory(
                imageResId = imageResId,
                celebrityName = nameOfCharacter,
                callType = "video"
            )

            GlobalScope.launch(Dispatchers.IO) {
                (context.applicationContext as MyApplication).database.prankCallHistoryDao().insert(prankCallHistory)
            }

            // Show interstitial ad and start the activity after the ad is dismissed

            InterstitialAd.showInterstitialAdWithClickCount(context){
                val intent = Intent(context, IncomingVideoCallActivity::class.java).apply {
                    putExtra("imageUrl", imageResId)
                    putExtra("celebrityName", nameOfCharacter)
                    putExtra("videoUrl", videoUrl)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = prankVideoList.size

    @SuppressLint("InflateParams")
    private fun showLoadingDialog(context: Context): Dialog {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null)
        val progressDialog = Dialog(context, androidx.constraintlayout.widget.R.style.AlertDialog_AppCompat)
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        progressDialog.setContentView(dialogView)
        progressDialog.setCancelable(true) // Prevent dialog from being dismissed by clicking outside
        progressDialog.setOnCancelListener {
            // Handle what happens when the dialog is canceled (optional)
            backMethod() // This will call the overridden onBackPressed method
        }
        progressDialog.show()
        return progressDialog
    }

    interface OnBackPressListener {
        fun onBackPressHandled()
    }
    private fun backMethod() {
        listener.onBackPressHandled() // This will call the onBackPressHandled() method in the activity
    }



}
