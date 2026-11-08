package com.erns.alertauni.domain.post

/** A quién llega un anuncio. Da el texto del botón de envío y del aviso posterior. */
data class Audience(
    val courseCode: String,
    val studentCount: Int? = null,
    val recipientName: String? = null,
) {
    val sendLabel: String
        get() = when {
            recipientName != null -> "Enviar sólo a $recipientName"
            hasCount -> "Enviar a ${students()} de $courseCode"
            else -> "Publicar en $courseCode"
        }

    val sentMessage: String
        get() = when {
            recipientName != null -> "Anuncio enviado a $recipientName"
            hasCount -> "Anuncio enviado a ${students()} de $courseCode"
            else -> "Anuncio publicado en $courseCode"
        }

    private val hasCount: Boolean get() = (studentCount ?: 0) > 0

    private fun students() = if (studentCount == 1) "1 estudiante" else "$studentCount estudiantes"
}
