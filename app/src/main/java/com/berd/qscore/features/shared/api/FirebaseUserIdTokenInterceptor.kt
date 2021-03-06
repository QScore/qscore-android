package com.berd.qscore.features.shared.api

import com.berd.qscore.utils.injection.Injector
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.GetTokenResult
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber

class FirebaseUserIdTokenInterceptor : Interceptor {
    private val firebaseAuth by lazy {  Injector.firebaseAuth }

    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val request: Request = chain.request()
            val user = checkNotNull(firebaseAuth.currentUser) { "User is not logged in" }
            val task: Task<GetTokenResult> = user.getIdToken(true)
            val tokenResult = Tasks.await(task)
            val idToken = checkNotNull(tokenResult.token) { "idToken is null" }
            val modifiedRequest: Request = request.newBuilder()
                .addHeader("Authorization", "bearer $idToken")
                .build()
            return chain.proceed(modifiedRequest)
        } catch (e: Exception) {
            Timber.d("Unable to proceed as logged in user: $e")
            return chain.proceed(chain.request())
        }
    }
}
