package com.erns.alertauni.data.remote

import com.erns.alertauni.data.model.CommentRequest
import com.erns.alertauni.data.model.GoogleAccountRequest
import com.erns.alertauni.data.remote.api.ApiConstants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import javax.inject.Inject

class AccountDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    suspend fun callCreateAccountEndpoint(googleAccountRequest: GoogleAccountRequest): Boolean {
        val response = supabaseClient.functions.invoke(ApiConstants.CREATE_ACCOUNT, googleAccountRequest)
        return response.status.value in 200..299
    }
    suspend fun callProfileEndpoint(): Boolean {
        val response = supabaseClient.functions.invoke(ApiConstants.CHECK_PROFILE)
        return response.status.value in 200..299
    }
}