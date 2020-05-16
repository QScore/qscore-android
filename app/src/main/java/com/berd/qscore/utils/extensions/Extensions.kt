package com.berd.qscore.utils.extensions

import android.animation.ValueAnimator
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
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.BaseInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.ImageView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.berd.qscore.R
import com.berd.qscore.utils.analytics.Analytics
import com.berd.qscore.utils.location.LatLngPair
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.bumptech.glide.integration.webp.decoder.WebpDrawableTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.ui.views.GiphyDialogFragment
import java.util.*
import java.util.regex.Pattern
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

    fun getColorDrawable(photoColor: String) = if (photoColor.isEmpty()) {
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

fun Context.showProgressDialog(message: String): ProgressDialog =
    ProgressDialog.show(this, null, message)

@Suppress("UNCHECKED_CAST")
inline fun <reified T : ViewModel> FragmentActivity.createViewModel(crossinline initializer: (SavedStateHandle) -> T): T {
    val factory = object : AbstractSavedStateViewModelFactory(this, null) {
        override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T =
            initializer(handle) as T
    }
    return ViewModelProvider(this, factory).get(T::class.java)
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : ViewModel> Fragment.createViewModel(crossinline initializer: (SavedStateHandle) -> T): T {
    val factory = object : AbstractSavedStateViewModelFactory(this, null) {
        override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T =
            initializer(handle) as T
    }
    return ViewModelProvider(this, factory).get(T::class.java)
}

fun Activity.setStatusbarColor(colorResId: Int) {
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = ContextCompat.getColor(this, colorResId)
}

fun Fragment.setStatusbarColor(colorResId: Int) = activity?.setStatusbarColor(colorResId)

fun View.setBackgroundColorResId(colorResId: Int) {
    val color = ContextCompat.getColor(context, colorResId)
    setBackgroundColor(color)
}

fun Context.getColor(colorResId: Int): Int {
    return ContextCompat.getColor(this, colorResId)
}

fun Fragment.getColor(colorResId: Int): Int {
    return activity?.let { ContextCompat.getColor(it, colorResId) } ?: 0
}

fun Fragment.setScreenName(screenName: String) {
    activity?.let { Analytics.setCurrentScreen(it, screenName) }
}

fun Activity.setScreenName(screenName: String) {
    Analytics.setCurrentScreen(this, screenName)
}


fun GiphyDialogFragment.onGifSelected(callback: (Media) -> Unit) {
    this.gifSelectionListener = object : GiphyDialogFragment.GifSelectionListener {
        override fun didSearchTerm(term: String) {
        }

        override fun onDismissed() {
        }

        override fun onGifSelected(media: Media) {
            callback(media)
        }
    }
}

fun View.fadeIn(
    duration: Long = 300L,
    interpolator: BaseInterpolator = AccelerateInterpolator()
) {
    val animation = AlphaAnimation(0f, 1f).apply {
        this.interpolator = interpolator //add this
        this.duration = duration
    }
    this.startAnimation(animation)
}

fun View.fadeOut(
    duration: Long = 300L,
    interpolator: BaseInterpolator = AccelerateInterpolator()
) {
    val animation = AlphaAnimation(1f, 0f).apply {
        this.interpolator = interpolator //add this
        this.duration = duration
    }
    this.startAnimation(animation)
}

fun View.animateViewHeight(
    finalValue: Int,
    startValue: Int = measuredHeight,
    duration: Long = 300L,
    interpolator: BaseInterpolator = DecelerateInterpolator(),
    doOnStart: View.() -> Unit = {},
    doOnEnd: View.() -> Unit = {}
) {
    ValueAnimator.ofInt(startValue, finalValue).let {
        it.duration = duration
        it.interpolator = interpolator
        it.addUpdateListener {
            val animatedValue = it.animatedValue as Int
            val layoutParams = layoutParams
            layoutParams.height = animatedValue
            this.layoutParams = layoutParams
        }
        it.doOnStart { doOnStart(this) }
        it.doOnEnd { doOnEnd(this) }
        it.start()
    }
}

fun Context.dip(value: Int): Int = (value * resources.displayMetrics.density).toInt()
fun Context.dip(value: Float): Int = (value * resources.displayMetrics.density).toInt()
fun Fragment.dip(value: Int): Int = (value * resources.displayMetrics.density).toInt()
fun Fragment.dip(value: Float): Int = (value * resources.displayMetrics.density).toInt()
