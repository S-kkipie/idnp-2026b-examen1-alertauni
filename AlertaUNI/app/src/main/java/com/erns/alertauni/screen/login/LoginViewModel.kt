package com.erns.alertauni.screen.login

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erns.alertauni.data.model.FCMTokenMessage
import com.erns.alertauni.data.model.GoogleAccountRequest
import com.erns.alertauni.data.repository.AuthRepository
import com.erns.alertauni.domain.manager.DataStoreHelper
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val application: Application,
    private val authRepository: AuthRepository,
    private val dataStoreHelper: DataStoreHelper
) : AndroidViewModel(application) {
    private val TAG = "LoginViewModel"

    sealed class AuthState {
        object Loading : AuthState()
        object Unauthenticated : AuthState()
        object IncompleteProfile : AuthState() // Necesita elegir Rol
        object CompleteProfile : AuthState() // Necesita elegir Rol
        object Authenticated : AuthState()     // Flujo completo
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState
    private val _googleAccountRequest = MutableStateFlow<GoogleAccountRequest?>(null)
    val googleAccountRequest: StateFlow<GoogleAccountRequest?> = _googleAccountRequest.asStateFlow()
    private val _rememberMe = MutableStateFlow(false)
    val rememberMe: StateFlow<Boolean> = _rememberMe.asStateFlow()

    val userTypeMenu = mapOf(
        "PROFESSOR" to "Docente",
        "STUDENT" to "Estudiante"
    )

    init {
        viewModelScope.launch {
            dataStoreHelper.clearUserType()
        }
    }

    fun startSessionWithGoogle(context: Context) {
        Log.d(TAG, "Iniciando sesión con Google...")
        val credentialManager = CredentialManager.create(context)

        // 1. Configurar las opciones de Google ID
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // <-- ¡Cámbialo a FALSE para que no falle en el emulador!
            .setServerClientId("12345678-abc.apps.googleusercontent.com")
            .setAutoSelectEnabled(false) // Evita que parpadee seleccionando una cuenta automáticamente si hay varias
            .build()

        // 2. Crear la solicitud general
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // 3. Ejecutar la solicitud dentro de una corrutina
        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                // 4. Extraer la credencial de Google
                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(result.credential.data)

                val googleIdToken = googleIdTokenCredential.idToken
                val firstname = googleIdTokenCredential.givenName ?: ""
                val surname = googleIdTokenCredential.familyName ?: ""
                Log.d(TAG, googleIdToken)
                Log.d(TAG, googleIdTokenCredential.familyName.toString())
                Log.d(TAG, googleIdTokenCredential.givenName.toString())
                Log.d(TAG, googleIdTokenCredential.id)

                authRepository.signInSupaWithGoogle(googleIdToken).onSuccess {
                    val email = it.email ?: ""
                    dataStoreHelper.saveEmail(email)
                    dataStoreHelper.setFirstname(firstname)
                    dataStoreHelper.setSurname(surname)
                    authRepository.checkProfile().onSuccess { profile ->
                        Log.d(TAG, "Success check profile user_email: ${profile.user_email}")
                        Log.d(TAG, "Success check profile user_type: ${profile.user_type}")

                        dataStoreHelper.setUserType(profile.user_type)
                        _authState.value = AuthState.Authenticated

                    }.onFailure {
                        Log.e(TAG, "Fail check profile: ${it.message}")
                        _authState.value = AuthState.IncompleteProfile
                    }

                }.onFailure { e ->
                    Log.e(TAG, "Error signing in with Google $e")
                    _authState.value = AuthState.Unauthenticated
                }

            } catch (e: NoCredentialException) {
                Log.e(TAG, "No credential found", e)
                _authState.value = AuthState.Unauthenticated
            } catch (e: GetCredentialException) {
                e.printStackTrace()
            }
        }
    }

    fun getUserInfo() {
        viewModelScope.launch {
            val googleAccountRequest = GoogleAccountRequest(
                email = dataStoreHelper.getEmail(),
                firstname = dataStoreHelper.getFirstname(),
                surname = dataStoreHelper.getSurname(),
                user_type = dataStoreHelper.getUserType()
            )
            _googleAccountRequest.value = googleAccountRequest
        }

    }

    fun updateUserProfile(googleAccountRequest: GoogleAccountRequest) {
        viewModelScope.launch {
            authRepository.updateProfile(googleAccountRequest).onSuccess {
                Log.d(TAG, "Se actualizo correctamente ${it}")
                _authState.value = AuthState.CompleteProfile
            }.onFailure {
                Log.d(TAG, "Fallo actualizando el perfil ${it}")
                _authState.value = AuthState.IncompleteProfile
            }
        }
    }

    private fun checkSession2() {
        viewModelScope.launch {
            val remember = dataStoreHelper.getRememberMe()
            _rememberMe.value = remember
        }
    }

    fun setRememberMe(value: Boolean) {
        _rememberMe.value = value
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreHelper.saveRememberMe(value)
        }
    }


    private suspend fun updateFCMToken() {
        withContext(Dispatchers.IO) {
            try {
                val fcmToken = dataStoreHelper.getFCMToken()
                val payload = FCMTokenMessage(fcmToken = fcmToken)
            } catch (e: Exception) {
                Log.e("FCMTokenUpdate", "Error updating FCM token: ${e.message}")
            }
        }
    }

}