package com.monish.insight.data.mapper

import com.monish.insight.data.model.Article
import com.monish.insight.data.model.IndiaArticle

fun IndiaArticle.toArticle(): Article {
    return Article(
        title = this.title,
        description = this.text,
        url = this.url,
        urlToImage = this.image,
        publishedAt = this.publish_date
    )
}
