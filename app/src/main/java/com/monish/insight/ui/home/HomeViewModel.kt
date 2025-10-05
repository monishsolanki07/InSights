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
import kotlinx.coroutines.withContext
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NewsRepository(application)
    private val MAX_ARTICLES = 15

    // World (default)
    private val _worldArticles = mutableStateOf<List<Article>>(emptyList())
    val worldArticles: State<List<Article>> = _worldArticles

    // India (secondary)
    private val _indiaArticles = mutableStateOf<List<Article>>(emptyList())
    val indiaArticles: State<List<Article>> = _indiaArticles

    // Sports (search-based)
    private val _sportsArticles = mutableStateOf<List<Article>>(emptyList())
    val sportsArticles: State<List<Article>> = _sportsArticles

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // Search articles state
    private val _searchArticles = mutableStateOf<List<Article>>(emptyList())
    val searchArticles: State<List<Article>> = _searchArticles

    // API keys (do not hardcode in production)
    private val worldApiKey = "3e2571c757af4c11b399c4db29198a42"
    private val indiaApiKey = "5433a783cb9149b59768f5bc91ce53e6"
    private val newsDataHubApiKey = "5DqGTdEhiD-p45OhsGYaZJ0n2HucaPFQjx4wIhXKrLQ"

    init {
        refreshAllNews()
        fetchSports()
    }

    fun refreshAllNews() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val worldDeferred = async {
                    repository.getTopHeadlines(worldApiKey)?.articles
                        ?.filter { !it.urlToImage.isNullOrBlank() }
                        ?.take(MAX_ARTICLES) ?: emptyList()
                }

                val indiaDeferred = async {
                    repository.getIndiaTopHeadlines(indiaApiKey)?.top_news
                        ?.flatMap { it.news ?: emptyList() }
                        ?.mapNotNull { safeIndiaArticleToArticle(it) }
                        ?.filter { !it.urlToImage.isNullOrBlank() }
                        ?.take(MAX_ARTICLES) ?: emptyList()
                }

                val worldArticlesResult = worldDeferred.await()
                val indiaArticlesResult = indiaDeferred.await()

                withContext(Dispatchers.Main) {
                    _worldArticles.value = worldArticlesResult
                    _indiaArticles.value = indiaArticlesResult
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "refreshAllNews error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun fetchSports(query: String = "sports", pageSize: Int = 10) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resp = withContext(Dispatchers.IO) {
                    repository.getCategoryNews(query = query, apiKey = worldApiKey)
                }

                val articles = resp.articles
                    ?.filter { !it.urlToImage.isNullOrBlank() }
                    ?.take(MAX_ARTICLES)
                    ?: emptyList()

                _sportsArticles.value = articles
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Sports fetch error: ${e.message}", e)
                _sportsArticles.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchNews(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resp = withContext(Dispatchers.IO) {
                    repository.searchNewsDataHub(query = query, apiKey = newsDataHubApiKey)
                }
                _searchArticles.value = resp.filter { !it.urlToImage.isNullOrBlank() }.take(MAX_ARTICLES)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Search fetch error: ${e.message}", e)
                _searchArticles.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSearch() {
        _searchArticles.value = emptyList()
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
