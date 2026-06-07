package com.zeroorhunderd.app.domain.model

sealed interface PurchaseOutcome {
    data object Success : PurchaseOutcome
    data object SoldOut : PurchaseOutcome
    data class Failure(val reason: String) : PurchaseOutcome
}

