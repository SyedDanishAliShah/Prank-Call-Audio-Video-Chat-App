package com.fp.funny.video.call.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fp.funny.video.call.dataclasses.ChatMessage
import com.fp.funny.video.call.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChatMessageAdapter(private val messageList: MutableList<ChatMessage>) : RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]

        // First, reset the visibility and content of all views
        holder.senderMessageContainer.visibility = View.GONE
        holder.senderMessageTextView.visibility = View.GONE
        holder.senderTimeTextView.visibility = View.GONE

        holder.receiverMessageContainer.visibility = View.GONE
        holder.receiverMessageTextView.visibility = View.GONE
        holder.receiverTimeTextView.visibility = View.GONE

        holder.imageViewChatSender.visibility = View.GONE
        holder.imageViewChatReceiver.visibility = View.GONE

        if (message.isImage) {
            // Show image views based on sender or receiver
            if (message.isSender) {
                holder.imageViewChatSender.visibility = View.VISIBLE
                holder.imageViewChatSender.setImageURI(Uri.parse(message.imageUri))
            } else {
                holder.imageViewChatReceiver.visibility = View.VISIBLE
                holder.imageViewChatReceiver.setImageURI(Uri.parse(message.imageUri))
            }
        } else {
            // Handle text messages
            if (message.isSender) {
                holder.senderMessageContainer.visibility = View.VISIBLE
                holder.senderMessageTextView.visibility = View.VISIBLE
                holder.senderTimeTextView.visibility = View.VISIBLE
                holder.senderMessageTextView.text = message.content
            } else {
                holder.receiverMessageContainer.visibility = View.VISIBLE
                holder.receiverMessageTextView.visibility = View.VISIBLE
                holder.receiverTimeTextView.visibility = View.VISIBLE
                holder.receiverMessageTextView.text = message.content
            }

            // Set the time for text messages
            val currentTime = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val formattedTime = dateFormat.format(currentTime)

            if (message.isSender) {
                holder.senderTimeTextView.text = formattedTime
            } else {
                holder.receiverTimeTextView.text = formattedTime
            }
        }
    }


    override fun getItemCount(): Int = messageList.size

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Text message views
        val receiverMessageContainer: ImageView = itemView.findViewById(R.id.rectangle_for_message_sender)
        val receiverMessageTextView: TextView = itemView.findViewById(R.id.type_a_message_tv_sender)
        val receiverTimeTextView: TextView = itemView.findViewById(R.id.current_time_tv_sender)

        val senderMessageContainer: ImageView = itemView.findViewById(R.id.rectangle_for_message_receiver)
        val senderMessageTextView: TextView = itemView.findViewById(R.id.type_a_message_tv_receiver)
        val senderTimeTextView: TextView = itemView.findViewById(R.id.current_time_tv_receiver)

        // Image message views
        val imageViewChatSender: ImageView = itemView.findViewById(R.id.imageView_chat_sender)
        val imageViewChatReceiver: ImageView = itemView.findViewById(R.id.imageView_chat_receiver)
    }
}

























