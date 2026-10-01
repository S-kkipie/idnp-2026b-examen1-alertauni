package com.erns.alertauni.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Solicitud de un estudiante que no figura en la lista oficial de matriculados. */
@Serializable
data class EnrollmentRequest(
    @SerialName("request_id") val requestId: Long,
    @SerialName("course_catalog_id") val courseCatalogId: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("email") val email: String,
    @SerialName("firstname") val firstname: String? = null,
    @SerialName("surname") val surname: String? = null,
    @SerialName("created_at") val createdAt: String = ""
) {
    val displayName: String
        get() = listOfNotNull(firstname, surname).joinToString(" ").ifBlank { email }
}

@Serializable
data class EnrollmentRequestAction(
    @SerialName("course_catalog_id") val courseCatalogId: String,
    @SerialName("action") val action: String,
    @SerialName("request_id") val requestId: Long? = null
) {
    companion object {
        const val LIST = "LIST"
        const val APPROVE = "APPROVE"
        const val REJECT = "REJECT"
    }
}

@Serializable
data class EnrollmentRequestReview(
    @SerialName("request_id") val requestId: Long,
    @SerialName("status") val status: String
)
