package com.unistay.android.data.local.datastore

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

// Khai báo DataStore delegate extension tường minh kiểu dữ liệu
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

@Singleton
class UserSessionManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        val USER_AVATAR_KEY = stringPreferencesKey("user_avatar")
        val USER_PHONE_KEY = stringPreferencesKey("user_phone")
        val USER_ROLE_KEY = stringPreferencesKey("user_role")
        val PREF_NOTIFICATIONS = androidx.datastore.preferences.core.booleanPreferencesKey("pref_notifications")
    }

    suspend fun saveSettings(notifications: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PREF_NOTIFICATIONS] = notifications
        }
    }

    val isNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PREF_NOTIFICATIONS] ?: true // Mặc định bật
    }

    suspend fun saveUserSession(userId: String, name: String, email: String, avatarUrl: String? = null, phone: String? = null, role: String? = null) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_NAME_KEY] = name
            preferences[USER_EMAIL_KEY] = email
            if (avatarUrl != null) preferences[USER_AVATAR_KEY] = avatarUrl
            if (phone != null) preferences[USER_PHONE_KEY] = phone
            if (role != null) preferences[USER_ROLE_KEY] = role
        }
    }

    suspend fun updateProfile(name: String, avatarUrl: String?, phone: String?) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
            if (avatarUrl != null) preferences[USER_AVATAR_KEY] = avatarUrl
            if (phone != null) preferences[USER_PHONE_KEY] = phone
        }
    }

    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    val userRole: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ROLE_KEY]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }

    val userAvatar: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_AVATAR_KEY]?.replace(Regex("https?://[^/]+(:5148)?/"), "http://tro24h.runasp.net/")
    }

    val userPhone: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_PHONE_KEY]
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}