package com.mpdeplazes.tiamat.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "markers")
data class MarkerEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val latitude: Double,
    val longitude: Double
)