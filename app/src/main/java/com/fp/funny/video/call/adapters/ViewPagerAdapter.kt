package com.fp.funny.video.call.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.fp.funny.video.call.fragments.OnBoardingScreenOneFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        // Always create the same fragment but pass the position as an argument
        return OnBoardingScreenOneFragment.newInstance(position)
    }
}

