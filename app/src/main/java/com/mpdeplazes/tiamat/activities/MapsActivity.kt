package com.mpdeplazes.tiamat.activities

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mpdeplazes.tiamat.R
import com.mpdeplazes.tiamat.db.TiamatDatabase
import com.mpdeplazes.tiamat.db.entity.MarkerEntity
import com.mpdeplazes.tiamat.dialogs.InfoDialog
import com.mpdeplazes.tiamat.util.removeAllAndUpDateMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var gMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var tiamatDatabase: TiamatDatabase
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var baseCoroutineJob: Job
    // todo where would this usually live, what values
    private val LOCATION_PERMISSION_REQUEST_CODE = 1337
    private val currentMarkers: MutableList<Marker> = mutableListOf()

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
        gMap = googleMap
        gMap.setOnMapClickListener { latLng -> onMapClick(latLng) }
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

    private fun onMapClick(latLng: LatLng) {
        val mapsActivity = this
        coroutineScope.launch {
            TiamatDatabase.getInstance(mapsActivity).markerDao().insert(
                latLng.let {
                    MarkerEntity(
                        latitude = it.latitude,
                        longitude = it.longitude
                    )
                }
            )

            val stringBuilder = StringBuilder("index,latitude,longitude")

            // Get list of markers
            tiamatDatabase.markerDao().getAll()
                .forEachIndexed { index, markerEntity ->
                    if (index != 0) {
                        stringBuilder.append("\n")
                    }
                    stringBuilder.append("$index, ${markerEntity.latitude}, ${markerEntity.longitude}")
                }

            mapsActivity.runOnUiThread {
                Toast.makeText(
                    mapsActivity,
                    stringBuilder,
                    Toast.LENGTH_LONG
                ).show()
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

        // todo potentially give option of realtime vs last known
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
        currentMarkers.removeAllAndUpDateMap()

        // Dynamic zoom title based on if we found location
        val zoom = if (lastKnownLocation != null) {
            15f.also {
                // add a marker
                MarkerOptions()
                    .position(latLng)
                    .title("Your Current Location")
                    .let { gMap.addMarker(it) }
                    .let { currentMarkers.add(it) }
            }
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

    private fun askForLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }
}
