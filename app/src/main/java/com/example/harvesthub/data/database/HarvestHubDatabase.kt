package com.example.harvesthub.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.harvesthub.data.model.ActivityLog
import com.example.harvesthub.data.model.Crop
import com.example.harvesthub.data.util.Converters

@Database(entities = [Crop::class, ActivityLog::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class HarvestHubDatabase : RoomDatabase() {
    abstract fun cropDao(): CropDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile
        private var INSTANCE: HarvestHubDatabase? = null

        fun getDatabase(context: Context): HarvestHubDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HarvestHubDatabase::class.java,
                    "harvest_hub_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 