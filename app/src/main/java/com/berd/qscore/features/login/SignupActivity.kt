package com.berd.qscore.features.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.berd.qscore.databinding.ActivitySignupBinding
import splitties.activities.start

class SignupActivity : AppCompatActivity() {

    private val viewModel by viewModels<SignupViewModel>()

    private val binding: ActivitySignupBinding by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupViews()
    }

    private fun setupViews() = binding.apply {
        haveAccount.setOnClickListener {
            start<LoginActivity> {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }
            finish()
        }

        signupButton.setOnClickListener {
            val email = email.text.toString()
            val password = password.text.toString()
            viewModel.signup(email, password)
        }

    }
}