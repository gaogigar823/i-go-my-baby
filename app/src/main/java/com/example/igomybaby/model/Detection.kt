package com.example.igomybaby.model

/** 소음 감지 관련 기본값·판정 기준의 단일 출처 */
object NoiseDefaults {
    const val THRESHOLD_DB = 65
    const val COOLDOWN_SECONDS = 5
    const val MIN_DURATION_MS = 300
    /** 이 값 이상이면 아기 울음으로 분류 */
    const val BABY_CRY_DB = 80
}

data class Detection(
    val timestamp: Long,
    val peakDb: Int,
    val category: Category,
)

enum class Category {
    BABY_CRY, LOUD;

    companion object {
        fun from(db: Int): Category =
            if (db >= NoiseDefaults.BABY_CRY_DB) BABY_CRY else LOUD
    }
}

data class MainState(
    val currentDb: Float = 0f,
    val threshold: Int = NoiseDefaults.THRESHOLD_DB,
    val isPaused: Boolean = false,
    val isAlertSent: Boolean = false,
    val recentDetections: List<Detection> = emptyList(),
)

data class SettingsState(
    val threshold: Int = NoiseDefaults.THRESHOLD_DB,
    val cooldownSeconds: Int = NoiseDefaults.COOLDOWN_SECONDS,
    val minDurationMs: Int = NoiseDefaults.MIN_DURATION_MS,
)
