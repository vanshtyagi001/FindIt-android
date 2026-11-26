package com.vanshtyagi.findit.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vanshtyagi.findit.data.remote.ItemDto
import com.vanshtyagi.findit.data.remote.UserDto

/**
 * Lightweight disk cache backed by SharedPreferences + Gson for:
 * 1. Stale-While-Revalidate caching of recent found items so HomeScreen renders immediately.
 * 2. Persisting the signed-in user's JWT token and profile across app restarts.
 */
class LocalCacheManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val itemsListType = object : TypeToken<List<ItemDto>>() {}.type

    fun getCachedItems(): List<ItemDto> {
        val raw = prefs.getString(KEY_RECENT_ITEMS, null) ?: return emptyList()
        return try {
            gson.fromJson<List<ItemDto>>(raw, itemsListType) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveCachedItems(items: List<ItemDto>) {
        try {
            prefs.edit()
                .putString(KEY_RECENT_ITEMS, gson.toJson(items))
                .putLong(KEY_RECENT_ITEMS_AT, System.currentTimeMillis())
                .apply()
        } catch (_: Exception) {
            // Ignore serialization failures; network remains source of truth.
        }
    }

    fun clearCachedItems() {
        prefs.edit()
            .remove(KEY_RECENT_ITEMS)
            .remove(KEY_RECENT_ITEMS_AT)
            .apply()
    }

    fun getUserSession(): Pair<String, UserDto>? {
        val token = prefs.getString(KEY_USER_TOKEN, null) ?: return null
        val userJson = prefs.getString(KEY_CURRENT_USER, null) ?: return null
        return try {
            val user = gson.fromJson(userJson, UserDto::class.java) ?: return null
            token to user
        } catch (_: Exception) {
            null
        }
    }

    fun saveUserSession(token: String, user: UserDto) {
        try {
            prefs.edit()
                .putString(KEY_USER_TOKEN, token)
                .putString(KEY_CURRENT_USER, gson.toJson(user))
                .apply()
        } catch (_: Exception) {
            // Ignore serialization failures.
        }
    }

    fun updateCachedUser(user: UserDto) {
        if (prefs.getString(KEY_USER_TOKEN, null) == null) return
        try {
            prefs.edit()
                .putString(KEY_CURRENT_USER, gson.toJson(user))
                .apply()
        } catch (_: Exception) {
            // Ignore serialization failures.
        }
    }

    fun clearUserSession() {
        prefs.edit()
            .remove(KEY_USER_TOKEN)
            .remove(KEY_CURRENT_USER)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "findit_cache_prefs"
        const val KEY_RECENT_ITEMS = "cached_recent_items"
        const val KEY_RECENT_ITEMS_AT = "cached_recent_items_at"
        const val KEY_USER_TOKEN = "user_token"
        const val KEY_CURRENT_USER = "current_user"
    }
}
