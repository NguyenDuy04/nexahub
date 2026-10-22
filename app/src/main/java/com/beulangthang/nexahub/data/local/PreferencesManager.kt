package com.beulangthang.nexahub.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class AppLanguage(val displayName: String) {
    VIETNAMESE("Tiếng Việt"),
    ENGLISH("English")
}

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(getSavedThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _appLanguage = MutableStateFlow(getSavedLanguage())
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit { putString(KEY_THEME_MODE, mode.name) }
        _themeMode.value = mode
    }

    fun setAppLanguage(language: AppLanguage) {
        prefs.edit { putString(KEY_LANGUAGE, language.name) }
        _appLanguage.value = language
    }

    private fun getSavedThemeMode(): ThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    private fun getSavedLanguage(): AppLanguage {
        val name = prefs.getString(KEY_LANGUAGE, AppLanguage.VIETNAMESE.name) ?: AppLanguage.VIETNAMESE.name
        return try {
            AppLanguage.valueOf(name)
        } catch (_: Exception) {
            AppLanguage.VIETNAMESE
        }
    }

    companion object {
        private const val PREFS_NAME = "nexahub_todo_prefs"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_LANGUAGE = "key_language"
    }
}
