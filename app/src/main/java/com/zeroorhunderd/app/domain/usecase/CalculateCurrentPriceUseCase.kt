package com.zeroorhunderd.app.domain.usecase

class CalculateCurrentPriceUseCase {
    fun execute(
        startPrice: Double,
        bottomPrice: Double,
        startTimestampMs: Long,
        currentTimeMs: Long
    ): Double {
        if (startTimestampMs <= 0) return startPrice
        
        val elapsedTimeMs = currentTimeMs - startTimestampMs
        if (elapsedTimeMs <= 0) return startPrice
        
        // Duration이 없으므로, 고정된 속도로 선형 하락
        // 예: 1시간(3,600,000ms) 동안 시작가에서 최저가까지 하락
        val dropDurationMs = 3600_000L 
        
        val progress = (elapsedTimeMs.toDouble() / dropDurationMs).coerceIn(0.0, 1.0)
        val currentPrice = startPrice - (startPrice - bottomPrice) * progress

        return currentPrice.coerceAtLeast(bottomPrice)
    }
}

