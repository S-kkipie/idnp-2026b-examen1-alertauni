package com.erns.alertauni.data.demo

import com.erns.alertauni.domain.post.AnnouncementFilter
import com.erns.alertauni.domain.post.AnnouncementQuery
import com.erns.alertauni.domain.post.DraftRules
import com.erns.alertauni.domain.post.Priority
import com.erns.alertauni.domain.post.RelativeDate
import com.erns.alertauni.domain.post.SearchableAnnouncement
import com.erns.alertauni.domain.post.TypeFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

/** T4A: los datos de prueba cubren la variedad pedida y son siempre los mismos. */
class DemoAnnouncementsTest {

    private val now: ZonedDateTime = ZonedDateTime.of(2026, 11, 12, 10, 0, 0, 0, ZoneId.of("America/Lima"))
    private val posts = DemoAnnouncements.posts(now)

    private fun searchable(i: Int) = posts[i].let {
        SearchableAnnouncement(
            it.title, it.content, it.courseCode, it.courseName.orEmpty(),
            DemoAnnouncements.studentNames[it.receiverPrivate], it.isPrivate, Priority.fromCode(it.priority)
        )
    }

    @Test
    fun `al menos 40 anuncios con ids negativos y unicos`() {
        assertTrue(posts.size >= 40)
        assertEquals(posts.size, posts.map { it.id }.toSet().size)
        assertTrue(posts.all { DemoAnnouncements.isSimulated(it.id) })
    }

    @Test
    fun `publicos y privados, los privados con destinatario conocido del mismo curso`() {
        val private = posts.filter { it.isPrivate }
        assertTrue(private.size >= 8)
        assertTrue(posts.count { !it.isPrivate } >= 20)
        private.forEach { post ->
            val student = DemoAnnouncements.students.first { it.id == post.receiverPrivate }
            assertEquals(post.courseCode, student.courseCode)
        }
        assertTrue(posts.filterNot { it.isPrivate }.all { it.receiverPrivate == null })
    }

    @Test
    fun `las tres prioridades con varios anuncios cada una`() {
        val byPriority = posts.groupingBy { Priority.fromCode(it.priority) }.eachCount()
        Priority.entries.forEach { assertTrue("$it: ${byPriority[it]}", (byPriority[it] ?: 0) >= 8) }
    }

    @Test
    fun `cuatro cursos INF-01 a INF-04`() {
        assertEquals(listOf("INF-01", "INF-02", "INF-03", "INF-04"), posts.map { it.courseCode }.distinct().sorted())
    }

    @Test
    fun `fechas de hoy, ayer y unos dos meses atras`() {
        val instants = posts.map { assertNotNull(RelativeDate.parse(it.date)); RelativeDate.parse(it.date)!! }
        val groups = instants.map { RelativeDate.groupLabel(it, now) }
        assertTrue(groups.count { it == "Hoy" } >= 3)
        assertTrue(groups.count { it == "Ayer" } >= 3)
        val span = Duration.between(instants.min(), instants.max()).toDays()
        assertTrue("span $span", span in 50..70)
        assertTrue(instants.all { !it.isAfter(now.toInstant()) })
        assertTrue(groups.toSet().size >= 4)
    }

    @Test
    fun `titulos y contenidos cortos y largos dentro de los limites`() {
        assertTrue(posts.any { it.title.length <= 10 })
        assertTrue(posts.any { it.title.length >= 60 })
        assertTrue(posts.any { it.content.length <= 40 })
        assertTrue(posts.any { it.content.length >= 300 })
        assertTrue(posts.all { it.title.length <= DraftRules.TITLE_MAX })
        assertTrue(posts.all { it.content.length <= DraftRules.CONTENT_MAX })
    }

    @Test
    fun `algunos con comentarios cerrados y sin conteo`() {
        val closed = posts.filterNot { it.allowComments }
        assertTrue(closed.size >= 3)
        assertTrue(closed.all { it.countComments == 0 })
        assertTrue(posts.any { it.allowComments && it.countComments > 0 })
    }

    @Test
    fun `misma fecha genera exactamente lo mismo`() {
        assertEquals(posts, DemoAnnouncements.posts(now))
    }

    @Test
    fun `sirven para probar busqueda y filtros`() {
        val all = posts.indices.toList()
        fun count(query: AnnouncementQuery) = AnnouncementFilter.filter(all, query, ::searchable).size
        assertTrue(count(AnnouncementQuery(text = "examen")) >= 4)
        assertEquals(0, count(AnnouncementQuery(text = "beca alimentaria")))
        assertEquals(posts.count { it.isPrivate }, count(AnnouncementQuery(type = TypeFilter.PRIVATE)))
        // Buscar por el destinatario de un privado
        assertTrue(count(AnnouncementQuery(text = "lucia quispe")) >= 2)
        assertTrue(count(AnnouncementQuery(onlyHigh = true, courseCode = "INF-01")) >= 2)
    }
}
