package com.erns.alertauni.data.model


data class UserAccount(
    val email: String,
    val firstname: String,
    val lastname: String,
    val accountId:String,
    val fcmToken: String,
    val googleIdToken: String,
    val phone: String,
    val type: String
)