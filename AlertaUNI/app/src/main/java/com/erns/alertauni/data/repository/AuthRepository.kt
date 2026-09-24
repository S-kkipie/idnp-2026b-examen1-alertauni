package com.erns.alertauni.data.repository

import com.erns.alertauni.data.model.GoogleAccountRequest
import com.erns.alertauni.data.model.UserProfile
import com.erns.alertauni.data.model.UserProfileResponse
import io.github.jan.supabase.gotrue.user.UserInfo

interface AuthRepository {
    suspend fun signInSupaWithGoogle(googleIdToken: String): Result<UserInfo>
    suspend fun checkProfile(): Result<UserProfile>
    suspend fun updateProfile(googleAccountRequest: GoogleAccountRequest): Result<UserProfileResponse>
}