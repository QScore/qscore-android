package com.berd.qscore.features.shared.user

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.berd.qscore.databinding.RowUserBinding
import com.berd.qscore.features.leaderboard.LeaderboardAdapter
import com.berd.qscore.features.search.UserViewHolder
import com.berd.qscore.features.shared.api.models.QUser
import splitties.systemservices.layoutInflater

typealias UserClickListener = (QUser) -> Unit

class UserAdapter(private val userClickListener: UserClickListener) : PagedListAdapter<QUser, UserViewHolder>(LeaderboardAdapter.DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = RowUserBinding.inflate(parent.layoutInflater)
        binding.root.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        return UserViewHolder(binding, userClickListener)
    }

    private fun handleClicked(item: QUser) {
        //Do something
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        getItem(position)?.let { holder.populateFrom(it) }
    }

    class DiffCallback : DiffUtil.ItemCallback<QUser>() {
        override fun areItemsTheSame(oldItem: QUser, newItem: QUser): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: QUser, newItem: QUser): Boolean {
            return oldItem == newItem
        }
    }
}
