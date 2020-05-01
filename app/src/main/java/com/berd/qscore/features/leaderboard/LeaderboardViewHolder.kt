package com.berd.qscore.features.leaderboard

import androidx.recyclerview.widget.RecyclerView
import com.berd.qscore.R
import com.berd.qscore.databinding.RowLeaderboardScoreBinding
import com.berd.qscore.features.shared.api.models.QLeaderboardScore
import com.berd.qscore.utils.extensions.loadAvatar
import com.bumptech.glide.Glide

class LeaderboardViewHolder(private val binding: RowLeaderboardScoreBinding) : RecyclerView.ViewHolder(binding.root) {

    fun populateFrom(score: QLeaderboardScore) = with(binding) {
        usernameText.text = score.username
        rankText.text = "#" + score.rank.toString()
        scoreText.text = score.score
        score.avatar?.let {
            avatarImage.loadAvatar(it)
        } ?: Glide.with(avatarImage).load(R.drawable.circle).into(avatarImage)
    }
}
