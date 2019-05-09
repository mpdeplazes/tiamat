package com.mpdeplazes.tiamat.util

import android.location.Address
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mpdeplazes.tiamat.db.entity.MarkerEntity

fun MutableList<Marker>.clearAndRemoveFromMap() {
    // Remove themselves from the map
    this.forEach {
        it.remove()
    }

    // Empty the list
    this.clear()
}

fun <T> MutableMap<T, Marker>.clearAndRemoveFromMap() {
    // Remove themselves from the map
    this.forEach {
        it.value.remove()
    }

    // Empty the list
    this.clear()
}


fun MarkerEntity.toLatLng(): LatLng {
    return LatLng(
        this.latitude,
        this.longitude
    )
}

fun Location.toLatLng(): LatLng {
    return LatLng(
        this.latitude,
        this.longitude
    )
}

fun Address.toLatLng(): LatLng {
    return LatLng(
        this.latitude,
        this.longitude
    )
}

fun MarkerEntity.toMarkerOptions(): MarkerOptions {
    return MarkerOptions()
        .position(this.toLatLng())
        .title(this.id)
}