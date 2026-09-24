package com.erns.alertauni.data.remote.api

import com.erns.alertauni.data.model.ChatMessage
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApiService {
    @POST("send-message")
    suspend fun sendMessage(@Body payload: ChatMessage): Response<Unit>
}