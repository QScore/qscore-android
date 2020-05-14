package com.berd.qscore.features.shared.activity

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import com.berd.qscore.features.shared.viewmodel.RxViewModelOld
import com.berd.qscore.utils.extensions.setScreenName
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

abstract class BaseFragment : Fragment() {

    protected val compositeDisposable = CompositeDisposable()

    protected inline fun <reified A : Any> RxViewModel<A>.observeActions(
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

    protected inline fun <reified A : Any, reified S : Any> RxViewModelOld<A, S>.observeState(
        crossinline stateListener: (S) -> Unit = {}
    ) {
        stateLiveData.observe(this@BaseFragment, Observer {
            stateListener(it)
        })
    }

    override fun onResume() {
        super.onResume()
        setScreenName(getScreenName())
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            setScreenName(getScreenName())
        }
    }

    abstract fun getScreenName(): String

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
