package com.monish.insight.data.remote

import com.monish.insight.data.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    // 🔹 Top Headlines (US)
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("apiKey") apiKey: String
    ): NewsResponse

    // 🔹 BBC News
    @GET("top-headlines")
    suspend fun getBBCNews(
        @Query("sources") sources: String = "bbc-news",
        @Query("apiKey") apiKey: String
    ): NewsResponse

    // 🔹 Business (Germany)
    @GET("top-headlines")
    suspend fun getBusinessNewsGermany(
        @Query("country") country: String = "de",
        @Query("category") category: String = "business",
        @Query("apiKey") apiKey: String
    ): NewsResponse
}
