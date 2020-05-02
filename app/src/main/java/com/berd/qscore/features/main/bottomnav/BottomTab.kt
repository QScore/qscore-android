package com.berd.qscore.features.main.bottomnav

import com.berd.qscore.R
import com.berd.qscore.features.leaderboard.LeaderboardFragment
import com.berd.qscore.features.search.SearchFragment
import com.berd.qscore.features.user.UserFragment
import com.berd.qscore.features.user.UserFragment.ProfileType

enum class BottomTab() {
    ME,
    SEARCH,
    LEADERBOARD;

    fun buildFragment() = when (this) {
        ME -> UserFragment.newInstance(ProfileType.CurrentUser)
        SEARCH -> SearchFragment.newInstance()
        LEADERBOARD -> LeaderboardFragment.newInstance()
    }

    companion object {
        fun fromMenuItemId(itemId: Int): BottomTab {
            return when (itemId) {
                R.id.me -> ME
                R.id.search -> SEARCH
                R.id.leaderboards -> LEADERBOARD
                else -> throw IllegalArgumentException("No bottom tab found for item id: $itemId")
            }
        }
    }
}
