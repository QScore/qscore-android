package com.berd.qscore.features.shared.api.models

data class QLeaderboardScore(
    val userId: String,
    val username: String,
    val avatar: String?,
    val score: String,
    val rank: Int
)
