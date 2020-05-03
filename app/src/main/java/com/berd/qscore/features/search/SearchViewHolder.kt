package com.berd.qscore.features.search

import androidx.recyclerview.widget.RecyclerView
import com.berd.qscore.databinding.RowSearchUserBinding
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.utils.extensions.loadAvatar
import com.berd.qscore.utils.extensions.loadDefaultAvatar

class SearchViewHolder(private val binding: RowSearchUserBinding, private val clickListener: SearchClickListener) :
    RecyclerView.ViewHolder(binding.root) {

    fun populateFrom(user: QUser) = with(binding) {
        username.text = user.username
        user.avatar?.let {
            avatar.loadAvatar(it)
        } ?: avatar.loadDefaultAvatar()
        mainLayout.setOnClickListener { clickListener(user.userId) }
    }
}
