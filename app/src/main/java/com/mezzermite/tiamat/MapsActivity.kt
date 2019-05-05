package com.mezzermite.tiamat

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mezzermite.tiamat.dialogs.InfoDialog

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var gMap: GoogleMap
    private lateinit var locationManager: LocationManager
    // todo where would this usually live, what values
    private val LOCATION_PERMISSION_REQUEST_CODE = 1337
    private val currentMarkers: MutableList<Marker> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
            .getMapAsync(this)

        locationManager =  getSystemService(LOCATION_SERVICE) as LocationManager
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap
        goToInitialMapPosition()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            permissions.forEachIndexed { i, permission ->
                // permission was denied
                if (permission == ACCESS_FINE_LOCATION && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // user didn't click never ask again, show dialog for why we want
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

    /**
     * Get current location and moves map position to that location. If permission not allowed, then
     * the center of Montana is used for location, and zoom is set to hold state.
     */
    private fun goToInitialMapPosition() {

        val allowedToGetLocation = ContextCompat.checkSelfPermission(
            this,
            ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // get last known location if permissions enabled else ask for permission
        val lastKnownLocation = if (allowedToGetLocation) {
            locationManager.getLastKnownLocation(GPS_PROVIDER)
        } else {
            askForLocationPermission()
            null
        }

        val latLng = lastKnownLocation
            ?.let { LatLng(it.latitude, it.longitude) }
            // default to montana, get it fancy or just default to hard coded
            ?: run {
                val geocoder = Geocoder(this)
                val addresses = geocoder.getFromLocationName("montana", 1)
                if (addresses.isNotEmpty()) {
                    addresses[0].let {
                        LatLng(it.latitude, it.longitude)
                    }
                } else {
                    LatLng(46.88, -110.36)
                }
            }

        // remove all markers
        removeAllMarkers()

        // Dynamic zoom title based on if we found location
        val zoom = if (lastKnownLocation != null) {
            // add a marker
            MarkerOptions()
                .position(latLng)
                .title("Your Current Location")
                .let { gMap.addMarker(it) }
                .let { currentMarkers.add(it) }
            15f
        } else {
            6f
        }

        // move to the marker with some zoom and animation
        CameraPosition.Builder()
            .target(latLng)
            .zoom(zoom)
            .build()
            .let {
                gMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(it),
                    2000,
                    null
                )
            }
    }

    private fun removeAllMarkers() {
        // Remove themselves from the map
        currentMarkers.forEach {
            it.remove()
        }

        // Empty the list
        currentMarkers.clear()
    }

    private fun askForLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }
}
