package com.erns.alertauni.domain.post

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnnouncementFilterTest {

    private val examen = SearchableAnnouncement(
        title = "Examen parcial",
        content = "La evaluación será el lunes en el aula 304.",
        courseCode = "INF-01",
        courseName = "Ingeniería de Software",
        priority = Priority.ALTA,
    )
    private val privado = SearchableAnnouncement(
        title = "Tu nota",
        content = "Revisa tu trabajo",
        courseCode = "INF-02",
        courseName = "Programación Web",
        recipientName = "Juan Pérez",
        isPrivate = true,
        priority = Priority.BAJA,
    )
    private val tareas = SearchableAnnouncement(
        title = "Tareas",
        content = "Resolver ejercicios de la página 12",
        courseCode = "INF-01",
        courseName = "Ingeniería de Software",
    )
    private val all = listOf(examen, privado, tareas)

    private fun run(query: AnnouncementQuery) = AnnouncementFilter.filter(all, query) { it }

    @Test
    fun `normaliza tildes y mayusculas`() {
        assertEquals("evaluacion programacion nino", AnnouncementFilter.normalize("Evaluación PROGRAMACIÓN Niño"))
    }

    @Test
    fun `busqueda vacia devuelve todo`() {
        assertEquals(all, run(AnnouncementQuery()))
        assertEquals(all, run(AnnouncementQuery(text = "   ")))
        assertFalse(AnnouncementQuery(text = "  ").isActive)
    }

    @Test
    fun `busca en titulo y contenido sin tildes`() {
        assertEquals(listOf(examen), run(AnnouncementQuery(text = "evaluacion")))
        assertEquals(listOf(tareas), run(AnnouncementQuery(text = "PAGINA")))
    }

    @Test
    fun `busca en acronimo y nombre del curso`() {
        assertEquals(listOf(privado), run(AnnouncementQuery(text = "inf-02")))
        assertEquals(listOf(examen, tareas), run(AnnouncementQuery(text = "ingenieria")))
    }

    @Test
    fun `busca por nombre del destinatario privado`() {
        assertEquals(listOf(privado), run(AnnouncementQuery(text = "perez")))
    }

    @Test
    fun `varias palabras deben aparecer todas`() {
        assertEquals(listOf(examen), run(AnnouncementQuery(text = "parcial INF-01")))
        assertEquals(emptyList<SearchableAnnouncement>(), run(AnnouncementQuery(text = "parcial INF-02")))
    }

    @Test
    fun `filtro por tipo`() {
        assertEquals(listOf(examen, tareas), run(AnnouncementQuery(type = TypeFilter.PUBLIC)))
        assertEquals(listOf(privado), run(AnnouncementQuery(type = TypeFilter.PRIVATE)))
    }

    @Test
    fun `filtros combinados con texto prioridad y curso`() {
        assertEquals(listOf(examen), run(AnnouncementQuery(onlyHigh = true)))
        assertEquals(listOf(examen, tareas), run(AnnouncementQuery(courseCode = "inf-01")))
        assertEquals(listOf(tareas), run(AnnouncementQuery(text = "tareas", courseCode = "INF-01")))
        assertEquals(
            emptyList<SearchableAnnouncement>(),
            run(AnnouncementQuery(type = TypeFilter.PRIVATE, onlyHigh = true))
        )
        assertTrue(AnnouncementQuery(courseCode = "INF-01").isActive)
    }

    @Test
    fun `resaltado usa indices del texto original`() {
        val text = "La evaluación será en la Evaluacion final"
        val ranges = AnnouncementFilter.highlightRanges(text, "EVALUACION")
        assertEquals(listOf("evaluación", "Evaluacion"), ranges.map { text.substring(it.first, it.last + 1) })
    }

    @Test
    fun `resaltado de varias palabras une los tramos solapados`() {
        val text = "Examen parcial"
        assertEquals(listOf(0..5, 7..13), AnnouncementFilter.highlightRanges(text, "parcial examen xamen"))
        assertEquals(listOf(7..13), AnnouncementFilter.highlightRanges(text, "parcial"))
        assertEquals(emptyList<IntRange>(), AnnouncementFilter.highlightRanges(text, " "))
    }

    @Test
    fun `resaltado con tilde descompuesta incluye la marca`() {
        val text = "Evaluación"
        val ranges = AnnouncementFilter.highlightRanges(text, "evaluación")
        assertEquals(listOf(0..text.lastIndex), ranges)
    }
}
