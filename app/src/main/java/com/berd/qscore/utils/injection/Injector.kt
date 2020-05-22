package com.berd.qscore.utils.injection

import android.content.Context
import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.utils.geofence.GeofenceHelper
import com.berd.qscore.utils.location.LocationHelper
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.location.GeofencingClient
import com.google.firebase.auth.FirebaseAuth

interface InjectorInterface {
    val appContext: Context
    val firebaseAuth: FirebaseAuth
    val fbLoginmanager: LoginManager
    val googleSignInClient: GoogleSignInClient
    val geofenceHelper: GeofenceHelper
    val geofencingClient: GeofencingClient
    val userRepository: UserRepository
    val api: Api
    val locationHelper: LocationHelper
}

data class InjectorImpl(
    override val appContext: Context,
    override val firebaseAuth: FirebaseAuth,
    override val fbLoginmanager: LoginManager,
    override val googleSignInClient: GoogleSignInClient,
    override val geofenceHelper: GeofenceHelper,
    override val userRepository: UserRepository,
    override val geofencingClient: GeofencingClient,
    override val api: Api,
    override val locationHelper: LocationHelper
) : InjectorInterface

object Injector : InjectorInterface {
    private lateinit var impl: InjectorInterface

    override val appContext by lazy { impl.appContext }
    override val firebaseAuth by lazy { impl.firebaseAuth }
    override val fbLoginmanager by lazy { impl.fbLoginmanager }
    override val googleSignInClient by lazy { impl.googleSignInClient }
    override val geofenceHelper by lazy { impl.geofenceHelper }
    override val geofencingClient by lazy { impl.geofencingClient }
    override val userRepository by lazy { impl.userRepository }
    override val api by lazy { impl.api }
    override val locationHelper by lazy { impl.locationHelper }

    fun initialize(impl: InjectorImpl) {
        Injector.impl = impl
    }
}
