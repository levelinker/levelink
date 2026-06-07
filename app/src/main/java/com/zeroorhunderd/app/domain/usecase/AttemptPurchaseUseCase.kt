package com.zeroorhunderd.app.domain.usecase

import com.zeroorhunderd.app.domain.model.PurchaseOutcome
import com.zeroorhunderd.app.domain.repository.AuctionRepository

class AttemptPurchaseUseCase(
    private val auctionRepository: AuctionRepository
) {
    suspend operator fun invoke(auctionId: String, userUid: String, price: Double): PurchaseOutcome {
        return auctionRepository.attemptPurchase(auctionId, userUid, price)
    }
}

