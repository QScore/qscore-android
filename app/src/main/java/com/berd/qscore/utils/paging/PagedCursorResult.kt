package com.berd.qscore.utils.paging

data class PagedCursorResult<T>(val items: List<T>, val nextCursor: String?)
