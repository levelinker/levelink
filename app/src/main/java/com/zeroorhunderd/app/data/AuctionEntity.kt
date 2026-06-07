package com.zeroorhunderd.app.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class AuctionEntity(
    val itemId: String? = null,
    val startPrice: Double? = null,
    val bottomPrice: Double? = null,
    val startTimestamp: Long? = null,
    val durationMs: Long? = null,
    val winner: String? = null,
    val winnerPrice: Double? = null,
    val imageUrl: String? = null
)

