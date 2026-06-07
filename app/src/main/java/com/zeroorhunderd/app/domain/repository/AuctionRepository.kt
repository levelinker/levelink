package com.zeroorhunderd.app.domain.repository

import com.zeroorhunderd.app.domain.model.Auction
import com.zeroorhunderd.app.domain.model.PurchaseOutcome
import kotlinx.coroutines.flow.Flow

interface AuctionRepository {
    fun observeAuctions(): Flow<List<Auction>>
    fun observeAuction(auctionId: String): Flow<Auction>
    fun observeServerTimeOffsetMs(): Flow<Long>
    suspend fun attemptPurchase(auctionId: String, userUid: String, price: Double): PurchaseOutcome
    suspend fun createAuction(auction: Auction)
}

