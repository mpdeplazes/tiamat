package com.mpdeplazes.tiamat.activities

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.mpdeplazes.tiamat.R
import com.mpdeplazes.tiamat.db.TiamatDatabase
import com.mpdeplazes.tiamat.dialogs.InfoDialog
import com.mpdeplazes.tiamat.dialogs.YesNoDialog
import com.mpdeplazes.tiamat.service.MapService
import com.mpdeplazes.tiamat.util.toLatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var locationManager: LocationManager
    private lateinit var tiamatDatabase: TiamatDatabase
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var baseCoroutineJob: Job
    private lateinit var mapService: MapService
    // todo where would this usually live, what values
    private val LOCATION_PERMISSION_REQUEST_CODE = 1337

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
            .getMapAsync(this)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        tiamatDatabase = TiamatDatabase.getInstance(this)
        baseCoroutineJob = Job()
        coroutineScope = CoroutineScope(Dispatchers.IO + baseCoroutineJob)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setOnMapLongClickListener { latLng -> onMapLongClickHandler(latLng) }
        googleMap.setOnMarkerClickListener { marker -> onMarkerClickHandler(marker) }

        mapService = MapService(this, coroutineScope, googleMap)

        goToInitialMapPosition()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            permissions.forEachIndexed { i, permission ->
                // permission was denied
                if (permission == ACCESS_FINE_LOCATION && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // user didn't click never ask again, show explanation dialog
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        InfoDialog.makeAndShow(
                            context = this,
                            message = getString(R.string.please_allow_location)
                        ) { _, _ ->
                            askForLocationPermission()
                        }
                    }
                } else {
                    goToInitialMapPosition()
                }
            }
        }
    }

    private fun onMapLongClickHandler(latLng: LatLng) {
        mapService.addMarker(latLng)
    }

    private fun onMarkerClickHandler(marker: Marker): Boolean {
        val position = marker.position

        YesNoDialog.makeAndShow(
            context = this,
            // todo global string with dynamic parts
            message = "Would you like to remove the marker at latitude:${position.latitude} " +
                    "longitude:${position.longitude}"
        ) { _, which ->
            when (which) {
                YesNoDialog.YES -> mapService.removeMarker(position)
            }
        }

        // don't do normal animation
        return true
    }

    /**
     * Get current location and moves map position to that location. If permission not allowed, then
     * the center of Montana is used for location, and zoom is set to hold state.
     */
    private fun goToInitialMapPosition() {
        val lastKnownLocation = getLastKnownLocation()
        val initialMapPosition = lastKnownLocation ?: MapService.getMontanaLocation(this)

        // Dynamic zoom title based on if we found location
        val zoom = if (lastKnownLocation != null) 15f else 6f
        mapService.moveMapToLocation(initialMapPosition, zoom)
        mapService.showAllMarkers()
    }

    /**
     * Gets the last known location, or null if permission is not enabled.
     */
    private fun getLastKnownLocation(): LatLng? {
        val allowedToGetLocation = ContextCompat.checkSelfPermission(
            this,
            ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // get last known location if permissions enabled else ask for permission
        return if (allowedToGetLocation) {
            locationManager.getLastKnownLocation(GPS_PROVIDER).toLatLng()
        } else {
            // double check they don't want to use position
            askForLocationPermission()
            null
        }
    }

    private fun askForLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }
}
