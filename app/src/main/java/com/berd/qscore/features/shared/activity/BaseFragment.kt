package com.berd.qscore.features.shared.activity

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

abstract class BaseFragment : Fragment() {

    protected val compositeDisposable = CompositeDisposable()

    protected inline fun <reified A : Any, reified S : Any> RxViewModel<A, S>.observeActions(
        crossinline actionListener: (A) -> Unit = {}
    ) {
        actionsObservable.subscribeBy(onNext = {
            actionListener(it)
        }, onError = {
            Timber.e("Unable to listen for actions: $it")
        }).addTo(compositeDisposable)
    }

    protected inline fun <reified A : Any, reified S : Any> RxViewModel<A, S>.observeState(
        crossinline stateListener: (S) -> Unit = {}
    ) {
        stateLiveData.observe(this@BaseFragment, Observer {
            stateListener(it)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
