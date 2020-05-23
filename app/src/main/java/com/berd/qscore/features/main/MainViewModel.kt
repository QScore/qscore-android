package com.berd.qscore.features.main

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.login.LoginManager
import com.berd.qscore.features.main.MainViewModel.MainAction
import com.berd.qscore.features.main.MainViewModel.MainAction.*
import com.berd.qscore.features.main.MainViewModel.MainState
import com.berd.qscore.features.main.bottomnav.BottomTab
import com.berd.qscore.features.shared.viewmodel.RxViewModelWithState
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch

class MainViewModel(handle: SavedStateHandle) : RxViewModelWithState<MainAction, MainState>(handle) {

    sealed class MainAction {
        class Initialize(val state: MainState) : MainAction()
        class ChangeTab(val tab: BottomTab) : MainAction()
        object LaunchLoginActivity : MainAction()
    }

    @Parcelize
    data class MainState(
        val selectedTab: BottomTab = BottomTab.ME
    ) : Parcelable

    override fun getInitialState() = MainState()

    override fun updateState(action: MainAction, state: MainState) =
        when (action) {
            is ChangeTab -> state.copy(selectedTab = action.tab)
            else -> state
        }

    fun onCreate() {
        action(Initialize(state))
    }

    fun onBottomTabSelected(itemId: Int) {
        val bottomTab = BottomTab.fromMenuItemId(itemId)
        action(ChangeTab(bottomTab))
    }
}
