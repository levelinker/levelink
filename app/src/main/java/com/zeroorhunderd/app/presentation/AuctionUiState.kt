package com.zeroorhunderd.app.presentation

data class AuctionUiState(
    val auctionId: String = "demo",
    val itemId: String = "",
    val startPrice: Double = 0.0,
    val bottomPrice: Double = 0.0,
    val startTimestampMs: Long = 0L,
    val durationMs: Long = 1L,
    val winnerUid: String? = null,
    val isProcessingPurchase: Boolean = false,
    val errorMessage: String? = null,
    val isUserWinner: Boolean = false,
    val isLoading: Boolean = true,
    val winnerPrice: Double? = null,
    val imageUrl: String? = null
) {
    val isSoldOut: Boolean get() = winnerUid != null
}

