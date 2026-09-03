package com.example.igomybaby.domain

/**
 * Determines when a sustained noise sample should produce an alert.
 *
 * This class deliberately has no Android dependencies so the timing rules can be unit tested.
 */
class NoiseAlertEvaluator {
    private var aboveThresholdSince: Long? = null
    private var lastAlertAt: Long? = null

    fun evaluate(
        db: Float,
        threshold: Int,
        minDurationMs: Long,
        cooldownMs: Long,
        nowMs: Long,
    ): Boolean {
        if (db < threshold) {
            aboveThresholdSince = null
            return false
        }

        val startedAt = aboveThresholdSince ?: nowMs.also { aboveThresholdSince = it }
        val previousAlertAt = lastAlertAt
        val durationReached = nowMs - startedAt >= minDurationMs
        val cooldownReached = previousAlertAt == null || nowMs - previousAlertAt >= cooldownMs

        return (durationReached && cooldownReached).also { shouldAlert ->
            if (shouldAlert) lastAlertAt = nowMs
        }
    }

    /**
     * 지속시간 추적만 초기화한다.
     * [lastAlertAt]은 의도적으로 유지 — 일시정지 → 재개 직후 쿨다운을 무시한
     * 연속 알림이 발생하지 않도록 막는다.
     */
    fun reset() {
        aboveThresholdSince = null
    }
}
