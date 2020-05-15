package com.berd.qscore.features.leaderboard

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import com.berd.qscore.databinding.RowLeaderboardScoreBinding
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.utils.extensions.loadAvatar
import com.berd.qscore.utils.extensions.loadDefaultAvatar

class LeaderboardViewHolder(
    private val binding: RowLeaderboardScoreBinding,
    private val clickListener: LeaderboardClickListener
) : RecyclerView.ViewHolder(binding.root) {

    @SuppressLint("SetTextI18n")
    fun populateFrom(user: QUser) = with(binding) {
        usernameText.text = user.username
        rankText.text = "#${user.rank.toString()}"
        scoreText.text = user.allTimeScore
        user.avatar?.let {
            avatarImage.loadAvatar(it)
        } ?: avatarImage.loadDefaultAvatar(user.userId)
        mainLayout.setOnClickListener { clickListener(user.userId) }
    }
}
