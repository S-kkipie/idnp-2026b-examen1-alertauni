package com.erns.alertauni.domain.post

import com.erns.alertauni.data.model.DraftEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class DraftRulesTest {

    private val lima: ZoneId = ZoneId.of("America/Lima")
    private val now: ZonedDateTime = ZonedDateTime.of(2026, 11, 12, 10, 0, 0, 0, lima)

    private fun millisAgo(minutes: Long) = now.toInstant().minus(Duration.ofMinutes(minutes)).toEpochMilli()

    private fun entity(owner: String, title: String, updatedAt: Long, id: Long) = DraftEntity(
        id = id, ownerUid = owner, mode = "COURSE", courseCatalogId = "c1", courseCode = "INF-01",
        priority = "MEDIA", title = title, content = "", allowComments = true, updatedAt = updatedAt
    )

    // ---- dueño ----

    @Test
    fun `el duenio es el uid de la sesion`() {
        assertEquals("uid-123", DraftRules.ownerOf(" uid-123 ", "docente@unsa.edu.pe"))
    }

    @Test
    fun `sin uid se usa el correo y sin ninguno no hay duenio`() {
        assertEquals("email:docente@unsa.edu.pe", DraftRules.ownerOf("", " Docente@UNSA.edu.pe "))
        assertNull(DraftRules.ownerOf(" ", ""))
        assertNull(DraftRules.ownerOf(null, null))
    }

    @Test
    fun `un correo no se confunde con un uid igual`() {
        assertTrue(DraftRules.ownerOf(null, "x") != DraftRules.ownerOf("x", null))
    }

    @Test
    fun `solo se muestran los borradores del duenio, del mas reciente al mas antiguo`() {
        val drafts = listOf(
            entity("uid-a", "Viejo", millisAgo(120), id = 1),
            entity("uid-b", "Ajeno", millisAgo(1), id = 2),
            entity("uid-a", "Nuevo", millisAgo(5), id = 3),
        )
        assertEquals(listOf("Nuevo", "Viejo"), DraftRules.visibleFor(drafts, "uid-a", now).map { it.title })
        assertEquals(listOf("Ajeno"), DraftRules.visibleFor(drafts, "uid-b", now).map { it.title })
        assertTrue(DraftRules.visibleFor(drafts, null, now).isEmpty())
        assertTrue(DraftRules.visibleFor(drafts, "uid-c", now).isEmpty())
    }

    // ---- validación ----

    @Test
    fun `se puede guardar con solo titulo o solo contenido`() {
        assertTrue(DraftRules.canSave(DraftForm(title = "Examen")))
        assertTrue(DraftRules.canSave(DraftForm(content = "Traer calculadora")))
        assertFalse(DraftRules.canSave(DraftForm(title = "  ", content = "\n")))
    }

    @Test
    fun `para enviar hace falta destinatario, titulo y contenido`() {
        val base = DraftForm(courseCatalogId = "c1", courseCode = "INF-01", title = "Examen", content = "Lunes 8")
        assertTrue(DraftRules.canSend(base))
        assertFalse(DraftRules.canSend(base.copy(content = "")))
        assertFalse(DraftRules.canSend(base.copy(courseCatalogId = null)))
        assertFalse(DraftRules.canSend(base.copy(mode = RecipientMode.STUDENT)))
        assertTrue(DraftRules.canSend(base.copy(mode = RecipientMode.STUDENT, recipientStudentId = "s1")))
    }

    @Test
    fun `cerrar un borrador sin cambios no pregunta`() {
        val saved = DraftForm(id = 4, courseCatalogId = "c1", title = "Examen")
        assertFalse(DraftRules.hasChanges(saved, saved))
        assertTrue(DraftRules.hasChanges(saved, saved.copy(title = "Examen final")))
        assertTrue(DraftRules.hasChanges(saved, saved.copy(priority = Priority.ALTA)))
    }

    @Test
    fun `cerrar un anuncio nuevo pregunta solo si hay texto`() {
        assertFalse(DraftRules.hasChanges(null, DraftForm(courseCatalogId = "c1")))
        assertTrue(DraftRules.hasChanges(null, DraftForm(content = "Hola")))
    }

    // ---- textos ----

    @Test
    fun `destinatario del borrador`() {
        assertEquals("Todo el curso INF-01", DraftRules.targetLabel(DraftForm(courseCode = "INF-01")))
        assertEquals(
            "Para: Lucía Quispe · INF-02",
            DraftRules.targetLabel(
                DraftForm(mode = RecipientMode.STUDENT, courseCode = "INF-02", recipientName = "Lucía Quispe")
            )
        )
        assertEquals(DraftRules.NO_TARGET, DraftRules.targetLabel(DraftForm()))
        assertEquals(DraftRules.NO_TARGET, DraftRules.targetLabel(DraftForm(mode = RecipientMode.STUDENT)))
    }

    @Test
    fun `fecha de edicion relativa`() {
        fun label(y: Int, mo: Int, d: Int, h: Int, mi: Int) =
            DraftRules.editedLabel(LocalDateTime.of(y, mo, d, h, mi).atZone(lima).toInstant(), now)
        assertEquals("Editado ahora", label(2026, 11, 12, 10, 0))
        assertEquals("Editado hace 5 min", label(2026, 11, 12, 9, 55))
        assertEquals("Editado ayer 18:20", label(2026, 11, 11, 18, 20))
        assertEquals("Editado el 3 nov", label(2026, 11, 3, 8, 0))
    }

    @Test
    fun `resumen con titulo vacio y contenido en una linea`() {
        val summary = DraftRules.summarize(
            DraftMapper.toEntity(DraftForm(content = "Línea 1\n\nLínea   2"), "uid-a", millisAgo(30)),
            now
        )
        assertEquals(DraftRules.NO_TITLE, summary.title)
        assertEquals("Línea 1 Línea 2", summary.preview)
        assertEquals("Editado hace 30 min", summary.editedLabel)
    }
}
