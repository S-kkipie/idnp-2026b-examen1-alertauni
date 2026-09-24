package com.erns.alertauni.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudentEntity(
    @SerialName("course_catalog_id") val course_catalog_id: String,
    @SerialName("student_id") val student_id: String,
    @SerialName("codigo") val codigo: String,
    @SerialName("firstname") val firstname: String,
    @SerialName("surname") val surname: String,
    @SerialName("email") val email: String
)