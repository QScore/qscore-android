package com.berd.qscore.features.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityUserBinding
import com.berd.qscore.features.user.UserFragment.ProfileType
import timber.log.Timber

class UserActivity : AppCompatActivity() {

    private val binding: ActivityUserBinding by lazy {
        ActivityUserBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d(">>ON CREATE")
        setContentView(binding.root)
        setupToolbar()
        setupFragment()
    }

    private fun setupFragment() {
        Timber.d(">>SETTING UP FRAGMENT")
        val userId = intent.extras?.getString(KEY_USER_ID) as String
        val fragment = UserFragment.newInstance(ProfileType.User(userId))
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer2, fragment)
            .commit()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
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
