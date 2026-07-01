package com.raf.fieldops.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemePreference {
    System,
    Light,
    Dark
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "fieldops_settings"
)

@Singleton
open class ThemeDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val themeKey = stringPreferencesKey("theme_preference")

    open val themePreference: Flow<ThemePreference> = context.dataStore.data
        .map { preferences ->
            val name = preferences[themeKey] ?: ThemePreference.System.name
            try {
                ThemePreference.valueOf(name)
            } catch (_: IllegalArgumentException) {

                ThemePreference.System
            }
        }

    suspend fun setThemePreference(preference: ThemePreference) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = preference.name
        }
    }
}
