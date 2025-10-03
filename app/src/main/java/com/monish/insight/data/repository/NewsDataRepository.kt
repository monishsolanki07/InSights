package com.monish.insight.data.repository

import com.monish.insight.data.mapper.toArticle
import com.monish.insight.data.model.NewArticle
import com.monish.insight.data.remote.NewsDataApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NewsDataRepository(
    private val api: NewsDataApiService,
    private val apiKey: String
) {
    private var nextPageToken: String? = null

    suspend fun fetchPage(
        size: Int = 10,
        country: String? = "au,us",
        language: String? = "en",
        pageToken: String? = null
    ): Pair<List<NewArticle>, String?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getLatest(
                    apikey = apiKey,
                    size = size,
                    country = country,
                    language = language,
                    page = pageToken ?: nextPageToken
                )
                val articles = response.results?.map { it.toArticle() } ?: emptyList()
                nextPageToken = response.nextPage
                Pair(articles, nextPageToken)
            } catch (e: Exception) {
                e.printStackTrace()
                Pair(emptyList(), null)
            }
        }
    }

    fun resetPagination() {
        nextPageToken = null
    }
}
