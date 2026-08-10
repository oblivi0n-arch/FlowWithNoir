package com.oblivi0n_arch.flowwithnoir

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_preferences")

class AppPreferences(private val context: Context) {

    private val isFirstLaunchKey = booleanPreferencesKey("is_first_launch")
    private val nicknameKey = stringPreferencesKey("nickname")

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
}