package com.erns.alertauni.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GoogleAccountRequest(
    val email: String,
    val firstname: String,
    val surname: String,
    val user_type: String?
)
