package com.monish.insight.ui.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monish.insight.data.model.Article
import com.monish.insight.data.repository.NewsRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel


class HomeViewModel(application: Application) : AndroidViewModel(application)  {
    private val repository = NewsRepository(application)

    private val _articles = mutableStateOf<List<Article>>(emptyList())
    val articles: State<List<Article>> = _articles

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val apiKey = "3e2571c757af4c11b399c4db29198a42" // 🔑 replace with real key

    init {
        fetchTopHeadlines()
    }

    private fun fetchTopHeadlines() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = repository.getTopHeadlines(apiKey)

                // ✅ Add log here
                Log.d("HomeViewModel", "Fetched articles: ${response.articles.size}")
                response.articles.forEach { Log.d("HomeViewModel", it.title ?: "No title") }

                _articles.value = response.articles
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching news: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

}