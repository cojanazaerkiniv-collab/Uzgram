package com.uzgram.messenger.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

@Singleton
class SessionPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val AVATAR_URL = stringPreferencesKey("avatar_url")
        val IS_ADMIN = booleanPreferencesKey("is_admin")
        val THEME = stringPreferencesKey("theme") // light | dark | system
        val LANGUAGE = stringPreferencesKey("language")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val SCREEN_LOCK_ENABLED = booleanPreferencesKey("screen_lock_enabled")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[Keys.ACCESS_TOKEN] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[Keys.REFRESH_TOKEN] }
    val userId: Flow<String?> = context.dataStore.data.map { it[Keys.USER_ID] }
    val username: Flow<String?> = context.dataStore.data.map { it[Keys.USERNAME] }
    val displayName: Flow<String?> = context.dataStore.data.map { it[Keys.DISPLAY_NAME] }
    val avatarUrl: Flow<String?> = context.dataStore.data.map { it[Keys.AVATAR_URL] }
    val isAdmin: Flow<Boolean> = context.dataStore.data.map { it[Keys.IS_ADMIN] ?: false }
    val theme: Flow<String> = context.dataStore.data.map { it[Keys.THEME] ?: "system" }
    val language: Flow<String> = context.dataStore.data.map { it[Keys.LANGUAGE] ?: "uz" }
    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.BIOMETRIC_ENABLED] ?: false }

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        userId: String,
        username: String,
        displayName: String,
        avatarUrl: String?,
        isAdmin: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = accessToken
            prefs[Keys.REFRESH_TOKEN] = refreshToken
            prefs[Keys.USER_ID] = userId
            prefs[Keys.USERNAME] = username
            prefs[Keys.DISPLAY_NAME] = displayName
            avatarUrl?.let { prefs[Keys.AVATAR_URL] = it }
            prefs[Keys.IS_ADMIN] = isAdmin
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.ACCESS_TOKEN)
            prefs.remove(Keys.REFRESH_TOKEN)
            prefs.remove(Keys.USER_ID)
            prefs.remove(Keys.USERNAME)
            prefs.remove(Keys.DISPLAY_NAME)
            prefs.remove(Keys.AVATAR_URL)
            prefs[Keys.IS_ADMIN] = false
        }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[Keys.THEME] = theme }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }
}
