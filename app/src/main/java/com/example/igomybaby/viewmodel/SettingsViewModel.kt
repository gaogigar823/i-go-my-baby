package com.example.igomybaby.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.igomybaby.data.AppPreferences
import com.example.igomybaby.model.SettingsState
import com.example.igomybaby.notification.NoiseNotifications
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = AppPreferences(application)

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferences.threshold,
                preferences.cooldownSeconds,
                preferences.minDurationMs,
            ) { threshold, cooldown, minDuration ->
                SettingsState(threshold, cooldown, minDuration)
            }.collect { _state.value = it }
        }
    }

    fun setThreshold(value: Int) {
        _state.update { it.copy(threshold = value) }
        viewModelScope.launch { preferences.setThreshold(value) }
    }

    fun sendPreviewNotification() {
        NoiseNotifications.showPreview(getApplication())
    }
}
