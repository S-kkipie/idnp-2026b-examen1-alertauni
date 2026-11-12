package com.erns.alertauni.domain.post

import com.erns.alertauni.data.model.DraftEntity
import java.time.Instant
import java.time.ZonedDateTime

enum class RecipientMode { COURSE, STUDENT }

/** Lo que el usuario escribe en la hoja de nuevo anuncio. [id] = 0 si aún no es un borrador guardado. */
data class DraftForm(
    val id: Long = 0,
    val mode: RecipientMode = RecipientMode.COURSE,
    val courseCatalogId: String? = null,
    val courseCode: String = "",
    val recipientStudentId: String? = null,
    val recipientName: String? = null,
    val priority: Priority = Priority.MEDIA,
    val title: String = "",
    val content: String = "",
    val allowComments: Boolean = true,
) {
    val isSaved: Boolean get() = id != 0L
    val hasText: Boolean get() = title.isNotBlank() || content.isNotBlank()
}

/** Borrador listo para la lista "Borradores". */
data class DraftSummary(
    val form: DraftForm,
    val title: String,
    val preview: String,
    val targetLabel: String,
    val editedLabel: String,
    val updatedAt: Instant,
) {
    val id: Long get() = form.id
    val isPrivate: Boolean get() = form.mode == RecipientMode.STUDENT
}

object DraftRules {
    const val TITLE_MAX = 80
    const val CONTENT_MAX = 500
    const val NO_TITLE = "(Sin título)"
    const val NO_TARGET = "Sin destinatario"

    /** Dueño de los borradores: el uid de la sesión, o el correo si no hay uid. Sin ninguno no hay borradores. */
    fun ownerOf(uid: String?, email: String?): String? {
        uid?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
        return email?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }?.let { "email:$it" }
    }

    /** Basta con título o contenido para guardar; para enviar hacen falta los dos y un destinatario. */
    fun canSave(form: DraftForm): Boolean = form.hasText

    fun canSend(form: DraftForm): Boolean {
        val hasTarget = when (form.mode) {
            RecipientMode.COURSE -> form.courseCatalogId != null
            RecipientMode.STUDENT -> form.courseCatalogId != null && form.recipientStudentId != null
        }
        return hasTarget && form.title.isNotBlank() && form.content.isNotBlank()
    }

    /** ¿Cerrar la hoja perdería algo? Un borrador abierto sin cambios se cierra sin preguntar. */
    fun hasChanges(initial: DraftForm?, current: DraftForm): Boolean =
        if (initial == null) current.hasText else current.copy(id = initial.id) != initial

    fun targetLabel(form: DraftForm): String = when {
        form.mode == RecipientMode.STUDENT && form.recipientName != null ->
            listOf("Para: ${form.recipientName}", form.courseCode).filter { it.isNotBlank() }.joinToString(" · ")
        form.mode == RecipientMode.COURSE && form.courseCode.isNotBlank() -> "Todo el curso ${form.courseCode}"
        else -> NO_TARGET
    }

    /** "Editado hace 5 min", "Editado ayer 14:30", "Editado el 3 nov". */
    fun editedLabel(updatedAt: Instant, now: ZonedDateTime): String {
        val relative = RelativeDate.format(updatedAt, now)
        return if (relative.first().isDigit()) "Editado el $relative" else "Editado $relative"
    }

    fun summarize(entity: DraftEntity, now: ZonedDateTime): DraftSummary {
        val form = DraftMapper.toForm(entity)
        val updatedAt = Instant.ofEpochMilli(entity.updatedAt)
        return DraftSummary(
            form = form,
            title = form.title.trim().ifEmpty { NO_TITLE },
            preview = form.content.trim().replace(Regex("\\s+"), " "),
            targetLabel = targetLabel(form),
            editedLabel = editedLabel(updatedAt, now),
            updatedAt = updatedAt,
        )
    }

    /** Defensa extra además del filtro de la consulta: sólo los del dueño, del más reciente al más antiguo. */
    fun visibleFor(drafts: List<DraftEntity>, owner: String?, now: ZonedDateTime): List<DraftSummary> {
        if (owner == null) return emptyList()
        return drafts
            .filter { it.ownerUid == owner }
            .sortedWith(compareByDescending<DraftEntity> { it.updatedAt }.thenByDescending { it.id })
            .map { summarize(it, now) }
    }
}

object DraftMapper {
    fun toEntity(form: DraftForm, ownerUid: String, updatedAt: Long): DraftEntity {
        val private = form.mode == RecipientMode.STUDENT
        return DraftEntity(
            id = form.id,
            ownerUid = ownerUid,
            mode = form.mode.name,
            courseCatalogId = form.courseCatalogId,
            courseCode = form.courseCode,
            // Un borrador "todo el curso" no guarda un destinatario elegido antes
            recipientStudentId = if (private) form.recipientStudentId else null,
            recipientName = if (private) form.recipientName else null,
            priority = form.priority.code,
            title = form.title.take(DraftRules.TITLE_MAX),
            content = form.content.take(DraftRules.CONTENT_MAX),
            allowComments = form.allowComments,
            updatedAt = updatedAt,
        )
    }

    fun toForm(entity: DraftEntity): DraftForm = DraftForm(
        id = entity.id,
        mode = RecipientMode.entries.firstOrNull { it.name == entity.mode } ?: RecipientMode.COURSE,
        courseCatalogId = entity.courseCatalogId,
        courseCode = entity.courseCode,
        recipientStudentId = entity.recipientStudentId,
        recipientName = entity.recipientName,
        priority = Priority.fromCode(entity.priority),
        title = entity.title,
        content = entity.content,
        allowComments = entity.allowComments,
    )
}
