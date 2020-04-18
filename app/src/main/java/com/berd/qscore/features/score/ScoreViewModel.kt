package com.berd.qscore.features.score

import android.os.CountDownTimer
import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.login.LoginManager
import com.berd.qscore.features.score.ScoreViewModel.ScoreAction
import com.berd.qscore.features.score.ScoreViewModel.ScoreAction.LaunchLoginActivity
import com.berd.qscore.features.score.ScoreViewModel.ScoreState
import com.berd.qscore.features.score.ScoreViewModel.ScoreState.Loading
import com.berd.qscore.features.score.ScoreViewModel.ScoreState.Ready
import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt


class ScoreViewModel : RxViewModel<ScoreAction, ScoreState>() {

    private val compositeDisposable = CompositeDisposable()

    sealed class ScoreAction {
        object LaunchLoginActivity : ScoreAction()
    }

    sealed class ScoreState {
        object Loading : ScoreState()
        class Ready(val score: Int) : ScoreState()
    }

    fun onCreate() {
        state = Loading
    }

    fun onResume() {
        viewModelScope.launch {
            try {
                val score = Api.getCurrentUser().score.roundToInt()
                state = Ready(score)
            } catch (e: Exception) {
                Timber.d("Error getting score: $e")
            }
        }
    }

    fun onLogout() = viewModelScope.launch {
        LoginManager.logout()
        action(LaunchLoginActivity)
    }

    override fun onCleared() {
        compositeDisposable.clear()
    }
}