package com.fp.funny.video.call.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.fp.funny.video.call.R

class ShowSenderReceiverDialogFragment : DialogFragment() {

    private var onSenderSelected: ((Boolean) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.dialog_sender_receiver_janny, container, false)

        val senderNameButton: View = view.findViewById(R.id.me_tv_janny)
        val receiverNameButton: View = view.findViewById(R.id.character_tv)

        senderNameButton.setOnClickListener {
            onSenderSelected?.invoke(true)
            dismiss()
        }

        receiverNameButton.setOnClickListener {
            onSenderSelected?.invoke(false)
            dismiss()
        }

        return view
    }

    override fun onStart() {
        super.onStart()

        // Convert width and height from pixels to dp
        val widthInPx = 400 // Example width in pixels
        val heightInPx = 300 // Example height in pixels

        val widthInDp = convertPxToDp(widthInPx, requireContext())
        val heightInDp = convertPxToDp(heightInPx, requireContext())

        // Convert dp back to pixels to set the dialog size
        val widthInDpPx = convertDpToPx(widthInDp, requireContext())
        val heightInDpPx = convertDpToPx(heightInDp, requireContext())

        dialog?.window?.setLayout(widthInDpPx, heightInDpPx)

        // Adjust dialog position here if needed
        val sendMessageIconLocation = IntArray(2)
        (activity?.findViewById<View>(R.id.send_message_icon))?.getLocationOnScreen(sendMessageIconLocation)
        val layoutParams = dialog?.window?.attributes
        layoutParams?.x = sendMessageIconLocation[0] - (widthInDpPx / 2 - (activity?.findViewById<View>(R.id.send_message_icon)?.width ?: 0) / 2)
        layoutParams?.y = sendMessageIconLocation[1] - heightInDpPx
        dialog?.window?.attributes = layoutParams
    }

    // Helper method to convert px to dp
    private fun convertPxToDp(px: Int, context: Context): Int {
        return (px / context.resources.displayMetrics.density).toInt()
    }

    // Helper method to convert dp to px
    private fun convertDpToPx(dp: Int, context: Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

}
