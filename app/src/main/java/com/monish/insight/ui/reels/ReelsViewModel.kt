package com.monish.insight.ui.reels

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monish.insight.data.model.NewArticle
import com.monish.insight.data.remote.NewsDataRetrofit
import com.monish.insight.data.repository.NewsDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

import java.util.*

class ReelsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val repository = NewsDataRepository(NewsDataRetrofit.api, "pub_492df361c57a4e53a20651cb92779f99")

    // DataStore keys
    private val CACHED_ARTICLES = stringPreferencesKey("cached_articles_json")
    private val CACHE_TIMESTAMP = longPreferencesKey("cached_articles_timestamp")

    private val json = Json { ignoreUnknownKeys = true }

    private val _articles = MutableStateFlow<List<NewArticle>>(emptyList())
    val articles: StateFlow<List<NewArticle>> = _articles

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val MAX_ARTICLES = 30
    private val CACHE_EXPIRY_MS = 5 * 60 * 60 * 1000L // 5 hours

    private val pageSize = 10
    private val countryFilter = "au,us"
    private val languageFilter = "en"

    private var currentPageToken: String? = null
    private var exhausted = false
    private var fetching = false

    init {
        viewModelScope.launch {
            loadCachedOrFetch()
        }
    }

    private suspend fun loadCachedArticles(): List<NewArticle> {
        return try {
            val prefs = context.dataStore.data.first()
            val cachedJson = prefs[CACHED_ARTICLES]
            val cacheTime = prefs[CACHE_TIMESTAMP] ?: 0L
            val now = System.currentTimeMillis()
            if (cachedJson != null && now - cacheTime < CACHE_EXPIRY_MS) {
                json.decodeFromString(cachedJson)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun saveArticlesToCache(articles: List<NewArticle>) {
        try {
            val jsonString = json.encodeToString(articles)
            context.dataStore.edit {
                it[CACHED_ARTICLES] = jsonString
                it[CACHE_TIMESTAMP] = System.currentTimeMillis()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private suspend fun loadCachedOrFetch() {
        val cachedArticles = loadCachedArticles()
        if (cachedArticles.isNotEmpty()) {
            _articles.value = cachedArticles
            exhausted = true  // no immediate fetch needed
        } else {
            fetchNextIfNeeded()
        }
    }

    fun fetchNextIfNeeded() {
        if (fetching || exhausted) return
        if (_articles.value.size >= MAX_ARTICLES) {
            exhausted = true
            return
        }

        fetching = true
        viewModelScope.launch {
            _isLoading.value = true

            val (fetched, nextPage) = repository.fetchPage(
                size = pageSize,
                country = countryFilter,
                language = languageFilter,
                pageToken = currentPageToken
            )
            currentPageToken = nextPage

            if (fetched.isEmpty()) {
                exhausted = true
            } else {
                val combined = (_articles.value + fetched)
                    .distinctBy { it.link }
                    .take(MAX_ARTICLES)

                _articles.value = combined
                saveArticlesToCache(combined)  // update cache

                if (combined.size >= MAX_ARTICLES || nextPage == null) {
                    exhausted = true
                }
            }

            fetching = false
            _isLoading.value = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            exhausted = false
            currentPageToken = null
            _articles.value = emptyList()
            loadCachedOrFetch()
        }
    }
}
