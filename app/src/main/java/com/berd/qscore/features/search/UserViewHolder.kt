package com.berd.qscore.features.search

import androidx.recyclerview.widget.RecyclerView
import com.berd.qscore.databinding.RowUserBinding
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.shared.user.UserClickListener
import com.berd.qscore.utils.extensions.loadAvatar
import com.berd.qscore.utils.extensions.loadDefaultAvatar

class UserViewHolder(private val binding: RowUserBinding, private val clickListener: UserClickListener) :
    RecyclerView.ViewHolder(binding.root) {

    fun populateFrom(user: QUser) = with(binding) {
        username.text = user.username
        user.avatar?.let {
            avatar.loadAvatar(it)
        } ?: avatar.loadDefaultAvatar(user.userId)
        mainLayout.setOnClickListener { clickListener(user) }
    }
}
