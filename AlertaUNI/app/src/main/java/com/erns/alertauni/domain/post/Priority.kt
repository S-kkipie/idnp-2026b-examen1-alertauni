package com.erns.alertauni.domain.post

/**
 * Prioridad de un anuncio. El backend aún no la guarda: viaja como campo opcional
 * `priority` y, si falta o no se reconoce, se asume MEDIA.
 */
enum class Priority(val label: String, val helper: String) {
    ALTA("Alta", "Llega como notificación inmediata"),
    MEDIA("Media", "Notificación normal"),
    BAJA("Baja", "Sólo aparece en el muro, sin notificación");

    /** Valor que se envía al backend. */
    val code: String get() = name

    companion object {
        fun fromCode(code: String?): Priority {
            val value = code?.trim().orEmpty()
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true)
            } ?: MEDIA
        }
    }
}
