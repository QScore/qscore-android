package com.berd.qscore.utils.injection

import android.content.Context
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth

interface InjectorInterface {
    val appContext: Context
    val firebaseAuth: FirebaseAuth
    val fbLoginmanager: LoginManager
    val googleSignInClient: GoogleSignInClient
}

data class InjectorImpl(
    override val appContext: Context,
    override val firebaseAuth: FirebaseAuth,
    override val fbLoginmanager: LoginManager,
    override val googleSignInClient: GoogleSignInClient
) : InjectorInterface

object Injector : InjectorInterface {
    private lateinit var impl: InjectorInterface

    override val appContext: Context get() = impl.appContext
    override val firebaseAuth: FirebaseAuth get() = impl.firebaseAuth
    override val fbLoginmanager: LoginManager get() = impl.fbLoginmanager
    override val googleSignInClient: GoogleSignInClient get() = impl.googleSignInClient

    fun initialize(impl: InjectorImpl) {
        Injector.impl = impl
    }
}
