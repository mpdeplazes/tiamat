package com.mpdeplazes.tiamat.service

import android.app.Activity
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.mpdeplazes.tiamat.db.TiamatDatabase
import com.mpdeplazes.tiamat.db.entity.MarkerEntity
import com.mpdeplazes.tiamat.util.clearAndRemoveFromMap
import com.mpdeplazes.tiamat.util.toLatLng
import com.mpdeplazes.tiamat.util.toMarkerOptions
import kotlinx.coroutines.CoroutineScope

/**
 * Handles Map Events and Logic
 */
class MapService(
    private val activity: Activity,
    private val coroutineScope: CoroutineScope,
    private val googleMap: GoogleMap
) {
    // todo this seems like a candidate for livedata if I get around to looking into it
    private val currentMarkers = mutableMapOf<MarkerEntity, Marker>()
    private val markerDao = TiamatDatabase.getInstance(activity)

    /**
     * Adds latLng as a marker
     */
    fun addMarker(latLng: LatLng) {
        val newMarker = MarkerEntity(
            latitude = latLng.latitude,
            longitude = latLng.longitude
        )

        markerDao.createMarkers(coroutineScope, listOf(newMarker)) {
                markers -> replaceMarkers(markers)
        }
    }

    /**
     * remove a marker via it's latLng
     */
    fun removeMarker(latLng: LatLng) {
        markerDao.deleteMarkerByLocation(coroutineScope, latLng) {
            replaceMarkers(it)
        }
    }

    /**
     * Moves map to a given location with given zoom
     */
    fun moveMapToLocation(latLng: LatLng, zoom: Float) {
        activity.runOnUiThread {
            CameraPosition
                .Builder()
                .target(latLng)
                .zoom(zoom)
                .build()
                .let {
                    googleMap.animateCamera(
                        CameraUpdateFactory.newCameraPosition(it),
                        1500,
                        null
                    )
                }
        }
    }

    /**
     * Updates the current markers to match the db
     */
    fun replaceMarkers(markers: List<MarkerEntity>) {
        activity.runOnUiThread {

            //todo this seems better than replacing the whole list, must be a faster way still

            // remove markers not in the new list
            currentMarkers.filterKeys { !markers.contains(it) }
                .forEach {
                    it.value.remove()
                    currentMarkers.remove(it.key)
                }

            // add markers not in the current list
            markers.filter { !currentMarkers.keys.contains(it) }
                .forEach { markerEntity ->
                    googleMap.addMarker(markerEntity.toMarkerOptions())
                        .also { marker -> currentMarkers[markerEntity] = marker}
                }
        }
    }

    /**
     * Gets all the markers in the db and shows them
     */
    fun showAllMarkers() {
        markerDao.getAllMarkers(coroutineScope) {
            replaceMarkers(it)
        }
    }

    companion object {
        /**
         * This is more to learn how the geocoder works, I don't imagine Montana's location
         * is going to change any time soon.
         */
        fun getMontanaLocation(context: Context): LatLng {
            val addresses = Geocoder(context).getFromLocationName("montana", 1)
            return if (addresses.isNotEmpty()) {
                addresses[0].toLatLng()
            } else {
                LatLng(46.88, -110.36)
            }
        }
    }
}