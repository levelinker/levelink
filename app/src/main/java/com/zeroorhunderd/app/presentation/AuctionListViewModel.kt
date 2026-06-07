package com.zeroorhunderd.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeroorhunderd.app.data.UserIdProvider
import com.zeroorhunderd.app.data.WalletRepository
import com.zeroorhunderd.app.domain.model.Auction
import com.zeroorhunderd.app.domain.model.PurchaseOutcome
import com.zeroorhunderd.app.domain.repository.AuctionRepository
import com.zeroorhunderd.app.domain.usecase.AttemptPurchaseUseCase
import com.zeroorhunderd.app.domain.usecase.CalculateCurrentPriceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class AuctionListViewModel(
    private val auctionRepository: AuctionRepository,
    private val walletRepository: WalletRepository,
    private val calculateCurrentPriceUseCase: CalculateCurrentPriceUseCase,
    private val attemptPurchaseUseCase: AttemptPurchaseUseCase,
    userIdProvider: UserIdProvider
) : ViewModel() {

    val userUid: String = userIdProvider.getOrCreateUid()

    private val _uiState = MutableStateFlow(AuctionListUiState(isLoading = true))
    val uiState: StateFlow<AuctionListUiState> = _uiState.asStateFlow()

    private val _isHistoryMode = MutableStateFlow(false)
    val isHistoryMode: StateFlow<Boolean> = _isHistoryMode.asStateFlow()

    private val _purchaseMessage = MutableStateFlow<String?>(null)
    val purchaseMessage: StateFlow<String?> = _purchaseMessage.asStateFlow()

    private val _serverTimeOffsetMs = MutableStateFlow(0L)
    val serverTimeOffsetMs: StateFlow<Long> = _serverTimeOffsetMs.asStateFlow()

    val userCredits: StateFlow<Double> = walletRepository.userCredits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1000000.0)

    init {
        viewModelScope.launch {
            auctionRepository.observeServerTimeOffsetMs().collectLatest { offset ->
                _serverTimeOffsetMs.value = offset
            }
        }

        viewModelScope.launch {
            auctionRepository.observeAuctions().collectLatest { auctions ->
                _uiState.value = _uiState.value.copy(
                    auctions = auctions,
                    isLoading = false
                )
            }
        }
    }

    fun toggleFilterMode() {
        _isHistoryMode.value = !_isHistoryMode.value
    }

    fun chargeCredits(amount: Double) {
        viewModelScope.launch {
            walletRepository.updateCredits(amount)
        }
    }

    fun attemptPurchase(auction: Auction) {
        if (auction.winnerUid != null) return

        viewModelScope.launch {
            val currentPrice = calculateCurrentPriceUseCase.execute(
                auction.startPrice,
                auction.bottomPrice,
                auction.startTimestampMs,
                System.currentTimeMillis() + _serverTimeOffsetMs.value
            )

            // 자산 체크 및 차감
            val hasEnoughCredits = walletRepository.spendCredits(currentPrice)
            if (!hasEnoughCredits) {
                _purchaseMessage.value = "잔액이 부족합니다"
                return@launch
            }

            val outcome = attemptPurchaseUseCase(auction.auctionId, userUid, currentPrice)
            
            when (outcome) {
                is PurchaseOutcome.Success -> {
                    _purchaseMessage.value = "구매 성공!"
                }
                is PurchaseOutcome.SoldOut -> {
                    walletRepository.updateCredits(currentPrice)
                    _purchaseMessage.value = "품절된 상품입니다 (환불 완료)"
                }
                is PurchaseOutcome.Failure -> {
                    walletRepository.updateCredits(currentPrice)
                    _purchaseMessage.value = "오류 발생: ${outcome.reason} (환불 완료)"
                }
            }
        }
    }

    fun clearPurchaseMessage() {
        _purchaseMessage.value = null
    }

    fun createNewAuction(
        itemId: String,
        startPrice: Double,
        bottomPrice: Double,
        imageUrl: String? = null
    ) {
        viewModelScope.launch {
            val newId = UUID.randomUUID().toString().take(8)
            val newAuction = Auction(
                auctionId = newId,
                itemId = itemId,
                startPrice = startPrice,
                bottomPrice = bottomPrice,
                startTimestampMs = System.currentTimeMillis() + _serverTimeOffsetMs.value,
                durationMs = 3600_000L * 24 * 7, // 기본 7일 (Duration 제거 대응)
                winnerUid = null,
                imageUrl = imageUrl
            )
            auctionRepository.createAuction(newAuction)
        }
    }
}
