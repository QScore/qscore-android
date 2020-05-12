package com.berd.qscore.features.shared.activity

import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.berd.qscore.features.shared.viewmodel.RxViewModelWithState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

abstract class BaseFragmentWithState : Fragment() {

    protected val compositeDisposable = CompositeDisposable()

    protected inline fun <reified A : Any, reified S : Parcelable> RxViewModelWithState<A, S>.observeActions(
        crossinline actionListener: (A) -> Unit = {}
    ) {
        actionsObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
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
