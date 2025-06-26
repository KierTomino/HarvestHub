package com.example.harvesthub.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cropId: Long,
    val activityType: ActivityType,
    val date: Date,
    val description: String,
    val photoUrls: List<String> = emptyList(),
    val notes: String = "",
    val weatherConditions: String = "",
    val resourcesUsed: List<String> = emptyList()
)

enum class ActivityType {
    PLANTING,
    WATERING,
    FERTILIZING,
    PEST_CONTROL,
    WEEDING,
    PRUNING,
    HARVESTING,
    OTHER
} 