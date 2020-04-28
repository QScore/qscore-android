package com.berd.qscore.features.leaderboard

import androidx.recyclerview.widget.RecyclerView
import com.berd.qscore.databinding.RowSearchUserBinding
import com.berd.qscore.features.shared.api.models.QUser

class LeaderboardViewHolder(private val binding: RowSearchUserBinding) : RecyclerView.ViewHolder(binding.root) {

    fun populateFrom(user: QUser) = with(binding) {
        username.text = user.username
    }
}
