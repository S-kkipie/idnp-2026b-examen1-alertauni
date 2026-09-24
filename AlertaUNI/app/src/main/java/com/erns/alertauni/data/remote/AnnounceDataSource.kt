package com.erns.alertauni.data.remote

import com.erns.alertauni.common.safeInvoke
import com.erns.alertauni.data.model.PostPrivateRequest
import com.erns.alertauni.data.model.PostPrivateResponse
import com.erns.alertauni.data.model.PostRequest
import com.erns.alertauni.data.model.SupabaseSimpleResponse
import com.erns.alertauni.data.remote.api.ApiConstants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import javax.inject.Inject

class AnnounceDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    suspend fun addPostPrivate(postPrivateRequest: PostPrivateRequest): SupabaseSimpleResponse<PostPrivateResponse> {
        val response =
            supabaseClient.functions.safeInvoke(ApiConstants.ADD_POST_PRIVATE, postPrivateRequest)
        if (response.status.value in 200..299) {
            return response.body<SupabaseSimpleResponse<PostPrivateResponse>>()
        } else {
            throw Exception("Failed to fetch: ${response.status}")
        }
    }

    suspend fun addPostPublic(postRequest: PostRequest): SupabaseSimpleResponse<PostPrivateResponse> {
        val response =
            supabaseClient.functions.safeInvoke(ApiConstants.ADD_POST_PUBLIC, postRequest)
        if (response.status.value in 200..299) {
            return response.body<SupabaseSimpleResponse<PostPrivateResponse>>()
        } else {
            throw Exception("Failed to fetch: ${response.status}")
        }

    }
}