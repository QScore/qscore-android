package com.berd.qscore.utils.logging

import com.berd.qscore.BuildConfig
import timber.log.Timber

object LogHelper {

    fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
