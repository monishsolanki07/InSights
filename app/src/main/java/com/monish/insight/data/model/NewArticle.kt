package com.monish.insight.data.model

// file: data/model/Article.kt
import kotlinx.serialization.Serializable


@Serializable
data class NewArticle(
    val article_id: String,
    val title: String,
    val link: String,
    val description: String,
    val content: String,
    val pubDate: String,
    val image_url: String?,
    val video_url: String?,
    val source_name: String
)
