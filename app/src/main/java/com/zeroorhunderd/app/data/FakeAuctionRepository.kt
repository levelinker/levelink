package com.zeroorhunderd.app.data
import com.zeroorhunderd.app.domain.model.Auction
import com.zeroorhunderd.app.domain.model.PurchaseOutcome
import com.zeroorhunderd.app.domain.repository.AuctionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class FakeAuctionRepository(
    private val userIdProvider: UserIdProvider
) : AuctionRepository {
    private val auctions = MutableStateFlow(
        mapOf(
            "demo" to Auction(
                auctionId = "demo",
                itemId = "DEMO_ITEM_001",
                startPrice = 100_000.0,
                bottomPrice = 1_000.0,
                startTimestampMs = System.currentTimeMillis(),
                durationMs = 120_000L,
                winnerUid = null
            )
        )
    )
    
    override fun observeAuctions(): Flow<List<Auction>> {
        return auctions.map { it.values.toList() }
    }

    override fun observeAuction(auctionId: String): Flow<Auction> {
        return auctions
            .map { map -> map[auctionId] ?: map.getValue("demo") }
            .distinctUntilChanged()
    }

    override fun observeServerTimeOffsetMs(): Flow<Long> {
        return MutableStateFlow(0L)
    }

    override suspend fun attemptPurchase(auctionId: String, userUid: String, price: Double): PurchaseOutcome {
        val current = auctions.value[auctionId] ?: return PurchaseOutcome.Failure("Auction not found")
        if (current.winnerUid != null) return PurchaseOutcome.SoldOut

        val updated = current.copy(winnerUid = userUid, winnerPrice = price)
        auctions.value = auctions.value + (auctionId to updated)
        return PurchaseOutcome.Success
    }

    override suspend fun createAuction(auction: Auction) {
        auctions.value = auctions.value + (auction.auctionId to auction)
    }

    fun getOrCreateUid(): String = userIdProvider.getOrCreateUid()
}
