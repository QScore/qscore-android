package com.berd.qscore

import android.app.Application
import com.berd.qscore.utils.injection.AppInjector
import com.berd.qscore.utils.injection.Injector

class QscoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Injector.initialize(AppInjector(this))
    }
}