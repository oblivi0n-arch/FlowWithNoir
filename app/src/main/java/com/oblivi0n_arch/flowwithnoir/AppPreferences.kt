package com.oblivi0n_arch.flowwithnoir

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "app_preferences")

class AppPreferences(private val context: Context) {

    private val isFirstLaunchKey = booleanPreferencesKey("is_first_launch")

    suspend fun isFirstLaunch(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[isFirstLaunchKey] ?: true
    }

    suspend fun setFirstLaunchDone() {
        context.dataStore.edit { preferences ->
            preferences[isFirstLaunchKey] = false
        }
    }
}