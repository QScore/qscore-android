package com.berd.qscore.features.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityUserBinding
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.user.UserFragment.ProfileType
import com.berd.qscore.utils.injection.Injector

class UserActivity : AppCompatActivity() {
    private val userRepository = Injector.userRepository

    private val binding: ActivityUserBinding by lazy {
        ActivityUserBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupFragment()
    }

    private fun setupFragment() {
        val userId = intent.extras?.getString(KEY_USER_ID) as String
        val profileType = if (userId == userRepository.currentUser?.userId) {
            ProfileType.CurrentUser
        } else {
            ProfileType.User(userId)
        }
        val fragment = UserFragment.newInstance(profileType)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    companion object {
        const val KEY_USER_ID = "KEY_USER_ID"

        fun newIntent(context: Context, userId: String): Intent {
            return Intent(context, UserActivity::class.java).apply {
                putExtra(KEY_USER_ID, userId)
            }
        }
    }
}
