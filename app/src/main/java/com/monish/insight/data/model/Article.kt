package com.monish.insight.data.model
import coil.compose.AsyncImage


data class Article(
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val publishedAt: String?
)


data class Source(
    val id: String?,
    val name: String?
)
