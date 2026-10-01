package com.fittracker.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.fittracker.app.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ApiKeyManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("fit_tracker_prefs", Context.MODE_PRIVATE)

    private val _apiKeyFlow = MutableStateFlow(getApiKey())
    val apiKeyFlow: StateFlow<String> = _apiKeyFlow.asStateFlow()

    fun getApiKey(): String {
        val storedKey = prefs.getString(KEY_GEMINI, "")?.trim() ?: ""
        val buildKey = BuildConfig.GEMINI_API_KEY.trim()

        // Si la clave guardada en SharedPreferences está vacía, sincronizar con la clave de BuildConfig
        if (storedKey.isBlank() && buildKey.isNotBlank() && !buildKey.contains("your_api_key")) {
            prefs.edit().putString(KEY_GEMINI, buildKey).apply()
            return buildKey
        }
        val effective = storedKey.ifBlank { buildKey }
        return if (effective.contains("your_api_key")) "" else effective
    }

    fun setApiKey(key: String) {
        val trimmed = key.trim()
        prefs.edit().putString(KEY_GEMINI, trimmed).apply()
        _apiKeyFlow.value = trimmed
    }

    fun hasValidKey(): Boolean {
        return getApiKey().isNotBlank()
    }

    companion object {
        private const val KEY_GEMINI = "gemini_api_key"

        @Volatile
        private var instance: ApiKeyManager? = null

        fun getInstance(context: Context): ApiKeyManager {
            return instance ?: synchronized(this) {
                instance ?: ApiKeyManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
