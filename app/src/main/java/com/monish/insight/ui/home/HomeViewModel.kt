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

    // Keys (don't hardcode in prod)
    private val worldApiKey = "3e2571c757af4c11b399c4db29198a42" // NewsAPI.org key
    private val indiaApiKey = "5433a783cb9149b59768f5bc91ce53e6" // WorldNewsAPI key

    // NewsDataHub API key
    private val newsDataHubApiKey = "5DqGTdEhiD-p45OhsGYaZJ0n2HucaPFQjx4wIhXKrLQ"

    init {
        // load world (default) and india in parallel
        refreshAllNews()
        fetchSports()
    }

    fun refreshAllNews() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // parallel fetch: world + india (don't block on sports)
                val worldDeferred = async(Dispatchers.IO) {
                    try {
                        val worldResp = repository.getTopHeadlines(worldApiKey)
                        (worldResp.articles ?: emptyList())
                            .filter { !it.urlToImage.isNullOrBlank() }
                            .take(MAX_ARTICLES)
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "World fetch error: ${e.message}", e)
                        emptyList()
                    }
                }

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

                _worldArticles.value = worldDeferred.await()
                _indiaArticles.value = indiaDeferred.await()

                Log.d("HomeViewModel", "Loaded World: ${_worldArticles.value.size}, India: ${_indiaArticles.value.size}")

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Unexpected refresh error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Fetch sports headlines using NewsAPI 'everything' search (q = "sports").
     */
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
                Log.d("HomeViewModel", "Sports loaded: ${articles.size}")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Sports fetch error: ${e.message}", e)
                _sportsArticles.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Search news via NewsDataHub API with given query keyword.
     */
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

    /**
     * Clear search articles list.
     */
    fun clearSearch() {
        _searchArticles.value = emptyList()
    }

    // ---------- Helper mapper ----------
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
