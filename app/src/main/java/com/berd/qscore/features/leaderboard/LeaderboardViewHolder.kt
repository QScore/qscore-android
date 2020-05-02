package com.berd.qscore.features.leaderboard

import androidx.recyclerview.widget.RecyclerView
import com.berd.qscore.R
import com.berd.qscore.databinding.RowLeaderboardScoreBinding
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.utils.extensions.loadAvatar
import com.bumptech.glide.Glide

class LeaderboardViewHolder(
    private val binding: RowLeaderboardScoreBinding,
    private val clickListener: LeaderboardClickListener
) : RecyclerView.ViewHolder(binding.root) {

    fun populateFrom(user: QUser) = with(binding) {
        usernameText.text = user.username
        rankText.text = "#" + user.rank.toString()
        scoreText.text = user.allTimeScore
        user.avatar?.let {
            avatarImage.loadAvatar(it)
        } ?: Glide.with(avatarImage).load(R.drawable.circle).into(avatarImage)
        mainLayout.setOnClickListener { clickListener(user.userId) }
    }
}
