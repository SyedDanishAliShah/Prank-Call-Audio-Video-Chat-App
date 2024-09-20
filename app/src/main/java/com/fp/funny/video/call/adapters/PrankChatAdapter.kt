package com.fp.funny.video.call.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fp.funny.video.call.FakeChatActivity
import com.fp.funny.video.call.MyApplication
import com.fp.funny.video.call.dataclasses.FakeCallItem
import com.fp.funny.video.call.dataclasses.PrankCallHistory
import com.fp.funny.video.call.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PrankChatAdapter(private val prankChatList: List<FakeCallItem>) : RecyclerView.Adapter<PrankChatAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_prank_chat, parent, false)
        return ImageViewHolder(view)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val prankChatData = prankChatList[position]
        val imageResId = prankChatData.image_url
        val nameOfCharacter = prankChatData.name
        val videoUrl = prankChatData.video_url
        Glide.with(holder.imageView.context)
            .load(prankChatData.image_url)
            .placeholder(R.drawable.prank_chat_characters_circle_outline)
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Caches the full-size image and any resized versions
            .apply(RequestOptions.circleCropTransform()) // Applies the circle crop transformation
            .into(holder.imageView)

        holder.nameOfCelebrity.text = prankChatData.name

        holder.imageView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, FakeChatActivity::class.java).apply {
                putExtra("imageResId", imageResId)
                putExtra("nameResId", nameOfCharacter)
                putExtra("videoUrl", videoUrl)
                // Add any other extras if needed
            }
            // Save the prank call history
            val prankCallHistory = PrankCallHistory(
                imageResId = imageResId,
                celebrityName = nameOfCharacter,
                callType = "chat"
            )

            GlobalScope.launch(Dispatchers.IO) {
                (context.applicationContext as MyApplication).database.prankCallHistoryDao().insert(prankCallHistory)
            }

            context.startActivity(intent)
        }


    }

    override fun getItemCount(): Int {
        return prankChatList.size
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.celebrity_pic_prank_chat)
        val nameOfCelebrity : TextView = itemView.findViewById(R.id.tv_name_of_character)
    }
}
