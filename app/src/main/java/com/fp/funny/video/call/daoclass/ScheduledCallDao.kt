package com.fp.funny.video.call.daoclass

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import com.fp.funny.video.call.dataclasses.ScheduledCall

@Dao
interface ScheduledCallDao {
    @Insert
    suspend fun insert(scheduledCall: ScheduledCall)

    @Delete
    suspend fun delete(scheduledCall: ScheduledCall)
}