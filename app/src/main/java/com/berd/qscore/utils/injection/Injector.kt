package com.berd.qscore.utils.injection

import android.content.Context

interface InjectorImpl {
    val appContext: Context
}

class AppInjector(override val appContext: Context) : InjectorImpl

object Injector : InjectorImpl {
    private lateinit var impl: InjectorImpl
    override val appContext: Context get() = impl.appContext

    fun initialize(impl: InjectorImpl) {
        Injector.impl = impl
    }
}
