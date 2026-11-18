package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.StudioPreferences
import com.example.data.repository.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val preferences: StateFlow<StudioPreferences> = settingsDataStore.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = StudioPreferences()
        )

    fun setDefaultPlatform(platform: String) {
        viewModelScope.launch { settingsDataStore.updateDefaultPlatform(platform) }
    }

    fun setDefaultTone(tone: String) {
        viewModelScope.launch { settingsDataStore.updateDefaultTone(tone) }
    }

    fun setDefaultShortDuration(seconds: Int) {
        viewModelScope.launch { settingsDataStore.updateDefaultShortDuration(seconds) }
    }

    fun setCompactDensity(compact: Boolean) {
        viewModelScope.launch { settingsDataStore.updateCompactDensity(compact) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.updateDynamicColor(enabled) }
    }
}
