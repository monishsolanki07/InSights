package com.monish.insight.data.mapper

import com.monish.insight.data.model.NewArticle
import com.monish.insight.data.remote.NewsDataArticle

fun NewsDataArticle.toArticle(): NewArticle {
    return NewArticle(
        article_id = this.article_id ?: "",
        title = this.title ?: "Untitled",
        link = this.link ?: "",
        description = this.description ?: "No description",
        content = this.content ?: this.description ?: "No content",
        pubDate = this.pubDate ?: "",
        image_url = this.image_url,
        video_url = null,  // If video info becomes available, handle here
        source_name = "Unknown" // Could be parsed from link or API if available
    )
}
