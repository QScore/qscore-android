package com.berd.qscore.features.shared.viewmodel

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import timber.log.Timber
import kotlin.properties.Delegates
import kotlin.properties.ObservableProperty

@Suppress("LeakingThis")
abstract class RxViewModelWithState<A, S : Parcelable>(private val handle: SavedStateHandle) : RxViewModel<A>() {

    init {
        Timber.d(">>Initializng viewmodel: $this")
    }

    companion object {
        const val KEY_STATE = "KEY_STATE"
    }

    protected var state by Delegates.observable((handle.get<S>(KEY_STATE) ?: getInitialState())) { prop, old, new ->
        Timber.d(">>state changed: $new")
    }

    protected abstract fun getInitialState(): S

    abstract fun updateState(action: A, state: S): S

    override fun action(action: A) {
        state = updateState(action, state)
        handle.set(KEY_STATE, state)
        actionSender.send(action)
    }
}
