package com.zeroorhunderd.app.data

import android.content.Context
import java.util.UUID

class UserIdProvider(
    private val appContext: Context
) {
    fun getOrCreateUid(): String {
        val preferences = appContext.getSharedPreferences("zero_or_hundred", Context.MODE_PRIVATE)
        val existing = preferences.getString("uid", null)
        if (!existing.isNullOrBlank()) return existing

        val created = UUID.randomUUID().toString()
        preferences.edit().putString("uid", created).apply()
        return created
    }
}

