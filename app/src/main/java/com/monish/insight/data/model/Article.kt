package com.monish.insight.data.model
import coil.compose.AsyncImage


data class Article(
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val publishedAt: String?
)

data class NewsDataHubResponse(
    val next_cursor: String?,
    val total_results: Int,
    val per_page: Int,
    val data: List<NewsDataHubArticle>
)

data class NewsDataHubArticle(
    val id: String,
    val title: String?,
    val description: String?,
    val article_link: String?,
    val media_url: String?,
    val pub_date: String?
)
