package com.berd.qscore.features.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserState
import com.amazonaws.mobile.client.UserStateDetails
import com.berd.qscore.R
import splitties.toast.toast
import timber.log.Timber

class EZLoginActivity() : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ez_login)
        startEasyLogin()
    }

    private fun startEasyLogin() {
        AWSMobileClient.getInstance().showSignIn(this, object : Callback<UserStateDetails> {
            override fun onResult(result: UserStateDetails) {
                when (result.userState) {
                    UserState.SIGNED_IN -> Timber.d(">>Signed in")
                    UserState.SIGNED_OUT -> Timber.d(">>Signed out")
                    else -> AWSMobileClient.getInstance().signOut()
                }
            }

            override fun onError(e: Exception) {
                Timber.d(">>Error signing in: $e")
                toast("Error signing in")
            }
        })
    }
}