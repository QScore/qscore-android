package com.berd.qscore.utils.analytics

import android.app.Activity
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * Analytics utility methods.
 */
object Analytics {
    private val fbAnalytics = Firebase.analytics

    fun setCurrentScreen(activity: Activity, screenName: String) {
        fbAnalytics.setCurrentScreen(activity, screenName, null)
    }

    fun trackAvatarClicked() {
        fbAnalytics.logEvent("avatar_clicked")
    }

    fun trackSearch(searchQuery: String) {
        fbAnalytics.logEvent("search", Bundle().apply {
            putString("query", searchQuery)
        })
    }

    private fun FirebaseAnalytics.logEvent(s: String) {
        logEvent(s, null)
    }
}
