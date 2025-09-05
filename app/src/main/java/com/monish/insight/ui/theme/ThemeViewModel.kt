package com.monish.insight.ui.theme

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// DataStore instance for Application
private val Application.dataStore by preferencesDataStore(name = "settings")

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.dataStore
    private val THEME_KEY = booleanPreferencesKey("dark_theme")

    // Flow that observes theme mode
    val isDarkTheme: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[THEME_KEY] ?: true // ✅ default = dark mode
    }

    // Save preference
    fun toggleTheme(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[THEME_KEY] = enabled
            }
        }
    }
}
