package com.erns.alertauni.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClassCodeRequest (
    @SerialName("class_code") val classCode:String
)

/** Acciones del docente sobre el código de inscripción de su curso. */
@Serializable
data class ClassCodeActionRequest(
    @SerialName("course_catalog_id") val courseCatalogId: String,
    @SerialName("action") val action: String
) {
    companion object {
        const val GET = "GET"
        const val REGENERATE = "REGENERATE"
        const val OPEN = "OPEN"
        const val CLOSE = "CLOSE"
    }
}
