package com.berd.qscore.utils.rx

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class RxEventSender<T> {
    private val bufferedEvents = arrayListOf<T>()

    private val publishSubject = PublishSubject.create<T>()

    val observable: Observable<T> = publishSubject
        .mergeWith(Observable.fromIterable(bufferedEvents))
        .doOnDispose {
            bufferedEvents.clear()
        }

    fun send(event: T) {
        if (publishSubject.hasObservers()) {
            publishSubject.onNext(event)
        } else {
            bufferedEvents.add(event)
        }
    }
}
