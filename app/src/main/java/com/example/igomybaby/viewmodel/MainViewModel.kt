package com.example.igomybaby.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.igomybaby.data.AppPreferences
import com.example.igomybaby.domain.NoiseAlertEvaluator
import com.example.igomybaby.model.Category
import com.example.igomybaby.model.Detection
import com.example.igomybaby.model.MainState
import com.example.igomybaby.model.NoiseDefaults
import com.example.igomybaby.notification.NoiseNotifications
import com.example.igomybaby.service.DecibelForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = AppPreferences(application)

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    private val alertEvaluator = NoiseAlertEvaluator()
    private var minDurationMs = NoiseDefaults.MIN_DURATION_MS.toLong()
    private var cooldownMs = NoiseDefaults.COOLDOWN_SECONDS * 1_000L

    init {
        viewModelScope.launch {
            combine(
                preferences.threshold,
                preferences.cooldownSeconds,
                preferences.minDurationMs,
            ) { threshold, cooldownSeconds, minDuration ->
                Triple(threshold, cooldownSeconds, minDuration)
            }.collect { (threshold, cooldownSeconds, minDuration) ->
                cooldownMs = cooldownSeconds * 1_000L
                minDurationMs = minDuration.toLong()
                _state.update { it.copy(threshold = threshold) }
            }
        }
        viewModelScope.launch {
            DecibelForegroundService.currentDb.collect { db ->
                processDb(db)
            }
        }
    }

    private fun processDb(db: Float) {
        _state.update {
            it.copy(
                currentDb = db,
                isAlertSent = it.isAlertSent && db >= it.threshold,
            )
        }

        val s = _state.value
        if (s.isPaused) return

        if (alertEvaluator.evaluate(
                db = db,
                threshold = s.threshold,
                minDurationMs = minDurationMs,
                cooldownMs = cooldownMs,
                nowMs = System.currentTimeMillis(),
            )
        ) {
            fireAlert(db.toInt(), s.threshold)
        }
    }

    private fun fireAlert(db: Int, threshold: Int) {
        val ctx = getApplication<Application>()
        val now = System.currentTimeMillis()

        val detection = Detection(
            timestamp = now,
            peakDb    = db,
            category  = Category.from(db),
        )

        _state.update { s ->
            s.copy(
                isAlertSent      = true,
                recentDetections = buildList {
                    add(detection)
                    addAll(s.recentDetections.take(9))
                },
            )
        }

        NoiseNotifications.showAlert(ctx, db, threshold)
    }

    fun togglePause() {
        val willPause = !_state.value.isPaused
        _state.update { it.copy(isPaused = willPause, isAlertSent = false) }
        alertEvaluator.reset()
        val ctx = getApplication<Application>()
        if (willPause) {
            ctx.stopService(Intent(ctx, DecibelForegroundService::class.java))
        } else {
            val intent = Intent(ctx, DecibelForegroundService::class.java)
            ctx.startForegroundService(intent)
        }
    }
}
