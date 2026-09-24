package com.erns.alertauni.data.repository

import android.util.Log
import com.erns.alertauni.common.AppEvent
import com.erns.alertauni.common.AppEventManager
import com.erns.alertauni.data.model.BackendException
import com.erns.alertauni.data.model.GoogleAccountRequest
import com.erns.alertauni.data.model.UserProfile
import com.erns.alertauni.data.model.UserProfileResponse
import com.erns.alertauni.data.remote.AuthDataSource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.gotrue.user.UserInfo
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val authDataSource: AuthDataSource
) : AuthRepository {

    override suspend fun signInSupaWithGoogle(googleIdToken: String): Result<UserInfo> {
        return try {
            supabaseClient.auth.signInWith(IDToken) {
                idToken = googleIdToken
                provider = Google
            }
            // 2. If it didn't throw an error, your session is now active!
            // Grab the fully parsed user details profile (ID, email, metadata, etc.)
            val user = supabaseClient.auth.currentUserOrNull()
                ?: throw Exception("Session created, but user data is missing")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkProfile(): Result<UserProfile> {
        return try {
            val success = authDataSource.callCheckProfileEndpoint()
            Result.success(success.data)
        } catch (e: BackendException) {
            if (e.errorData.code == "PROFILE_NOT_FOUND") {
                // Disparamos el evento global de navegación asíncronamente
                AppEventManager.emit(AppEvent.NavigateToCompleteProfile)
            }
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(googleAccountRequest: GoogleAccountRequest): Result<UserProfileResponse> {
        return try {
            val success = authDataSource.callUpdateProfileEndpoint(googleAccountRequest)
            Result.success(success.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}