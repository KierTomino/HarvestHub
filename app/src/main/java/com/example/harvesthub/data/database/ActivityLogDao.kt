package com.example.harvesthub.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.harvesthub.data.model.ActivityLog
import com.example.harvesthub.data.model.ActivityType

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs WHERE cropId = :cropId ORDER BY date DESC")
    fun getActivitiesForCrop(cropId: Long): LiveData<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE activityType = :type ORDER BY date DESC")
    fun getActivitiesByType(type: ActivityType): LiveData<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs ORDER BY date DESC")
    fun getAllActivities(): LiveData<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityLog): Long

    @Update
    suspend fun updateActivity(activity: ActivityLog)

    @Delete
    suspend fun deleteActivity(activity: ActivityLog)

    @Query("SELECT * FROM activity_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getActivitiesInDateRange(startDate: Long, endDate: Long): LiveData<List<ActivityLog>>
} 