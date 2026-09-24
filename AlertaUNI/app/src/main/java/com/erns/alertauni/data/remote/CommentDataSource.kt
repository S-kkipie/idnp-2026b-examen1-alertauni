package com.erns.alertauni.data.remote

import com.erns.alertauni.data.model.CommentDto
import com.erns.alertauni.data.model.CommentRequest
import com.erns.alertauni.data.model.CommentResponse
import com.erns.alertauni.data.model.SupabaseListResponse
import com.erns.alertauni.data.remote.api.ApiConstants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import javax.inject.Inject

class CommentDataSource@Inject constructor(
    private val supabaseClient: SupabaseClient
)
{
    suspend fun callInsertEndpoint(commentRequest: CommentRequest): Boolean {
        val response = supabaseClient.functions.invoke(ApiConstants.ADD_COMMENT, commentRequest)
        return response.status.value in 200..299
    }

    suspend fun callCommentsEndpoint(commentDto: CommentDto): SupabaseListResponse<CommentResponse> {
        val response = supabaseClient.functions.invoke(ApiConstants.GET_COMMENTS,commentDto)
        if (response.status.value in 200..299) {
            return response.body<SupabaseListResponse<CommentResponse>>()
        } else {
            throw Exception("Failed to fetch: ${response.status}")
        }
    }

    suspend fun callAddPrivateCommentEndpoint(commentRequest: CommentRequest): Boolean {
        val response = supabaseClient.functions.invoke(ApiConstants.ADD_COMMENT_PRIVATE, commentRequest)
        return response.status.value in 200..299
    }

}