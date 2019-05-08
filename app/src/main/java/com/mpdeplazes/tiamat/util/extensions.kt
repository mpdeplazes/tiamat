package com.mpdeplazes.tiamat.util

import com.google.android.gms.maps.model.Marker

fun MutableList<Marker>.removeAllAndUpDateMap() {
    // Remove themselves from the map
    this.forEach {
        it.remove()
    }

    // Empty the list
    this.clear()
}