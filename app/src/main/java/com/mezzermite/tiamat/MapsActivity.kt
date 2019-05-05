package com.mezzermite.tiamat
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.location.LocationManager
import android.location.LocationListener
import android.support.v4.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
            .getMapAsync(this)

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // Called when a new location is found by the network location provider.
                makeUseOfNewLocation(location)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            }

            override fun onProviderEnabled(provider: String) {
            }

            override fun onProviderDisabled(provider: String) {
            }
        }
    }

    fun makeUseOfNewLocation(location: Location) {
        // todo
        println("eyyy")
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val canLocate = (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)

        val lastKnownLocation = if (canLocate) {
            // todo keep up to date instead of single call?
//            locationManager.requestLocationUpdates(
//                LocationManager.GPS_PROVIDER,
//                0,
//                0f,
//                locationListener
//            )
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } else {
            //todo async call to get permission
            null
        }

        lastKnownLocation
            ?.let { LatLng(it.latitude, it.longitude) }
            ?: LatLng(-34.0, 151.0)
                .let {
                    // add marker and move map
                    mMap.addMarker(MarkerOptions().position(it).title("Marker in Sydney"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(it))
                }
    }

    // todo can you write a kotlin contract to prove we asked for permission
    private fun <T> doIfCreepingAllowed(function: () -> T?): T? {
        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            function()
        } else {
            null
        }
    }
}
