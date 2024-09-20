package com.fp.funny.video.call.databaseclass

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fp.funny.video.call.daoclass.PrankCallHistoryDao
import com.fp.funny.video.call.dataclasses.PrankCallHistory

@Database(entities = [PrankCallHistory::class], version = 1)
abstract class AppDataBasePrankCallHistory : RoomDatabase() {
    abstract fun prankCallHistoryDao(): PrankCallHistoryDao
}