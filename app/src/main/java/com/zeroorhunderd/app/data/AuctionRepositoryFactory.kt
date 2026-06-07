package com.zeroorhunderd.app.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.zeroorhunderd.app.domain.repository.AuctionRepository

object AuctionRepositoryFactory {
    fun create(appContext: Context): AuctionRepository {
        val context = appContext.applicationContext
        
        // Initialize Firebase if not already initialized
        if (FirebaseApp.getApps(context).isEmpty()) {
            try {
                FirebaseApp.initializeApp(context)
            } catch (e: Exception) {
                // Handle initialization error or fallback to Fake
            }
        }

        return if (FirebaseApp.getApps(context).isNotEmpty()) {
            FirebaseAuctionRepository(
                userIdProvider = UserIdProvider(context)
            )
        } else {
            FakeAuctionRepository(
                userIdProvider = UserIdProvider(context)
            )
        }
    }
}
