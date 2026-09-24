package com.erns.alertauni.domain.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class DataStoreHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val GOOGLE_TOKEN_KEY = stringPreferencesKey("GOOGLE_TOKEN_KEY")
        val SESSION_TOKEN_KEY = stringPreferencesKey("SESSION_TOKEN_KEY")
        var USER_UID_KEY = stringPreferencesKey("USER_UID_KEY")
        val FCM_TOKEN_KEY = stringPreferencesKey("FCM_TOKEN_KEY")
        val USER_EMAIL = stringPreferencesKey("USER_EMAIL")
        val USER_FIRSTNAME = stringPreferencesKey("USER_FIRSTNAME")
        val USER_SURNAME = stringPreferencesKey("USER_SURNAME")

        val USER_TYPE = stringPreferencesKey("USER_TYPE")

        //        val USER_TYPE = intPreferencesKey("USER_TYPE")
        val REMEMBER_ME = booleanPreferencesKey("REMEMBER_ME")
    }

    suspend fun saveUserUid(userId: String) {
        dataStore.edit { preferences ->
            preferences[USER_UID_KEY] = userId
        }
    }

    suspend fun getUserUid(): String {
        return dataStore.data.map { preferences ->
            preferences[USER_UID_KEY] ?: ""
        }.first()
    }

    suspend fun clearUserUid() {
        dataStore.edit { preferences ->
            preferences.remove(USER_UID_KEY)
        }
    }

    suspend fun saveSessionToken(token: String) {
        dataStore.edit { preferences ->
            preferences[SESSION_TOKEN_KEY] = token
        }
    }

    suspend fun getSessionToken(): String {
        return dataStore.data.map { preferences ->
            preferences[SESSION_TOKEN_KEY] ?: ""
        }.first()
    }

    suspend fun clearSessionToken() {
        dataStore.edit { preferences ->
            preferences.remove(SESSION_TOKEN_KEY)
        }
    }

    suspend fun saveFCMToken(token: String) {
        dataStore.edit { preferences ->
            preferences[FCM_TOKEN_KEY] = token
        }
    }

    suspend fun getFCMToken(): String {
        return dataStore.data.map { preferences ->
            preferences[FCM_TOKEN_KEY] ?: ""
        }.first()
    }

    suspend fun clearFCMToken() {
        dataStore.edit { preferences ->
            preferences.remove(FCM_TOKEN_KEY)
        }
    }

    suspend fun saveEmail(email: String) {
        dataStore.edit { preferences ->
            preferences[USER_EMAIL] = email
        }
    }

    suspend fun getEmail(): String {
        return dataStore.data.map { preferences ->
            preferences[USER_EMAIL] ?: ""
        }.first()
    }

    suspend fun clearEmail() {
        dataStore.edit { preferences ->
            preferences.remove(USER_EMAIL)
        }
    }

    suspend fun setFirstname(firstname: String) {
        dataStore.edit { preferences ->
            preferences[USER_FIRSTNAME] = firstname
        }
    }

    suspend fun getFirstname(): String {
        return dataStore.data.map { preferences ->
            preferences[USER_FIRSTNAME] ?: ""
        }.first()
    }

    suspend fun setSurname(surname: String) {
        dataStore.edit { preferences ->
            preferences[USER_SURNAME] = surname
        }
    }

    suspend fun getSurname(): String {
        return dataStore.data.map { preferences ->
            preferences[USER_SURNAME] ?: ""
        }.first()
    }

    suspend fun setUserType(userType: String) {
        dataStore.edit { preferences ->
            preferences[USER_TYPE] = userType
        }
    }

    suspend fun getUserType(): String? {
        return dataStore.data.map { preferences ->
            preferences[USER_TYPE]
        }.first()
    }

    suspend fun clearUserType() {
        dataStore.edit { preferences ->
            preferences.remove(USER_TYPE)
        }
    }

    suspend fun saveRememberMe(remember: Boolean) {
        dataStore.edit { preferences ->
            preferences[REMEMBER_ME] = remember
        }
    }

    suspend fun getRememberMe(): Boolean {
        return dataStore.data.map { preferences ->
            preferences[REMEMBER_ME] ?: false
        }.first()
    }
}
