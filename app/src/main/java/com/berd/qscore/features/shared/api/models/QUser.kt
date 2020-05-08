package com.berd.qscore.features.shared.api.models

import com.berd.qscore.features.geofence.GeofenceStatus

data class QUser(
    val userId: String,
    val username: String,
    val score: Double,
    val rank: String?,
    val avatar: String?,
    val allTimeScore: String,
    val followerCount: Int,
    val followingCount: Int,
    val isCurrentUserFollowing: Boolean = false,
    val geofenceStatus: GeofenceStatus? = null
)
