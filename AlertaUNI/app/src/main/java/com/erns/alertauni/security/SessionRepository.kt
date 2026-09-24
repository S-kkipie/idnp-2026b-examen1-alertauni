package com.erns.alertauni.security

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "session_prefs")

@Singleton
class SessionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("encrypted_token")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_CODE_KEY = stringPreferencesKey("user_code")
        private val USER_TYPE_KEY = intPreferencesKey("user_type")
    }

    // Escucha en tiempo real si el token cambia (Desencriptándolo al vuelo)
    val tokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        val encryptedToken = preferences[TOKEN_KEY]
        if (!encryptedToken.isNullOrEmpty()) {
            CryptoManager.decrypt(encryptedToken)
        } else null
    }

    // Datos no sensibles pueden guardarse en texto plano
    val userNameFlow: Flow<String?> = context.dataStore.data.map { it[USER_NAME_KEY] }
    val userEmailFlow: Flow<String?> = context.dataStore.data.map { it[USER_EMAIL_KEY] }
    val userCodeFlow: Flow<String?> = context.dataStore.data.map { it[USER_CODE_KEY] }
    val userTypeFlow: Flow<Int?> = context.dataStore.data.map { it[USER_TYPE_KEY] }

    suspend fun saveSession(token: String, nombre: String) {
        val tokenEncriptado = CryptoManager.encrypt(token)
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = tokenEncriptado
            preferences[USER_NAME_KEY] = nombre
        }
    }

    suspend fun closeSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
