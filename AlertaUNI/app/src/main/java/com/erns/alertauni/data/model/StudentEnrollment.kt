package com.erns.alertauni.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudentEnrollment(
    @SerialName("course_catalog_id") val course_catalog_id: String,
    @SerialName("course_id") val courseId: String,
    @SerialName("course_code") val courseCode: String,
    @SerialName("course_name") val courseName: String,
    @SerialName("semester") val semester: String,
    @SerialName("course_type") val courseType: String,
    @SerialName("group_type") val groupType: String,
    @SerialName("firstname") val firstname: String,
    @SerialName("surname") val surname: String,
    @SerialName("email") val email: String
)
