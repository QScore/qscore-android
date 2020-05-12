package com.berd.qscore.features.shared.activity

import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import com.berd.qscore.features.shared.viewmodel.RxViewModelWithState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

abstract class BaseActivityWithState : AppCompatActivity() {

    protected val compositeDisposable = CompositeDisposable()

    protected inline fun <reified A : Any, reified S : Parcelable> RxViewModelWithState<A, S>.observeActions(
        crossinline actionListener: (A) -> Unit = {}
    ) {
        actionsObservable.subscribeBy(onNext = {
            actionListener(it)
        }, onError = {
            Timber.e("Unable to listen for actions: $it")
        }).addTo(compositeDisposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
