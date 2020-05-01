package com.berd.qscore.features.search

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.berd.qscore.R
import com.berd.qscore.databinding.RowSearchUserBinding
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.utils.extensions.loadAvatar
import com.bumptech.glide.Glide

class SearchViewHolder(private val binding: RowSearchUserBinding) : RecyclerView.ViewHolder(binding.root) {

    fun populateFrom(user: QUser) = with(binding) {
        username.text = user.username
        if (user.isCurrentUserFollowing) {
            addButton.backgroundTintList = ContextCompat.getColorStateList(addButton.context, R.color.colorPrimaryDark)
            addButton.text = addButton.resources.getString(R.string.remove)
        } else {
            addButton.backgroundTintList = ContextCompat.getColorStateList(addButton.context, R.color.light_gray)
            addButton.text = addButton.resources.getString(R.string.add)
        }
        user.avatar?.let {
            avatar.loadAvatar(it)
        } ?: Glide.with(avatar).load(R.drawable.circle).into(avatar)

    }
}
