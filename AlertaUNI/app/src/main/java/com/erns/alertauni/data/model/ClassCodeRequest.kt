package com.erns.alertauni.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClassCodeRequest (
    @SerialName("class_code") val classCode:String
)