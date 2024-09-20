package com.fp.funny.video.call.databaseclass

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fp.funny.video.call.daoclass.ScheduledCallDao
import com.fp.funny.video.call.dataclasses.ScheduledCall

@Database(entities = [ScheduledCall::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scheduledCallDao(): ScheduledCallDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

