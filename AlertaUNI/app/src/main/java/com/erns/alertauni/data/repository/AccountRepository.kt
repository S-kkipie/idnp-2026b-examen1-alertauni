package com.erns.alertauni.data.repository

import com.erns.alertauni.data.model.GoogleAccountRequest

interface AccountRepository {
    suspend fun createAccountWithGoogle(googleAccountRequest: GoogleAccountRequest): Result<Unit>
    suspend fun checkProfile(): Result<Boolean>
}