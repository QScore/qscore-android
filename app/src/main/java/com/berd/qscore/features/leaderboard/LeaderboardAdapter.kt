package com.berd.qscore.features.leaderboard

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.berd.qscore.databinding.RowLeaderboardScoreBinding
import com.berd.qscore.features.shared.api.models.QLeaderboardScore
import splitties.systemservices.layoutInflater

class LeaderboardAdapter : ListAdapter<QLeaderboardScore, LeaderboardViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = RowLeaderboardScoreBinding.inflate(parent.layoutInflater)
        binding.root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.populateFrom(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<QLeaderboardScore>() {
        override fun areItemsTheSame(oldItem: QLeaderboardScore, newItem: QLeaderboardScore): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: QLeaderboardScore, newItem: QLeaderboardScore): Boolean {
            return oldItem == newItem
        }
    }

}
