package com.uzgram.messenger.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.uzgram.messenger.data.remote.dto.UserDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore("uzgram_user_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    private object Keys {
        val ACCESS_TOKEN  = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val CURRENT_USER  = stringPreferencesKey("current_user_json")
        val LANGUAGE      = stringPreferencesKey("app_language")
        val THEME         = stringPreferencesKey("app_theme")
        val FCM_TOKEN     = stringPreferencesKey("fcm_token")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val SCREEN_LOCK   = booleanPreferencesKey("screen_lock_enabled")
    }

    val accessToken: Flow<String?> = context.userDataStore.data
        .map { it[Keys.ACCESS_TOKEN] }

    val refreshToken: Flow<String?> = context.userDataStore.data
        .map { it[Keys.REFRESH_TOKEN] }

    val currentUser: Flow<UserDto?> = context.userDataStore.data
        .map { prefs ->
            prefs[Keys.CURRENT_USER]?.let {
                try { gson.fromJson(it, UserDto::class.java) } catch (_: Exception) { null }
            }
        }

    val language: Flow<String> = context.userDataStore.data
        .map { it[Keys.LANGUAGE] ?: "uz" }

    val theme: Flow<String> = context.userDataStore.data
        .map { it[Keys.THEME] ?: "system" }

    val biometricEnabled: Flow<Boolean> = context.userDataStore.data
        .map { it[Keys.BIOMETRIC_ENABLED] ?: false }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.userDataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN]  = accessToken
            prefs[Keys.REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun saveCurrentUser(user: UserDto) {
        context.userDataStore.edit { prefs ->
            prefs[Keys.CURRENT_USER] = gson.toJson(user)
        }
    }

    suspend fun saveAccessToken(token: String) {
        context.userDataStore.edit { prefs -> prefs[Keys.ACCESS_TOKEN] = token }
    }

    suspend fun saveFcmToken(token: String) {
        context.userDataStore.edit { prefs -> prefs[Keys.FCM_TOKEN] = token }
    }

    suspend fun setLanguage(lang: String) {
        context.userDataStore.edit { prefs -> prefs[Keys.LANGUAGE] = lang }
    }

    suspend fun setTheme(theme: String) {
        context.userDataStore.edit { prefs -> prefs[Keys.THEME] = theme }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.userDataStore.edit { prefs -> prefs[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun clearAll() {
        context.userDataStore.edit { it.clear() }
    }

    suspend fun clearTokens() {
        context.userDataStore.edit { prefs ->
            prefs.remove(Keys.ACCESS_TOKEN)
            prefs.remove(Keys.REFRESH_TOKEN)
            prefs.remove(Keys.CURRENT_USER)
        }
    }
}
