package com.berd.qscore.features.main.bottomnav

import androidx.fragment.app.Fragment
import com.berd.qscore.R
import com.berd.qscore.features.score.ScoreFragment
import com.berd.qscore.features.search.SearchFragment

enum class BottomTab(val fragment: Fragment) {
    ME(ScoreFragment.newInstance()),
    SEARCH(SearchFragment.newInstance()),
    LEADERBOARD(ScoreFragment.newInstance());

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
