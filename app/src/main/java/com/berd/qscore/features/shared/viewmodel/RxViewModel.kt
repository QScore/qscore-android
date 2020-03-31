package com.berd.qscore.features.shared.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.berd.qscore.utils.rx.RxEventSender

abstract class RxViewModel<A, S> : ViewModel() {

    private val _actions = RxEventSender<A>()
    val actionsObservable = _actions.observable

    private val _state = MutableLiveData<S>()
    val stateLiveData = _state as LiveData<S>

    protected fun action(action: A) = _actions.send(action)

    protected var state: S?
        get() = _state.value
        set(value) = _state.postValue(value)
}
