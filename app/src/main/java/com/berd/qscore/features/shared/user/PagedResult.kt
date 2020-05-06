package com.berd.qscore.features.shared.user

data class PagedResult<T>(val items: List<T>, val nextCursor: String?)
