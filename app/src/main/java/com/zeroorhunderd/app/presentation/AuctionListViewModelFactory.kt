package com.zeroorhunderd.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zeroorhunderd.app.data.UserIdProvider
import com.zeroorhunderd.app.data.WalletRepository
import com.zeroorhunderd.app.domain.repository.AuctionRepository
import com.zeroorhunderd.app.domain.usecase.AttemptPurchaseUseCase
import com.zeroorhunderd.app.domain.usecase.CalculateCurrentPriceUseCase

class AuctionListViewModelFactory(
    private val auctionRepository: AuctionRepository,
    private val walletRepository: WalletRepository,
    private val calculateCurrentPriceUseCase: CalculateCurrentPriceUseCase,
    private val attemptPurchaseUseCase: AttemptPurchaseUseCase,
    private val userIdProvider: UserIdProvider
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuctionListViewModel::class.java)) {
            return AuctionListViewModel(
                auctionRepository,
                walletRepository,
                calculateCurrentPriceUseCase,
                attemptPurchaseUseCase,
                userIdProvider
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
