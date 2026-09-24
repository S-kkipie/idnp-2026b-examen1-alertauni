package com.erns.alertauni.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseEnrollRequest(
    @SerialName("course_catalog_id") val courseCatalogId: String
)
