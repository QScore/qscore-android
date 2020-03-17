package com.berd.qscore.utils.extensions

import android.location.Location
import com.berd.qscore.utils.location.LatLngPair

fun Location.toLatLngPair() = LatLngPair(latitude, longitude)

fun LatLngPair.asParam() = "$lat,$lng"