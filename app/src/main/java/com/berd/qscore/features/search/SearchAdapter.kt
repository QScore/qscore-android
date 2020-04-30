package com.berd.qscore.features.search

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.berd.qscore.databinding.RowSearchUserBinding
import com.berd.qscore.features.leaderboard.LeaderboardViewHolder
import com.berd.qscore.features.shared.api.models.QUser
import splitties.systemservices.layoutInflater

class SearchAdapter : ListAdapter<QUser, LeaderboardViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = RowSearchUserBinding.inflate(parent.layoutInflater)
        binding.root.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.populateFrom(getItem(position))
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
