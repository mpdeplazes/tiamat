package com.mpdeplazes.tiamat.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mpdeplazes.tiamat.db.entity.MarkerEntity

@Dao
interface MarkerDao {
    @Query("SELECT * FROM markers")
    fun getAll(): List<MarkerEntity>

    @Insert
    suspend fun insert(vararg markerEntity: MarkerEntity)

    @Delete
    suspend fun delete(vararg markerEntity: MarkerEntity)

    @Query("DELETE FROM markers WHERE latitude = :latitude AND longitude = :longitude")
    suspend fun deleteByLocation(latitude: Double, longitude: Double)
}