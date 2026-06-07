package com.zeroorhunderd.app.domain.usecase

class CalculateProgressUseCase {
    operator fun invoke(
        startTimestampMs: Long,
        durationMs: Long,
        currentTimeMs: Long
    ): Float {
        if (durationMs <= 0) return 1f
        val elapsedTime = currentTimeMs - startTimestampMs
        return (elapsedTime.toDouble() / durationMs.toDouble()).coerceIn(0.0, 1.0).toFloat()
    }
}

