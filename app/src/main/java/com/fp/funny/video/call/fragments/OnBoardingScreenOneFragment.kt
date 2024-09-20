package com.fp.funny.video.call.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import com.fp.funny.video.call.MainActivity
import com.fp.funny.video.call.R

class OnBoardingScreenOneFragment : Fragment() {

    private var position: Int = 0

    companion object {
        private const val ARG_POSITION = "position"

        fun newInstance(position: Int): OnBoardingScreenOneFragment {
            val fragment = OnBoardingScreenOneFragment()
            val args = Bundle()
            args.putInt(ARG_POSITION, position)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            position = it.getInt(ARG_POSITION)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout based on the position
        return when (position) {
            0 -> inflater.inflate(R.layout.fragment_on_boarding_screen_one, container, false)
            1 -> inflater.inflate(R.layout.fragment_on_boarding_screen_two, container, false)
            2 -> inflater.inflate(R.layout.fragment_on_boarding_screen_three, container, false)
            else -> inflater.inflate(R.layout.fragment_on_boarding_screen_one, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nextButton = view.findViewById<ImageView>(R.id.next_button)
        val skipTv = view.findViewById<TextView>(R.id.skip_tv)

        // Get the ViewPager2 from the activity
        val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager_on_boarding)

        nextButton.setOnClickListener {
            // Navigate to the next item or show dialog
            if (position == 2) {
                navigateToMainActivityAndShowDialog()
            } else {
                viewPager.currentItem += 1
            }
        }

        // Check if skipTv is present in the layout
        skipTv?.let {
            it.setOnClickListener {
                val intent = Intent(activity, MainActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }
        }
    }

    private fun navigateToMainActivityAndShowDialog() {
        // Navigate to MainActivity
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)

        // Delay sending the broadcast to ensure MainActivity is ready
        Handler(Looper.getMainLooper()).postDelayed({
            val broadcastIntent = Intent("SHOW_PRIVACY_POLICY_DIALOG")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(broadcastIntent)
        }, 100)  // Adjust the delay if needed
    }

    /*private fun showPrivacyPolicyDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_privacy_policy, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val checkBox = dialogView.findViewById<ImageView>(R.id.check_box_privacy_policy_dialog)
        val continueButton = dialogView.findViewById<ImageView>(R.id.privacy_policy_dialogue_button_rectangle)

        checkBox.setOnClickListener {
            checkBox.setImageResource(R.drawable.checked_icon_privacy_policy_dialogue)  // Change to the new checkbox image
        }

        continueButton.setOnClickListener {
            continueButton.setImageResource(R.drawable.privacy_privacy_dialogue_selected_rectangle)  // Change to the new continue button image
            Handler(Looper.getMainLooper()).postDelayed({
                dialog.dismiss()
            }, 300)  // Adjust the delay to fit the desired button press animation time
        }

        dialog.show()
    }*/
}


