package com.erns.alertauni.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BackendError(
    @SerialName("code")val code:String,
    @SerialName("message")val message:String
)
class BackendException(val errorData: BackendError) : Exception(errorData.message)