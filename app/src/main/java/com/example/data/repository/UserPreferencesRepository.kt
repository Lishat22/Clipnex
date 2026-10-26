package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "clipnex_prefs")

class UserPreferencesRepository(private val context: Context) {
    companion object {
        val KEY_CUSTOM_API_KEY = stringPreferencesKey("custom_katto_api_key")
        val KEY_DEMO_MODE = booleanPreferencesKey("is_demo_mode")
        val KEY_ONBOARDING_SEEN = booleanPreferencesKey("has_seen_onboarding")
    }

    val customApiKey: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_CUSTOM_API_KEY]?.takeIf { it.isNotBlank() }
    }

    val isDemoMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEMO_MODE] ?: false
    }

    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_SEEN] ?: false
    }

    suspend fun setCustomApiKey(key: String) {
        context.dataStore.edit { preferences ->
            if (key.isBlank()) {
                preferences.remove(KEY_CUSTOM_API_KEY)
            } else {
                preferences[KEY_CUSTOM_API_KEY] = key.trim()
            }
        }
    }

    suspend fun setDemoMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DEMO_MODE] = enabled
        }
    }

    suspend fun setOnboardingSeen(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_SEEN] = seen
        }
    }
}
