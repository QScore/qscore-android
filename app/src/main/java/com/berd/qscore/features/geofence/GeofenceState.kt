package com.berd.qscore.features.geofence

sealed class GeofenceState(val message: String) {
    object Unknown : GeofenceState("You are home! (But I'm guessing)")
    object Home : GeofenceState("You are home!")
    object Away : GeofenceState("You are away!")
}