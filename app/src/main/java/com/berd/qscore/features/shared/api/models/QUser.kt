package com.berd.qscore.features.shared.api.models

data class QUser(
    val userId: String,
    val username: String,
    val score: Double,
    val rank: String?,
    val avatar: String?,
    val allTimeScore: String,
    val followerCount: Int,
    val followingCount: Int,
    val isCurrentUserFollowing: Boolean = false
)
