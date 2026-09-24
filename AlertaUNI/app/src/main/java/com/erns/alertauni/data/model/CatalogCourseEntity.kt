package com.erns.alertauni.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CatalogCourseEntity(
    @SerialName("course_catalog_id") val id: String,
    @SerialName("course_code") val courseCode: String,
    @SerialName("course_name")val courseName: String
)
