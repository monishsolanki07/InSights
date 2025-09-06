package com.monish.insight.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monish.insight.data.model.Article
import com.monish.insight.data.model.IndiaArticle
import com.monish.insight.data.repository.NewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NewsRepository(application)

    private val MAX_ARTICLES = 15

    // India (priority)
    private val _indiaArticles = mutableStateOf<List<Article>>(emptyList())
    val indiaArticles: State<List<Article>> = _indiaArticles

    // World (secondary)
    private val _worldArticles = mutableStateOf<List<Article>>(emptyList())
    val worldArticles: State<List<Article>> = _worldArticles

    // Loading
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // Keys (don’t hardcode in prod)
    private val indiaApiKey = "5433a783cb9149b59768f5bc91ce53e6"
    private val worldApiKey = "3e2571c757af4c11b399c4db29198a42"

    init {
        refreshAllNews()
    }

    fun refreshAllNews() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 🚀 Run India + World in parallel
                val indiaDeferred = async(Dispatchers.IO) {
                    try {
                        val indiaResp = repository.getIndiaTopHeadlines(indiaApiKey)
                        indiaResp?.top_news
                            ?.flatMap { it.news ?: emptyList() }
                            ?.mapNotNull { safeIndiaArticleToArticle(it) }
                            ?.take(MAX_ARTICLES)
                            ?: emptyList()
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "India fetch error: ${e.message}", e)
                        emptyList()
                    }
                }

                val worldDeferred = async(Dispatchers.IO) {
                    try {
                        val worldResp = repository.getTopHeadlines(worldApiKey)
                        worldResp?.articles
                            ?.filter { !it.urlToImage.isNullOrBlank() }
                            ?.take(MAX_ARTICLES)
                            ?: emptyList()
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "World fetch error: ${e.message}", e)
                        emptyList()
                    }
                }

                // ✅ Collect both when ready
                _indiaArticles.value = indiaDeferred.await()
                _worldArticles.value = worldDeferred.await()

                Log.d("HomeViewModel", "India: ${_indiaArticles.value.size}, World: ${_worldArticles.value.size}")

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Unexpected error refreshing news: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun safeIndiaArticleToArticle(it: IndiaArticle): Article? {
        val title = it.title ?: it.text ?: return null
        return Article(
            title = title,
            description = it.text,
            url = it.url,
            urlToImage = it.image,
            publishedAt = it.publish_date
        )
    }
}
