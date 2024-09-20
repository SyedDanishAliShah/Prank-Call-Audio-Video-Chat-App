package com.fp.funny.video.call

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import com.fp.funny.video.call.adapters.ChatMessageAdapter

class SenderReceiverDialog(
    context: Context,
    private val onSenderSelected: () -> Unit,
    private val onReceiverSelected: () -> Unit,
    private var chatMessageAdapter: ChatMessageAdapter
) : Dialog(context) {

    init {
        setContentView(R.layout.dialog_sender_receiver_janny)

        val senderNameButton: View = findViewById(R.id.me_tv_janny)
        val receiverNameButton: View = findViewById(R.id.character_tv)

        senderNameButton.setOnClickListener {
            onSenderSelected()  // Execute the logic for sender
            dismiss()  // Immediately dismiss the dialog

            // Update RecyclerView after dismissing the dialog
            Handler(Looper.getMainLooper()).postDelayed({
                updateRecyclerView()
            }, 1000)  // Adjust the delay time as needed
        }

        receiverNameButton.setOnClickListener {
            onReceiverSelected()  // Execute the logic for receiver
            dismiss()  // Immediately dismiss the dialog

            // Update RecyclerView after dismissing the dialog
            Handler(Looper.getMainLooper()).postDelayed({
                updateRecyclerView()
            }, 1000)  // Adjust the delay time as needed
        }
    }

    override fun onStart() {
        super.onStart()

        // Convert width and height from pixels to dp
        val widthInPx = 400 // Example width in pixels
        val heightInPx = 300 // Example height in pixels

        val widthInDp = convertPxToDp(widthInPx, context)
        val heightInDp = convertPxToDp(heightInPx, context)

        // Convert dp back to pixels to set the dialog size
        val widthInDpPx = convertDpToPx(widthInDp, context)
        val heightInDpPx = convertDpToPx(heightInDp, context)

        window?.setLayout(widthInDpPx, heightInDpPx)

        // Adjust dialog position here if needed
        val sendMessageIconLocation = IntArray(2)
        val sendMessageIconView = (context as? Activity)?.findViewById<View>(R.id.send_message_icon)
        sendMessageIconView?.getLocationOnScreen(sendMessageIconLocation)
        val layoutParams = window?.attributes
        layoutParams?.x = sendMessageIconLocation[0] - (-140 - (sendMessageIconView?.width ?: 0) / 2)
        layoutParams?.y = sendMessageIconLocation[1] - (-460)
        window?.attributes = layoutParams
    }

    // Helper method to convert px to dp
    private fun convertPxToDp(px: Int, context: Context): Int {
        return (px / context.resources.displayMetrics.density).toInt()
    }

    // Helper method to convert dp to px
    private fun convertDpToPx(dp: Int, context: Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    // Method to update RecyclerView
    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecyclerView() {
        chatMessageAdapter.notifyDataSetChanged()  // Example update call
    }
}
