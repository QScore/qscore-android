package com.berd.qscore.utils.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.berd.qscore.R
import com.berd.qscore.utils.location.LatLngPair
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.bumptech.glide.integration.webp.decoder.WebpDrawableTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import java.util.*
import kotlin.math.abs

private val whiteColorDrawable = ColorDrawable().apply { color = Color.parseColor("#FFFFFF") }

fun ImageView.loadAvatar(url: String) {
    Glide.with(this) //.asBitmap()
        .load(url)
        .error(R.drawable.circle)
        .optionalTransform(CircleCrop())
        .optionalTransform(WebpDrawable::class.java, WebpDrawableTransformation(CircleCrop()))
        .into(this)
}

fun ImageView.loadDefaultAvatar(userId: String) {
    val selector = abs(userId.hashCode()) % 4
    val resId = when (selector) {
        0 -> R.drawable.rabbit
        1 -> R.drawable.bear
        2 -> R.drawable.monkey
        3 -> R.drawable.panda
        else -> R.drawable.panda
    }
    Glide.with(this) //.asBitmap()
        .load(resId)
        .circleCrop()
        .into(this)
}

@SuppressLint("CheckResult")
fun ImageView.loadUrl(
    url: String,
    photoColor: String? = null,
    override: Int? = null,
    errorResId: Int? = null,
    skipCache: Boolean = false,
    placeHolderResId: Int? = null,
    placeHolderDrawable: Drawable? = null,
    cacheKey: String? = null,
    shouldTransition: Boolean = true,
    dontAnimate: Boolean = false,
    dontTransform: Boolean = false,
    onlyRetrieveFromCache: Boolean = false,
    customOptions: RequestOptions? = null
) {

    fun getColorDrawable(photoColor: String) = if (photoColor.isNullOrEmpty()) {
        whiteColorDrawable
    } else {
        ColorDrawable().apply {
            this.color = try {
                Color.parseColor("#$photoColor")
            } catch (e: IllegalArgumentException) {
                Color.parseColor("#FFFFFF")
            }
        }
    }

    if (!context.isValid()) {
        return
    }

    if (url.startsWith("content")) {
        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .onlyRetrieveFromCache(onlyRetrieveFromCache)
            .into(this)
    } else {
        val requestOptions = RequestOptions().apply {
            if (photoColor != null) placeholder(getColorDrawable(photoColor))
            if (override != null) override(override)
            skipMemoryCache(skipCache)
            if (skipCache) diskCacheStrategy(DiskCacheStrategy.NONE)
            if (cacheKey != null) signature(ObjectKey(cacheKey))
            if (errorResId != null) error(errorResId)
            if (placeHolderResId != null) placeholder(placeHolderResId)
            if (placeHolderDrawable != null) placeholder(placeHolderDrawable)
            if (dontAnimate) dontAnimate()
            if (dontTransform) dontTransform()
        }

        Glide.with(this)
            .load(url)
            .apply { if (shouldTransition) transition(DrawableTransitionOptions.withCrossFade(100)) }
            .apply(requestOptions)
            .apply { if (customOptions != null) apply(customOptions) }
            .into(this)
    }
}

fun Context.isValid(): Boolean {
    if (this is Activity) {
        return !isDestroyed && !isFinishing
    }
    return true
}

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

fun Float.dpToPixels(dm: DisplayMetrics): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, dm)
}

fun Float.spToPixels(dm: DisplayMetrics): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, dm)
}

fun EditText.onChangeDebounce(delay: Long, cb: () -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        private var timer = Timer()

        override fun afterTextChanged(s: Editable?) {
            timer.cancel()
            timer = Timer()
            timer.schedule(
                object : TimerTask() {
                    override fun run() {
                        cb()
                    }
                },
                delay   //milliseconds
            )
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

fun EditText.onChange(cb: () -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            cb()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
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
