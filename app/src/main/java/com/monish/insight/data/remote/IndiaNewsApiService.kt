package com.monish.insight.data.remote

import com.monish.insight.data.model.IndiaNewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface IndiaNewsApiService {
    @GET("top-news")
    suspend fun getIndiaTopNews(
        @Query("api-key") apiKey: String,
        @Query("source-country") sourceCountry: String,
        @Query("language") language: String,
        @Query("date") date: String? = null,
        @Query("category") category: String? = null,
        @Query("q") query: String? = null
    ): IndiaNewsResponse


}
