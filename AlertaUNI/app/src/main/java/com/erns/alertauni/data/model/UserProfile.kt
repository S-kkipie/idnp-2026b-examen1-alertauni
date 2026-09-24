package com.erns.alertauni.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    @SerialName("user_email") val user_email:String,
    @SerialName("user_type")val user_type:String
)
