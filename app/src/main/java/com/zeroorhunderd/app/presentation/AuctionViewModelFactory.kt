package com.zeroorhunderd.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zeroorhunderd.app.data.UserIdProvider
import com.zeroorhunderd.app.data.WalletRepository
import com.zeroorhunderd.app.domain.repository.AuctionRepository
import com.zeroorhunderd.app.domain.usecase.AttemptPurchaseUseCase
import com.zeroorhunderd.app.domain.usecase.CalculateCurrentPriceUseCase
import com.zeroorhunderd.app.domain.usecase.CalculateProgressUseCase

class AuctionViewModelFactory(
    private val auctionId: String,
    private val calculateCurrentPriceUseCase: CalculateCurrentPriceUseCase,
    private val calculateProgressUseCase: CalculateProgressUseCase,
    private val attemptPurchaseUseCase: AttemptPurchaseUseCase,
    private val auctionRepository: AuctionRepository,
    private val walletRepository: WalletRepository,
    private val userIdProvider: UserIdProvider
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuctionViewModel::class.java)) {
            return AuctionViewModel(
                auctionId,
                calculateCurrentPriceUseCase,
                calculateProgressUseCase,
                attemptPurchaseUseCase,
                auctionRepository,
                walletRepository,
                userIdProvider
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

