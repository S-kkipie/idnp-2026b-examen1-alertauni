package com.erns.alertauni.data.remote

import com.erns.alertauni.common.safeInvoke
import com.erns.alertauni.data.model.CatalogCourseEntity
import com.erns.alertauni.data.model.GoogleAccountRequest
import com.erns.alertauni.data.model.SupabaseListResponse
import com.erns.alertauni.data.model.SupabaseSimpleResponse
import com.erns.alertauni.data.model.UserProfile
import com.erns.alertauni.data.model.UserProfileResponse
import com.erns.alertauni.data.remote.api.ApiConstants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import javax.inject.Inject

class AuthDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    suspend fun callCheckProfileEndpoint(): SupabaseSimpleResponse<UserProfile> {
        val response = supabaseClient.functions.safeInvoke(ApiConstants.CHECK_PROFILE)
        return response.body<SupabaseSimpleResponse<UserProfile>>()
    }

    suspend fun callUpdateProfileEndpoint(googleAccountRequest: GoogleAccountRequest): SupabaseSimpleResponse<UserProfileResponse> {
        val response =
            supabaseClient.functions.safeInvoke(ApiConstants.UPDATE_PROFILE, googleAccountRequest)
        if (response.status.value in 200..299) {
            return response.body<SupabaseSimpleResponse<UserProfileResponse>>()
        } else {
            throw Exception("Failed to fetch: ${response.status}")
        }
    }
}