package com.fp.funny.video.call.dataclasses

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_calls")
data class ScheduledCall(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val callerName: String,
    /* val callerNumber: String,*/
    val triggerTime: Long
    /*val ringtoneResId: List<Int>*/

)
