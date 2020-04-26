package com.berd.qscore.features.friends

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityFriendsBinding
import com.berd.qscore.features.friends.AddFriendsViewModel.State.*
import com.berd.qscore.features.shared.activity.BaseActivity
import com.berd.qscore.utils.extensions.invisible
import com.berd.qscore.utils.extensions.onChangeDebounce
import com.berd.qscore.utils.extensions.visible

class AddFriendsActivity : BaseActivity() {
    private val viewModel by viewModels<AddFriendsViewModel>()

    private val binding: ActivityFriendsBinding by lazy {
        ActivityFriendsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)
        observeEvents()
        setupViews()
    }

    private fun observeEvents() {
        viewModel.observeState {
            when (it) {
                InProgress -> handleInProgress()
                Ready -> handleReady()
                ConnectionError -> handleConnectionError()
                FieldsUpdated -> handleFieldsUpdated()
            }
        }
    }

    private fun handleConnectionError() = binding.apply {
        errorText.text = getString(R.string.connection_error)
        errorText.visible()
    }

    private fun handleReady() = binding.apply {
        errorText.invisible()
    }

    private fun handleInProgress() = binding.apply {
        errorText.invisible()
    }

    private fun handleFieldsUpdated() = binding.apply {
        errorText.invisible()
    }

    private fun setupViews() = binding.apply {
        username.onChangeDebounce(100) { viewModel.onFieldsUpdated(username.text.toString()) }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }
}
