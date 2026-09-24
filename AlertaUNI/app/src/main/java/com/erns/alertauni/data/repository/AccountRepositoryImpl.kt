package com.erns.alertauni.data.repository

import com.erns.alertauni.data.model.GoogleAccountRequest
import com.erns.alertauni.data.remote.AccountDataSource
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val accountDataSource: AccountDataSource,

) : AccountRepository {
    override suspend fun createAccountWithGoogle(googleAccountRequest: GoogleAccountRequest): Result<Unit> {
        return try {
            val success = accountDataSource.callCreateAccountEndpoint(googleAccountRequest)
            if (success) Result.success(Unit)
            else Result.failure(Exception("Failed to insert record via Edge Function"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkProfile(): Result<Boolean> {
        return try {
            val success = accountDataSource.callProfileEndpoint()
            if (success)
                Result.success(true)
            else
                Result.failure(Exception("Failed to insert record via Edge Function"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}