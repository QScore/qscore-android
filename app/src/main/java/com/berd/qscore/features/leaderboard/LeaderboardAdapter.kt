package com.berd.qscore.features.leaderboard

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.berd.qscore.databinding.RowLeaderboardScoreBinding
import com.berd.qscore.features.shared.api.models.QUser
import splitties.systemservices.layoutInflater

typealias LeaderboardClickListener = (String) -> Unit

class LeaderboardAdapter(val clickListener: LeaderboardClickListener) : PagedListAdapter<QUser, LeaderboardViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = RowLeaderboardScoreBinding.inflate(parent.layoutInflater)
        binding.root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return LeaderboardViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
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
