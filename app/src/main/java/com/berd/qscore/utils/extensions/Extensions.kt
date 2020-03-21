package com.berd.qscore.utils.extensions

import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.view.View
import androidx.core.app.ActivityCompat
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

fun Context.hasPermissions(vararg permissions: String) =
    permissions.all {
        ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

fun Context.showProgressDialog(message: String) =
     ProgressDialog.show(this, null, message)