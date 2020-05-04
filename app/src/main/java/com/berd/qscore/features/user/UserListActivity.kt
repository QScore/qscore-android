package com.berd.qscore.features.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.berd.qscore.databinding.ActivityUserListBinding
import com.berd.qscore.features.shared.activity.BaseActivity
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.shared.user.UserAdapter
import com.berd.qscore.utils.extensions.createViewModel
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
        setupRecyclerView()
        observeViewModel()
        viewModel.onCreate()
    }

    private fun observeViewModel() {
        viewModel.observeState {
            when (it) {
//                is Ready -> handleReady(it.users)
            }
        }
    }

//    private fun handleReady(users: List<QUser>) {
//        userAdapter.submitList(users)
//    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = userAdapter
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
