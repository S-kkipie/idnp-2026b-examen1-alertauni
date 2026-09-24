package com.erns.alertauni.data.remote.api

import com.erns.alertauni.data.model.BackendResponse
import com.erns.alertauni.data.model.FCMTokenMessage
import com.erns.alertauni.data.model.NotificationData
import com.erns.alertauni.data.model.NotificationPayload
import com.erns.alertauni.data.model.ResponseData
import com.erns.alertauni.data.model.UserAccount
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface HomeApiService {
    @POST("exec") // si tu endpoint termina en /exec
    suspend fun enviarDatos(@Body payload: FCMTokenMessage): Response<BackendResponse<ResponseData>>
    @POST("notifis")
    suspend fun notifications(@Body payload: NotificationPayload): Response<BackendResponse<List<NotificationData>>>
    @POST("update-fcmtoken") // si tu endpoint termina en /exec
    suspend fun updateFCMToken(@Body payload: FCMTokenMessage): Response<BackendResponse<ResponseData>>

    @POST("create-account")
    suspend fun createUserAccount(@Body payload: UserAccount): Response<BackendResponse<ResponseData>>
}