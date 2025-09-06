package com.monish.insight.data.repository

import android.content.Context
import androidx.room.Room
import com.monish.insight.data.local.AppDatabase
import com.monish.insight.data.local.BookmarkEntity
import com.monish.insight.data.remote.IndiaRetrofitInstance
import kotlinx.coroutines.flow.Flow

class NewsRepository(context: Context) {

    private val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "news_db"
    ).build()

    private val bookmarkDao = db.bookmarkDao()

    // 🌍 World news
    suspend fun getTopHeadlines(apiKey: String) =
        RetrofitInstance.api.getTopHeadlines(apiKey = apiKey)

    // 🇮🇳 India news
    suspend fun getIndiaTopHeadlines(apiKey: String) =
        IndiaRetrofitInstance.api.getIndiaTopNews(
            apiKey = apiKey,
            sourceCountry = "in",
            language = "en",
            category = "sports", // or "technology", "sports", "business"
            query = null
        )




    // 📌 Bookmarks
    suspend fun insertBookmark(bookmark: BookmarkEntity) =
        bookmarkDao.insertBookmark(bookmark)

    suspend fun deleteBookmark(bookmark: BookmarkEntity) =
        bookmarkDao.deleteBookmark(bookmark)

    fun getAllBookmarks(): Flow<List<BookmarkEntity>> =
        bookmarkDao.getAllBookmarks()
}
