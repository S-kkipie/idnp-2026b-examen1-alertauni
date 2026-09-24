package com.erns.alertauni.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    @SerialName("email") val email: String,
    @SerialName("user_type") val userType: Int,
    @SerialName("created_at") val createdAt: String
)
