package com.fp.funny.video.call

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.fp.funny.video.call.adapters.ViewPagerAdapterHistory
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HistoryActivity : AppCompatActivity() {

    private lateinit var backIcon : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        backIcon = findViewById(R.id.back_icon_arrow_history_screen)

        backIcon.setOnClickListener {
            val intent = Intent(this, ScheduleACallActivity::class.java)
            startActivity(intent)
            finish()
        }

        val viewPager: ViewPager2 = findViewById(R.id.view_pager_history_screen)
        val tabLayout: TabLayout = findViewById(R.id.tab_layout_history_screen)

        // Set up the ViewPager with the adapter
        viewPager.adapter = ViewPagerAdapterHistory(this)

        // Attach the TabLayout with the ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Video call"
                1 -> "Audio call"
                2 -> "Chat"
                else -> throw IllegalStateException("Unexpected position $position")
            }
        }.attach()
    }
    }
