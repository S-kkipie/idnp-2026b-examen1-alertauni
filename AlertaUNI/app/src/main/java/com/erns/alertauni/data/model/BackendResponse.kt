package com.erns.alertauni.data.model

data class BackendResponse<T>(
    val code: Int,
    val success: Boolean,
    val message: String,
    val data:T?
)

data class ResponseData(
    val uid: String?
)

data class NotificationData(
    val id:Int,
    val cursoId: String,
    val mensaje: String,
    val fecha: String
)