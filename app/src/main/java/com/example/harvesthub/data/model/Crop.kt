package com.example.harvesthub.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "crops")
data class Crop(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val variety: String,
    val plantingDate: Date,
    val expectedHarvestDate: Date,
    val fieldLocation: String,
    val notes: String = "",
    val photoUrls: List<String> = emptyList(),
    val status: CropStatus = CropStatus.ACTIVE
)

enum class CropStatus {
    ACTIVE,
    HARVESTED,
    FAILED
} 