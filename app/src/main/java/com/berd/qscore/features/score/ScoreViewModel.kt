package com.berd.qscore.features.score

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.geofence.GeofenceIntentService
import com.berd.qscore.features.geofence.GeofenceState
import com.berd.qscore.features.geofence.GeofenceState.*
import com.berd.qscore.features.login.LoginManager
import com.berd.qscore.features.score.ScoreViewModel.Action.*
import com.berd.qscore.utils.rx.RxEventSender
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.launch
import timber.log.Timber


class ScoreViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    sealed class Action {
        object LaunchLoginActivity : Action()
    }

    private val _actions = RxEventSender<Action>()
    val actions = _actions.observable

    private val _viewState = MutableLiveData<GeofenceState>()
    val viewState = _viewState as LiveData<GeofenceState>

    fun onCreate() {
        subscribeToGeofence()
        _viewState.postValue(Unknown)
    }

    fun onLogout() = viewModelScope.launch {
        LoginManager.logout()
        _actions.send(LaunchLoginActivity)
    }

    private fun subscribeToGeofence() {
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
        //User is home
        _viewState.postValue(Home)
    }

    private fun handleGeofenceExit() {
        _viewState.postValue(Away)
    }

    override fun onCleared() {
        compositeDisposable.clear()
    }
}