package com.erns.alertauni.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Código de inscripción de un curso, visible sólo para su docente. */
@Serializable
data class ClassCodeInfo(
    @SerialName("course_catalog_id") val courseCatalogId: String,
    @SerialName("class_code") val classCode: String,
    @SerialName("class_code_enable") val enabled: Boolean,
    @SerialName("class_code_expires_at") val expiresAt: String? = null,
    @SerialName("enrolled_count") val enrolledCount: Int = 0,
    @SerialName("roster_count") val rosterCount: Int = 0,
    @SerialName("pending_count") val pendingCount: Int = 0
)
