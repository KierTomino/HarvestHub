package com.example.harvesthub.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.harvesthub.data.model.Crop
import com.example.harvesthub.data.model.CropStatus

@Dao
interface CropDao {
    @Query("SELECT * FROM crops ORDER BY plantingDate DESC")
    fun getAllCrops(): LiveData<List<Crop>>

    @Query("SELECT * FROM crops WHERE status = :status ORDER BY plantingDate DESC")
    fun getCropsByStatus(status: CropStatus): LiveData<List<Crop>>

    @Query("SELECT * FROM crops WHERE id = :cropId")
    fun getCropById(cropId: Long): LiveData<Crop>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrop(crop: Crop): Long

    @Update
    suspend fun updateCrop(crop: Crop)

    @Delete
    suspend fun deleteCrop(crop: Crop)

    @Query("SELECT * FROM crops WHERE name LIKE '%' || :query || '%' OR variety LIKE '%' || :query || '%'")
    fun searchCrops(query: String): LiveData<List<Crop>>
} 