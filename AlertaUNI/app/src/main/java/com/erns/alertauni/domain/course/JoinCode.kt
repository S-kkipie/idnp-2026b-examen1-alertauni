package com.erns.alertauni.domain.course

import java.net.URI

/**
 * Formato compartido del código de inscripción.
 *
 * El QR y el código alfanumérico transportan el MISMO valor: el QR contiene
 * `alertauni://join?code=<CODIGO>` y el estudiante también puede escribir `<CODIGO>`.
 * Así el escáner es sólo un atajo y la validación en el servidor es única.
 */
object JoinCode {
    const val SCHEME = "alertauni"
    const val HOST = "join"
    private val CODE_REGEX = Regex("^[A-Za-z0-9]{3,10}(-[A-Za-z0-9]{2,10})?$")

    /** Contenido que se codifica en el QR del docente. */
    fun toQrContent(code: String): String = "$SCHEME://$HOST?code=${code.trim()}"

    /**
     * Obtiene el código a partir de lo que escribió el estudiante o de lo que leyó el escáner.
     * Devuelve null si el texto no corresponde a un código de AlertaUNI.
     */
    fun parse(raw: String?): String? {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty()) return null

        val candidate = if (text.startsWith("$SCHEME://", ignoreCase = true)) {
            codeFromUri(text) ?: return null
        } else {
            text
        }
        return candidate.takeIf { CODE_REGEX.matches(it) }
    }

    private fun codeFromUri(text: String): String? = try {
        val uri = URI(text)
        if (!uri.host.equals(HOST, ignoreCase = true)) null
        else uri.rawQuery
            ?.split("&")
            ?.map { it.split("=", limit = 2) }
            ?.firstOrNull { it.size == 2 && it[0] == "code" }
            ?.get(1)
            ?.trim()
    } catch (e: Exception) {
        null
    }
}
