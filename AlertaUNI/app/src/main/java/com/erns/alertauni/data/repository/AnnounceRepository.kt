package com.erns.alertauni.data.repository

import com.erns.alertauni.data.model.PostPrivateRequest
import com.erns.alertauni.data.model.PostPrivateResponse
import com.erns.alertauni.data.model.PostRequest
import com.erns.alertauni.data.model.SupabaseSimpleResponse

interface AnnounceRepository {
    suspend fun addPostPrivate(postPrivateRequest: PostPrivateRequest): Result<PostPrivateResponse>
    suspend fun addPostPublic(postRequest: PostRequest): Result<PostPrivateResponse>
}