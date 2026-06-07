package com.zeroorhunderd.app.presentation

import com.zeroorhunderd.app.domain.model.Auction

data class AuctionListUiState(
    val auctions: List<Auction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
