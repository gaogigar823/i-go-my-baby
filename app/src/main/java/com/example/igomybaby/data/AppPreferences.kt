package com.example.igomybaby.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.igomybaby.model.NoiseDefaults
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class AppPreferences(context: Context) {
    private val store = context.applicationContext.appDataStore

    private companion object {
        val KEY_THRESHOLD = intPreferencesKey("threshold")
        val KEY_COOLDOWN = intPreferencesKey("cooldown_seconds")
        val KEY_MIN_DURATION = intPreferencesKey("min_duration_ms")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    val threshold: Flow<Int> = store.data.map { it[KEY_THRESHOLD] ?: NoiseDefaults.THRESHOLD_DB }
    val cooldownSeconds: Flow<Int> = store.data.map { it[KEY_COOLDOWN] ?: NoiseDefaults.COOLDOWN_SECONDS }
    val minDurationMs: Flow<Int> = store.data.map { it[KEY_MIN_DURATION] ?: NoiseDefaults.MIN_DURATION_MS }
    val onboardingDone: Flow<Boolean> = store.data.map { it[KEY_ONBOARDING_DONE] ?: false }

    suspend fun setThreshold(value: Int) {
        store.edit { it[KEY_THRESHOLD] = value }
    }

    suspend fun setOnboardingDone() {
        store.edit { it[KEY_ONBOARDING_DONE] = true }
    }
}
