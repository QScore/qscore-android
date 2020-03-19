package com.berd.qscore.features.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.berd.qscore.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private val binding: ActivitySignupBinding by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}