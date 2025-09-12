package com.monish.insight.ui.bookmarks


import androidx.lifecycle.viewModelScope
import com.monish.insight.data.local.BookmarkEntity
import com.monish.insight.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.app.Application
import androidx.lifecycle.AndroidViewModel

class BookmarksViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NewsRepository(application)

    // Hold all bookmarks
    private val _allBookmarks = MutableStateFlow<List<BookmarkEntity>>(emptyList())
    val allBookmarks: StateFlow<List<BookmarkEntity>> = _allBookmarks

    init {
        loadBookmarks()

    }

    private fun loadBookmarks() {
        viewModelScope.launch {
            repository.getAllBookmarks().collect { bookmarks ->
                _allBookmarks.value = bookmarks
            }
        }
    }

    fun addBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            repository.insertBookmark(bookmark)
        }
    }

    fun deleteBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            repository.deleteBookmark(bookmark)
        }
    }

    fun toggleBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            val exists = _allBookmarks.value.any { it.url == bookmark.url }
            if (exists) {
                deleteBookmark(bookmark)
            } else {
                addBookmark(bookmark)
            }
        }
    }
}
