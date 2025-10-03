// file: data/remote/NewsDataApiService.kt
package com.monish.insight.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

data class NewsDataResponse(
    val status: String?,
    val totalResults: Int?,
    val results: List<NewsDataArticle>?,
    val nextPage: String?
)

data class NewsDataArticle(
    val article_id: String?,
    val title: String?,
    val link: String?,
    val keywords: List<String>?,
    val creator: List<String>?,
    val description: String?,       // summary
    val content: String?,           // full content if provided
    val image_url: String?,         // some responses use image_url or image
    val pubDate: String?            // publication date naming varies
)

interface NewsDataApiService {
    @GET("latest")
    suspend fun getLatest(
        @Query("apikey") apikey: String,
        @Query("size") size: Int = 10,
        @Query("country") country: String? = "au,us",
        @Query("language") language: String? = "en",
        @Query("page") page: String? = null // pagination token
    ): NewsDataResponse
}

