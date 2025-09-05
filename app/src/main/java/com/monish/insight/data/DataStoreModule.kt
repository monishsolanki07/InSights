package com.monish.insight.data


import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

// ✅ This makes `context.dataStore` available everywhere
val Context.dataStore by preferencesDataStore(name = "settings")
