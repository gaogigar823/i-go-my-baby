package com.example.igomybaby.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoiseAlertEvaluatorTest {
    private val evaluator = NoiseAlertEvaluator()

    @Test
    fun `alerts only after noise remains above threshold`() {
        assertFalse(evaluate(db = 70f, nowMs = 1_000))
        assertFalse(evaluate(db = 70f, nowMs = 1_299))
        assertTrue(evaluate(db = 70f, nowMs = 1_300))
    }

    @Test
    fun `noise below threshold resets duration`() {
        assertFalse(evaluate(db = 70f, nowMs = 1_000))
        assertFalse(evaluate(db = 60f, nowMs = 1_200))
        assertFalse(evaluate(db = 70f, nowMs = 1_300))
        assertTrue(evaluate(db = 70f, nowMs = 1_600))
    }

    @Test
    fun `cooldown prevents repeated alerts`() {
        assertFalse(evaluate(db = 70f, nowMs = 1_000))
        assertTrue(evaluate(db = 70f, nowMs = 1_300))
        assertFalse(evaluate(db = 70f, nowMs = 6_299))
        assertTrue(evaluate(db = 70f, nowMs = 6_300))
    }

    private fun evaluate(db: Float, nowMs: Long) = evaluator.evaluate(
        db = db,
        threshold = 65,
        minDurationMs = 300,
        cooldownMs = 5_000,
        nowMs = nowMs,
    )
}
