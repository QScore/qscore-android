package com.berd.qscore.features.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityUserListBinding
import com.berd.qscore.features.shared.activity.BaseActivity
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.shared.user.UserAdapter
import com.berd.qscore.features.user.UserListViewModel.UserListAction.SubmitPagedList
import com.berd.qscore.features.user.UserListViewModel.UserListState.*
import com.berd.qscore.utils.extensions.createViewModel
import com.berd.qscore.utils.extensions.gone
import com.berd.qscore.utils.extensions.visible
import java.io.Serializable

class UserListActivity : BaseActivity() {

    private val viewModel by lazy {
        createViewModel {
            UserListViewModel(userId, listType)
        }
    }

    private val userId by lazy {
        intent.getStringExtra(KEY_USER_ID)
    }

    private val listType by lazy {
        intent.getSerializableExtra(KEY_LIST_TYPE) as UserListType
    }

    private val userAdapter by lazy {
        UserAdapter(::onUserClicked)
    }

    private fun onUserClicked(user: QUser) {
        val intent = UserActivity.newIntent(this, user.userId)
        startActivity(intent)
    }

    private val binding: ActivityUserListBinding by lazy {
        ActivityUserListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        viewModel.onCreate()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = when (listType) {
            UserListType.FOLLOWERS -> getString(R.string.followers)
            UserListType.FOLLOWED -> getString(R.string.following)
        }
    }

    override fun getScreenName() = when (listType) {
        UserListType.FOLLOWERS -> "followers"
        UserListType.FOLLOWED -> "following"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = userAdapter
    }

    private fun observeViewModel() {
        viewModel.observeActions {
            when (it) {
                is SubmitPagedList -> submitPagedList(it.pagedList)
            }
        }

        viewModel.observeState {
            when (it) {
                Loading -> handleLoading()
                Loaded -> handleLoaded()
                NoUsersFound -> handleNoUsersFound()
            }
        }
    }

    private fun submitPagedList(pagedList: PagedList<QUser>) {
        userAdapter.submitList(pagedList)
    }

    private fun handleNoUsersFound() {
        binding.emptyText.visible()
        binding.progress.gone()
        binding.recyclerView.gone()
    }

    private fun handleLoaded() {
        binding.progress.gone()
        binding.emptyText.gone()
        binding.recyclerView.visible()
    }

    private fun handleLoading() {
        binding.progress.visible()
        binding.recyclerView.gone()
        binding.emptyText.gone()
    }

    enum class UserListType : Serializable {
        FOLLOWERS,
        FOLLOWED
    }

    companion object {
        const val KEY_USER_ID = "KEY_USER_ID"
        const val KEY_LIST_TYPE = "KEY_LIST_TYPE"
        fun newIntent(context: Context, userId: String, userListType: UserListType) =
            Intent(context, UserListActivity::class.java).apply {
                putExtra(KEY_USER_ID, userId)
                putExtra(KEY_LIST_TYPE, userListType)
            }
    }
}
