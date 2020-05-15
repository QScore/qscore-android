package com.berd.qscore.features.shared.prefs

import com.berd.qscore.utils.extensions.asParam
import com.berd.qscore.utils.location.LatLngPair
import splitties.experimental.ExperimentalSplittiesApi
import splitties.preferences.Preferences

@OptIn(ExperimentalSplittiesApi::class)
object Prefs : Preferences("qscorePrefs") {
    private var locationPref by StringPref("location", "")

    var userLocation: LatLngPair?
        get() {
            if (locationPref.isEmpty()) {
                return null
            }
            val (lat, lng) = locationPref.split(",")
            return LatLngPair(lat.toDouble(), lng.toDouble())
        }
        set(value) {
            locationPref = value?.asParam() ?: ""
        }
}
