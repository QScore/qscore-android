package com.berd.qscore.utils.injection

import android.content.Context
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient

interface InjectorImpl {
    val appContext: Context
    val appSyncClient: AWSAppSyncClient
}

class AppInjector(
    override val appContext: Context,
    override val appSyncClient: AWSAppSyncClient
) : InjectorImpl

object Injector : InjectorImpl {
    private lateinit var impl: InjectorImpl
    override val appContext: Context get() = impl.appContext
    override val appSyncClient: AWSAppSyncClient get() = impl.appSyncClient

    fun initialize(impl: InjectorImpl) {
        Injector.impl = impl
    }
}
