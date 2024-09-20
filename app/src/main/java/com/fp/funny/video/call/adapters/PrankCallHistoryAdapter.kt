package com.fp.funny.video.call.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.fp.funny.video.call.dataclasses.PrankCallHistory
import com.fp.funny.video.call.R

class PrankCallHistoryAdapter : ListAdapter<PrankCallHistory, PrankCallHistoryAdapter.ViewHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutRes = when (viewType) {
            VIEW_TYPE_VIDEO -> R.layout.item_video_call_history
            VIEW_TYPE_AUDIO -> R.layout.item_audio_call_history
            VIEW_TYPE_CHAT -> R.layout.item_fake_chat_history
            else -> R.layout.item_video_call_history // Default layout
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prankCallHistory = getItem(position)
        holder.bind(prankCallHistory)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).callType) {
            "video" -> VIEW_TYPE_VIDEO
            "audio" -> VIEW_TYPE_AUDIO
            "chat" -> VIEW_TYPE_CHAT
            else -> VIEW_TYPE_DEFAULT
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.character_image_video_call_history)
        private val textViewName: TextView = itemView.findViewById(R.id.name_of_the_character_image_video_call_history)

        @SuppressLint("DiscouragedApi")
        fun bind(prankCallHistory: PrankCallHistory) {
            val context = itemView.context

            if (prankCallHistory.imageResId.startsWith("http")) {
                // Use Glide to load the image if it's a URL
                Glide.with(context)
                    .load(prankCallHistory.imageResId)
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                    .placeholder(R.drawable.ic_launcher_background) // Use a placeholder image
                    .into(imageView)
            } else {
                // If it's a resource name, convert it to an ID
                val resId = context.resources.getIdentifier(prankCallHistory.imageResId, "drawable", context.packageName)
                if (resId != 0) {
                    imageView.setImageResource(resId)
                } else {
                    // Handle the case where the resource is not found (fallback)
                    imageView.setImageResource(R.drawable.ic_launcher_background)
                }
            }

            textViewName.text = prankCallHistory.celebrityName
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PrankCallHistory>() {
        override fun areItemsTheSame(oldItem: PrankCallHistory, newItem: PrankCallHistory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PrankCallHistory, newItem: PrankCallHistory): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        private const val VIEW_TYPE_VIDEO = 1
        private const val VIEW_TYPE_AUDIO = 2
        private const val VIEW_TYPE_CHAT = 3
        private const val VIEW_TYPE_DEFAULT = 0
    }
}
