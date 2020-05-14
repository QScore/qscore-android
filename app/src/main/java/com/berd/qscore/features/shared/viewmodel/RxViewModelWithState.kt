package com.berd.qscore.features.shared.viewmodel

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle

@Suppress("LeakingThis")
abstract class RxViewModelWithState<A, S : Parcelable>(private val handle: SavedStateHandle) : RxViewModel<A>() {

    companion object {
        const val KEY_STATE = "KEY_STATE"
    }

    protected var state = handle.get<S>(KEY_STATE) ?: getInitialState()
        private set

    protected abstract fun getInitialState(): S

    abstract fun updateState(action: A, state: S): S

    override fun action(action: A) {
        state = updateState(action, state)
        handle.set(KEY_STATE, state)
        actionSender.send(action)
    }
}
