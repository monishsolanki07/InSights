package com.monish.insight.ui.reels

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("news_prefs")

object PrefKeys {
    val LAST_FETCH_TIME = longPreferencesKey("LAST_FETCH_TIME")
    val FETCH_COUNT = intPreferencesKey("FETCH_COUNT")
    val LAST_FETCH_DAY = intPreferencesKey("LAST_FETCH_DAY")
}
