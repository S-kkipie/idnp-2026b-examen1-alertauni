package com.erns.alertauni.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseEnrollResponse(
    @SerialName("created_at") val created:String
)
