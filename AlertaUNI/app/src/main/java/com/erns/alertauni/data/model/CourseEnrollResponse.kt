package com.erns.alertauni.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseEnrollResponse(
    @SerialName("created_at") val created: String,
    // ENROLLED: incorporado directamente (figura en la lista de matriculados)
    // PENDING: solicitud enviada al docente para su aprobación
    @SerialName("status") val status: String = STATUS_ENROLLED
) {
    companion object {
        const val STATUS_ENROLLED = "ENROLLED"
        const val STATUS_PENDING = "PENDING"
    }
}
