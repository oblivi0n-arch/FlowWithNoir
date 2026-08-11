package com.oblivi0n_arch.flowwithnoir

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.oblivi0n_arch.flowwithnoir.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_preferences")

data class ReminderSettings(
    val isEnabled: Boolean = false,
    val hour: Int = 20,
    val minute: Int = 0,
    val skipAfterSession: Boolean = true
) {
    val timeLabel: String
        get() = "%02d:%02d".format(hour, minute)
}

class AppPreferences(private val context: Context) {

    private val isFirstLaunchKey = booleanPreferencesKey("is_first_launch")
    private val nicknameKey = stringPreferencesKey("nickname")
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val reminderEnabledKey = booleanPreferencesKey("reminder_enabled")
    private val reminderHourKey = intPreferencesKey("reminder_hour")
    private val reminderMinuteKey = intPreferencesKey("reminder_minute")
    private val reminderSkipKey = booleanPreferencesKey("reminder_skip_after_session")

    suspend fun isFirstLaunch(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[isFirstLaunchKey] ?: true
    }

    suspend fun setFirstLaunchDone() {
        context.dataStore.edit { preferences ->
            preferences[isFirstLaunchKey] = false
        }
    }

    fun nicknameFlow(): Flow<String?> =
        context.dataStore.data.map { preferences -> preferences[nicknameKey] }

    suspend fun setNickname(nickname: String) {
        context.dataStore.edit { preferences ->
            preferences[nicknameKey] = nickname
        }
    }

    fun themeModeFlow(): Flow<ThemeMode> =
        context.dataStore.data.map { preferences ->
            preferences[themeModeKey]?.let { stored ->
                runCatching { ThemeMode.valueOf(stored) }.getOrNull()
            } ?: ThemeMode.SYSTEM
        }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[themeModeKey] = themeMode.name
        }
    }

    fun reminderSettingsFlow(): Flow<ReminderSettings> =
        context.dataStore.data.map { preferences ->
            ReminderSettings(
                isEnabled = preferences[reminderEnabledKey] ?: false,
                hour = preferences[reminderHourKey] ?: 20,
                minute = preferences[reminderMinuteKey] ?: 0,
                skipAfterSession = preferences[reminderSkipKey] ?: true
            )
        }

    suspend fun reminderSettings(): ReminderSettings = reminderSettingsFlow().first()

    suspend fun setReminderEnabled(isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[reminderEnabledKey] = isEnabled
        }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[reminderHourKey] = hour
            preferences[reminderMinuteKey] = minute
        }
    }

    suspend fun setSkipAfterSession(skip: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[reminderSkipKey] = skip
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
