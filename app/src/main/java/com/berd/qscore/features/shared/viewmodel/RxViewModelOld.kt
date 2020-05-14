package com.berd.qscore.features.shared.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


abstract class RxViewModelOld<A, S> : RxViewModel<A>() {


    private val _state = MutableLiveData<S>()
    val stateLiveData = _state as LiveData<S>

    protected var state: S?
        get() = _state.value
        set(value) = _state.postValue(value)
}
