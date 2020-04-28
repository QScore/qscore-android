package com.berd.qscore.features.leaderboard

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.berd.qscore.databinding.RowSearchUserBinding
import com.berd.qscore.features.shared.api.models.QUser
import splitties.systemservices.layoutInflater

class LeaderboardAdapter : ListAdapter<QUser, LeaderboardViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = RowSearchUserBinding.inflate(parent.layoutInflater)
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
