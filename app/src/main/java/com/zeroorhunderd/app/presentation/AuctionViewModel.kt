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
import com.zeroorhunderd.app.domain.usecase.CalculateProgressUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class AuctionViewModel(
    private val auctionId: String,
    private val calculateCurrentPriceUseCase: CalculateCurrentPriceUseCase,
    private val calculateProgressUseCase: CalculateProgressUseCase,
    private val attemptPurchaseUseCase: AttemptPurchaseUseCase,
    private val auctionRepository: AuctionRepository,
    private val walletRepository: WalletRepository,
    userIdProvider: UserIdProvider
) : ViewModel() {
    private val userUid: String = userIdProvider.getOrCreateUid()

    private val _uiState = MutableStateFlow(AuctionUiState(auctionId = auctionId))
    val uiState: StateFlow<AuctionUiState> = _uiState.asStateFlow()

    val userCredits: StateFlow<Double> = walletRepository.userCredits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1000000.0)

    private val _currentTimeMs = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMs: StateFlow<Long> = _currentTimeMs.asStateFlow()

    private val _serverTimeOffsetMs = MutableStateFlow(0L)

    init {
        viewModelScope.launch {
            auctionRepository.observeServerTimeOffsetMs().collectLatest { offset ->
                _serverTimeOffsetMs.value = offset
            }
        }

        observeAuction(auctionId)

        viewModelScope.launch {
            while (true) {
                _currentTimeMs.value = System.currentTimeMillis() + _serverTimeOffsetMs.value
                delay(16L)
            }
        }
    }

    private fun observeAuction(id: String) {
        viewModelScope.launch {
            auctionRepository.observeAuction(id).collectLatest { auction ->
                _uiState.value = _uiState.value.copy(
                    auctionId = id,
                    itemId = auction.itemId,
                    startPrice = auction.startPrice,
                    bottomPrice = auction.bottomPrice,
                    startTimestampMs = auction.startTimestampMs,
                    durationMs = auction.durationMs,
                    winnerUid = auction.winnerUid,
                    isUserWinner = auction.winnerUid == userUid,
                    winnerPrice = auction.winnerPrice,
                    imageUrl = auction.imageUrl,
                    errorMessage = null,
                    isLoading = false
                )
            }
        }
    }

    fun attemptPurchase() {
        val state = _uiState.value
        if (state.isProcessingPurchase || state.isSoldOut) return

        viewModelScope.launch {
            val currentPrice = calculateCurrentPrice(
                state.startPrice,
                state.bottomPrice,
                state.startTimestampMs,
                _currentTimeMs.value
            )

            // 자산 체크 및 차감
            val hasEnoughCredits = walletRepository.spendCredits(currentPrice)
            if (!hasEnoughCredits) {
                _uiState.value = _uiState.value.copy(errorMessage = "INSUFFICIENT CREDITS")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isProcessingPurchase = true, errorMessage = null)
            val outcome = attemptPurchaseUseCase(state.auctionId, userUid, currentPrice)
            
            _uiState.value = when (outcome) {
                is PurchaseOutcome.Success -> _uiState.value.copy(
                    isProcessingPurchase = false,
                    winnerPrice = currentPrice
                )
                is PurchaseOutcome.SoldOut -> {
                    // 구매 실패(품절) 시 돈 다시 돌려주기
                    walletRepository.updateCredits(currentPrice)
                    _uiState.value.copy(isProcessingPurchase = false)
                }
                is PurchaseOutcome.Failure -> {
                    // 에러 발생 시 돈 다시 돌려주기
                    walletRepository.updateCredits(currentPrice)
                    _uiState.value.copy(
                        isProcessingPurchase = false,
                        errorMessage = outcome.reason
                    )
                }
            }
        }
    }

    fun calculateCurrentPrice(
        startPrice: Double,
        bottomPrice: Double,
        startTimestampMs: Long,
        currentTimeMs: Long
    ): Double {
        return calculateCurrentPriceUseCase.execute(
            startPrice = startPrice,
            bottomPrice = bottomPrice,
            startTimestampMs = startTimestampMs,
            currentTimeMs = currentTimeMs
        )
    }

    fun calculateProgress(
        startTimestampMs: Long,
        durationMs: Long,
        currentTimeMs: Long
    ): Float {
        // Duration 필드 제거에 따라 1시간을 기준으로 선형 진행률 계산
        val dropDurationMs = 3600_000L
        val elapsedTime = currentTimeMs - startTimestampMs
        return (elapsedTime.toDouble() / dropDurationMs).coerceIn(0.0, 1.0).toFloat()
    }
}

