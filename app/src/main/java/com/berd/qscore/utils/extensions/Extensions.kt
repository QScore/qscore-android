package com.berd.qscore.utils.extensions

import android.location.Location
import android.view.View
import com.berd.qscore.utils.location.LatLngPair

fun Location.toLatLngPair() = LatLngPair(latitude, longitude)

fun LatLngPair.asParam() = "$lat,$lng"

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.visible() {
    visibility = View.VISIBLE
}