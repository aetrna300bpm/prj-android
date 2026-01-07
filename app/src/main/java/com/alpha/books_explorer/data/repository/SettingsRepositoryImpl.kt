package com.alpha.books_explorer.data.repository

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.alpha.books_explorer.domain.model.ThemeMode
import com.alpha.books_explorer.domain.repository.SettingsRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SettingsRepositoryImpl(
    private val context: Context
) : SettingsRepository {

    private val sharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    override fun getThemeMode(): Flow<ThemeMode> = callbackFlow {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == "theme_mode") {
                trySend(getThemeModeFromPrefs(prefs))
            }
        }
        
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(getThemeModeFromPrefs(sharedPreferences))
        
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        sharedPreferences.edit().putString("theme_mode", mode.name).apply()
        applyTheme(mode)
    }
    
    private fun getThemeModeFromPrefs(prefs: android.content.SharedPreferences): ThemeMode {
        val modeName = prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(modeName)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }
    
    fun applyTheme(mode: ThemeMode) {
        val nightMode = when (mode) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
