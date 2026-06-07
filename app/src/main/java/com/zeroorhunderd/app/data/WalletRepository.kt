package com.zeroorhunderd.app.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_wallet")

class WalletRepository(private val context: Context) {
    private val CREDITS_KEY = doublePreferencesKey("user_credits")

    val userCredits: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[CREDITS_KEY] ?: 1000000.0 // 초기 지원금 100만 원
    }

    suspend fun updateCredits(amount: Double) {
        context.dataStore.edit { prefs ->
            val current = prefs[CREDITS_KEY] ?: 1000000.0
            prefs[CREDITS_KEY] = current + amount
        }
    }

    suspend fun spendCredits(amount: Double): Boolean {
        var success = false
        context.dataStore.edit { prefs ->
            val current = prefs[CREDITS_KEY] ?: 1000000.0
            if (current >= amount) {
                prefs[CREDITS_KEY] = current - amount
                success = true
            }
        }
        return success
    }
}
