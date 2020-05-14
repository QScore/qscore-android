package com.berd.qscore.features.shared.activity

import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import com.berd.qscore.features.shared.viewmodel.RxViewModelOld
import com.berd.qscore.utils.extensions.setScreenName
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

abstract class BaseActivity : AppCompatActivity() {

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
        stateLiveData.observe(this@BaseActivity, Observer {
            stateListener(it)
        })
    }

    override fun onResume() {
        super.onResume()
        setScreenName(getScreenName())
    }

    abstract fun getScreenName(): String

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
