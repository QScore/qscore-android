package com.berd.qscore.utils.extensions

import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

@Suppress("UNCHECKED_CAST")
inline fun <reified T : ViewModel> FragmentActivity.createViewModel(crossinline initializer: () -> T): T {
    val factory = object : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = initializer() as T
    }
    return ViewModelProvider(this, factory).get(T::class.java)
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : ViewModel> Fragment.createViewModel(crossinline initializer: () -> T): T {
    val factory = object : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = initializer() as T
    }
    return ViewModelProvider(this, factory).get(T::class.java)
}