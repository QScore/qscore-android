package com.berd.qscore.utils.injection

import android.content.Context
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth

interface InjectorInterface {
    val appContext: Context
    val firebaseAuth: FirebaseAuth
    val fbLoginmanager: LoginManager
}

data class InjectorImpl(
    override val appContext: Context,
    override val firebaseAuth: FirebaseAuth,
    override val fbLoginmanager: LoginManager
) : InjectorInterface

object Injector : InjectorInterface {
    private lateinit var impl: InjectorInterface

    override val appContext: Context get() = impl.appContext
    override val firebaseAuth: FirebaseAuth get() = impl.firebaseAuth
    override val fbLoginmanager: LoginManager get() = impl.fbLoginmanager

    fun initialize(impl: InjectorImpl) {
        Injector.impl = impl
    }
}
