package com.monish.insight.ui.profile

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property to initialize DataStore
val Context.dataStore by preferencesDataStore("user_prefs")

class UserPreferences(private val context: Context) {
    private val USER_NAME = stringPreferencesKey("user_name")

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_NAME] = name
        }
    }

    fun getUserName(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[USER_NAME]
        }
    }
}
