package com.fp.funny.video.call.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fp.funny.video.call.MyApplication
import com.fp.funny.video.call.adapters.PrankCallHistoryAdapter
import com.fp.funny.video.call.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AudioCallFragment : Fragment(R.layout.fragment_audio_call) {

        private lateinit var recyclerView: RecyclerView
        private lateinit var adapter: PrankCallHistoryAdapter
        private lateinit var searchIcon: ImageView

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_audio_call, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            recyclerView = view.findViewById(R.id.rv_fragment_audio_call)
            searchIcon = view.findViewById(R.id.search_icon_history_screen_fragment_audio_call)

            adapter = PrankCallHistoryAdapter()
            recyclerView.adapter = adapter
            recyclerView.layoutManager = GridLayoutManager(context, 3)

            // Load data specific to audio
            loadPrankCallHistory("audio")
        }

        @OptIn(DelicateCoroutinesApi::class)
        private fun loadPrankCallHistory(callType: String) {
            GlobalScope.launch(Dispatchers.IO) {
                val historyList = (requireContext().applicationContext as MyApplication)
                    .database.prankCallHistoryDao().getPrankCallsByType(callType)
                withContext(Dispatchers.Main) {
                    if (historyList.isEmpty()) {
                        searchIcon.visibility = View.VISIBLE
                    } else {
                        searchIcon.visibility = View.GONE
                    }
                    adapter.submitList(historyList)
                }
            }
        }
    }
