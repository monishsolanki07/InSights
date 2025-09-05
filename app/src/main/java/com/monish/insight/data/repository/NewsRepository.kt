package com.monish.insight.data.repository

import android.content.Context
import androidx.room.Room
import com.monish.insight.data.local.AppDatabase
import com.monish.insight.data.local.BookmarkEntity
import kotlinx.coroutines.flow.Flow

class NewsRepository(context: Context) {

    private val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "news_db"
    ).build()

    private val bookmarkDao = db.bookmarkDao()

    // 🔹 API calls
    suspend fun getTopHeadlines(apiKey: String) =
        RetrofitInstance.api.getTopHeadlines(apiKey = apiKey)

    // 🔹 Bookmarks
    suspend fun insertBookmark(bookmark: BookmarkEntity) =
        bookmarkDao.insertBookmark(bookmark)

    suspend fun deleteBookmark(bookmark: BookmarkEntity) =
        bookmarkDao.deleteBookmark(bookmark)

    fun getAllBookmarks(): Flow<List<BookmarkEntity>> =
        bookmarkDao.getAllBookmarks()
}
