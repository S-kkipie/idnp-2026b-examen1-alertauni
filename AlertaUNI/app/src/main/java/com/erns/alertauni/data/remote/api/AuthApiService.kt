package com.erns.alertauni.data.remote.api

import com.erns.alertauni.data.model.BackendResponse
import com.erns.alertauni.data.model.FCMTokenMessage
import com.erns.alertauni.data.model.ResponseData
import com.erns.alertauni.data.model.UserAccount
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("update-fcmtoken")
    suspend fun updateFCMToken(@Body payload: FCMTokenMessage): Response<BackendResponse<ResponseData>>
    @POST("create-professor-account")
    suspend fun createProfessorAccount(@Body payload: UserAccount): Response<BackendResponse<ResponseData>>
    @POST("create-student-account")
    suspend fun createStudentAccount(@Body payload: UserAccount): Response<BackendResponse<ResponseData>>
}