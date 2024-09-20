package com.fp.funny.video.call.daoclass

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fp.funny.video.call.dataclasses.PrankCallHistory

@Dao
interface PrankCallHistoryDao {
    @Insert
    suspend fun insert(prankCallHistory: PrankCallHistory)

    @Query("SELECT * FROM prank_call_history WHERE callType = :callType")
    suspend fun getPrankCallsByType(callType: String): List<PrankCallHistory>
}