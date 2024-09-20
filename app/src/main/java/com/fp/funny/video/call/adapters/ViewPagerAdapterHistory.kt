package com.fp.funny.video.call.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.fp.funny.video.call.fragments.AudioCallFragment
import com.fp.funny.video.call.fragments.ChatFragment
import com.fp.funny.video.call.fragments.VideoCallFragment

class ViewPagerAdapterHistory(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> VideoCallFragment()
            1 -> AudioCallFragment()
            2 -> ChatFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}