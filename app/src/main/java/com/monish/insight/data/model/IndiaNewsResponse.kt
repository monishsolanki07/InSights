package com.monish.insight.data.model

import com.google.gson.annotations.SerializedName

// The top-level API response
data class IndiaNewsResponse(
    val top_news: List<TopNewsItem>? = null
)

data class TopNewsItem(
    val news: List<IndiaArticle>? = null
)

data class IndiaArticle(
    val id: Long?,
    val title: String?,
    val text: String?,
    val url: String?,
    val image: String?,
    val publish_date: String?
)
