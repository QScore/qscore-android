package com.berd.qscore.features.shared.viewmodel

import androidx.lifecycle.ViewModel
import com.berd.qscore.utils.rx.RxEventSender
import io.reactivex.disposables.CompositeDisposable


abstract class RxViewModel<A> : ViewModel() {

    protected val compositeDisposable = CompositeDisposable()

    protected val actionSender = RxEventSender<A>()
    val actionsObservable = actionSender.observable

    protected open fun action(action: A) = actionSender.send(action)

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

}
