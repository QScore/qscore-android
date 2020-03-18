package com.berd.qscore.features.score

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.berd.qscore.features.geofence.GeofenceIntentService
import com.berd.qscore.features.score.ScoreViewModel.State.Away
import com.berd.qscore.features.score.ScoreViewModel.State.Home
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

class ScoreViewModel : ViewModel() {

    sealed class State {
        object StartingUp : State()
        object Home : State()
        object Away : State()
    }

    private val compositeDisposable = CompositeDisposable()

    private val _viewState = MutableLiveData<State>()
    val viewState = _viewState as LiveData<State>

    fun onCreate() {
        GeofenceIntentService.events.subscribeBy(onNext = {
            handleGeofenceEvent(it)
        }, onError = {
            Timber.e("Unable to handle geofence event: $it")
        }).addTo(compositeDisposable)
    }

    private fun handleGeofenceEvent(it: GeofenceIntentService.Event) = when (it) {
        GeofenceIntentService.Event.Entered -> handleGeofenceEnter()
        GeofenceIntentService.Event.Exited -> handleGeofenceExit()
    }

    private fun handleGeofenceEnter() {
        _viewState.postValue(Home)
    }

    private fun handleGeofenceExit() {
        _viewState.postValue(Away)
    }

    override fun onCleared() {
        compositeDisposable.clear()
    }
}