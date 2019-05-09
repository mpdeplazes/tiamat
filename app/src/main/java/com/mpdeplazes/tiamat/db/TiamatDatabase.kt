package com.mpdeplazes.tiamat.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.android.gms.maps.model.LatLng
import com.mpdeplazes.tiamat.db.dao.MarkerDao
import com.mpdeplazes.tiamat.db.entity.MarkerEntity
import com.mpdeplazes.tiamat.util.SingletonHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [MarkerEntity::class], version = 1)
abstract class TiamatDatabase : RoomDatabase() {

    abstract fun markerDao(): MarkerDao

    /**
     * Adds all markers, gets the new set of all, and passes them to callback function
     */
    fun createMarkers(
        coroutineScope: CoroutineScope,
        markerEntities: List<MarkerEntity>,
        onResult: (markers: List<MarkerEntity>) -> Unit
    ) {

        coroutineScope.launch {
            markerDao().insert(*markerEntities.toTypedArray())

            // todo I could see this being a problem as the table gets large enough
            // todo Retrieve only visible markers from db
            onResult(markerDao().getAll())
        }
    }

    /**
     * Deletes all markers, and then returns the new set of all markers to callback fun
     */
    fun deleteMarkers(
        coroutineScope: CoroutineScope,
        markerEntities: List<MarkerEntity>,
        onResult: (markers: List<MarkerEntity>) -> Unit
    ) {
        coroutineScope.launch {
            markerDao().delete(*markerEntities.toTypedArray())
            onResult(markerDao().getAll())
        }
    }

    /**
     * Delete a marker by location, returns new set of markers to callback fun
     */
    fun deleteMarkerByLocation(
        coroutineScope: CoroutineScope,
        latLng: LatLng,
        onResult: (markers: List<MarkerEntity>) -> Unit
    ) {
        coroutineScope.launch {
            markerDao().deleteByLocation(latLng.latitude, latLng.longitude)
            onResult(markerDao().getAll())
        }
    }

    /**
     * Returns all markers to the callback fun
     */
    fun getAllMarkers(
        coroutineScope: CoroutineScope,
        onResult: (markers: List<MarkerEntity>) -> Unit
    ) {
        coroutineScope.launch {
            onResult(markerDao().getAll())
        }
    }

    companion object : SingletonHolder<TiamatDatabase, Context>({
        Room.databaseBuilder(
            it.applicationContext,
            TiamatDatabase::class.java,
            "tiamat_db"
        ).build()
    })
}