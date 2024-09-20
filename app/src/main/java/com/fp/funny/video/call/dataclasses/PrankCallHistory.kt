package com.fp.funny.video.call.dataclasses

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prank_call_history")
data class PrankCallHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageResId: String,
    val celebrityName: String,
    val callType: String // "video", "audio", "chat"
)
