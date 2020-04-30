package com.berd.qscore.features.leaderboard

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.berd.qscore.R
import com.berd.qscore.databinding.RowSearchUserBinding
import com.berd.qscore.features.shared.api.models.QUser

class LeaderboardViewHolder(private val binding: RowSearchUserBinding) : RecyclerView.ViewHolder(binding.root) {

    fun populateFrom(user: QUser) = with(binding) {
        username.text = user.username
        if (user.isCurrentUserFollowing) {
            addButton.backgroundTintList = ContextCompat.getColorStateList(addButton.context, R.color.colorPrimaryDark)
            addButton.text = addButton.resources.getString(R.string.remove)
        } else {
            addButton.backgroundTintList = ContextCompat.getColorStateList(addButton.context, R.color.gray)
            addButton.text = addButton.resources.getString(R.string.add)
        }
    }
}
