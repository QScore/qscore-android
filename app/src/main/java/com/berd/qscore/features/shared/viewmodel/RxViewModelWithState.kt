package com.berd.qscore.features.shared.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.berd.qscore.utils.rx.RxEventSender

typealias StateMutation<S> = S.() -> S

abstract class StateAction<S>(val mutation: StateMutation<S>? = null)

@Suppress("LeakingThis")
abstract class RxViewModelWithState<A : StateAction<S>, S>(private val handle: SavedStateHandle) : ViewModel() {

    companion object {
        const val KEY_STATE = "KEY_STATE"
    }

    protected var state = handle.get<S>(KEY_STATE) ?: getInitialState()
        private set

    abstract fun getInitialState(): S

    private val actionSender = RxEventSender<A>()
    val actionsObservable = actionSender.observable

    protected fun action(action: A) {
        state = action.mutation?.invoke(state) ?: state
        handle.set(KEY_STATE, state)
        actionSender.send(action)
    }
}
