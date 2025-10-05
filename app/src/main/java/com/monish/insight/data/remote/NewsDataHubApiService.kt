package com.monish.insight.data.remote

import com.monish.insight.data.model.NewsDataHubResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NewsDataHubApiService {

    @GET("v1/top-news")
    suspend fun getTopNews(
        @Query("q") q: String,
        @Query("hours") hours: Int = 72,
        @Query("require_media") require_media: Boolean = true,
        @Query("language") language: String = "en",
        @Query("per_page") per_page: Int = 20,
        @Header("x-api-key") apiKey: String
    ): NewsDataHubResponse
}
