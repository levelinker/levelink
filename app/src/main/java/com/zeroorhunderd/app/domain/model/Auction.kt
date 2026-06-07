package com.zeroorhunderd.app.domain.model

data class Auction(
    val auctionId: String,
    val itemId: String,
    val startPrice: Double,
    val bottomPrice: Double,
    val startTimestampMs: Long,
    val durationMs: Long,
    val winnerUid: String?,
    val winnerPrice: Double? = null,
    val imageUrl: String? = null
)

