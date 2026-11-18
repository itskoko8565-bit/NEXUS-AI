package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.StudioPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.nexusDataStore: DataStore<Preferences> by preferencesDataStore(name = "nexus_studio_prefs")

class SettingsDataStore(context: Context) {
    private val dataStore = context.applicationContext.nexusDataStore

    private object Keys {
        val DEFAULT_PLATFORM = stringPreferencesKey("default_platform")
        val DEFAULT_TONE = stringPreferencesKey("default_tone")
        val DEFAULT_SHORT_DURATION = intPreferencesKey("default_short_duration")
        val COMPACT_DENSITY = booleanPreferencesKey("compact_density")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    }

    val preferencesFlow: Flow<StudioPreferences> = dataStore.data.map { prefs ->
        StudioPreferences(
            defaultPlatform = prefs[Keys.DEFAULT_PLATFORM] ?: "YouTube",
            defaultTone = prefs[Keys.DEFAULT_TONE] ?: "Crisp & Analytical",
            defaultShortDurationSec = prefs[Keys.DEFAULT_SHORT_DURATION] ?: 45,
            compactDensity = prefs[Keys.COMPACT_DENSITY] ?: false,
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: false
        )
    }

    suspend fun updateDefaultPlatform(platform: String) {
        dataStore.edit { it[Keys.DEFAULT_PLATFORM] = platform }
    }

    suspend fun updateDefaultTone(tone: String) {
        dataStore.edit { it[Keys.DEFAULT_TONE] = tone }
    }

    suspend fun updateDefaultShortDuration(seconds: Int) {
        dataStore.edit { it[Keys.DEFAULT_SHORT_DURATION] = seconds.coerceIn(15, 90) }
    }

    suspend fun updateCompactDensity(compact: Boolean) {
        dataStore.edit { it[Keys.COMPACT_DENSITY] = compact }
    }

    suspend fun updateDynamicColor(dynamic: Boolean) {
        dataStore.edit { it[Keys.DYNAMIC_COLOR] = dynamic }
    }
}
