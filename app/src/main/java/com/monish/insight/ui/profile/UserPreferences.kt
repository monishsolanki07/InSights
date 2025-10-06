package com.monish.insight.ui.profile

// UserPreferences.kt
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.monish.insight.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    private val KEY_NAME = stringPreferencesKey("name")
    private val KEY_EMAIL = stringPreferencesKey("email")
    private val KEY_LOCATION = stringPreferencesKey("location")
    private val KEY_BIO = stringPreferencesKey("bio")

    val userProfileFlow: Flow<UserProfile> = context.dataStore.data.map { prefs ->
        UserProfile(
            name = prefs[KEY_NAME] ?: "",
            email = prefs[KEY_EMAIL] ?: "",
            location = prefs[KEY_LOCATION] ?: "",
            bio = prefs[KEY_BIO] ?: ""
        )
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NAME] = profile.name
            prefs[KEY_EMAIL] = profile.email
            prefs[KEY_LOCATION] = profile.location
            prefs[KEY_BIO] = profile.bio
        }
    }
}

