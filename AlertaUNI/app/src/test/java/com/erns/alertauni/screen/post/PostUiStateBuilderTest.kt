package com.erns.alertauni.screen.post

import com.erns.alertauni.data.demo.DemoAnnouncements
import com.erns.alertauni.data.model.DraftEntity
import com.erns.alertauni.data.model.PostEntity
import com.erns.alertauni.domain.post.AnnouncementQuery
import com.erns.alertauni.domain.post.LocalPostState
import com.erns.alertauni.domain.post.Priority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class PostUiStateBuilderTest {

    private val now: ZonedDateTime = ZonedDateTime.of(2026, 11, 12, 10, 0, 0, 0, ZoneId.of("America/Lima"))

    private val backendPost = PostEntity(
        id = 15, authorType = 1, courseCode = "INF-01", title = "Del backend", content = "Real",
        allowComments = true, countComments = 2, isPrivate = false, date = "2026-11-12 14:00:00", priority = null
    )

    private fun draft(owner: String, id: Long) = DraftEntity(
        id = id, ownerUid = owner, mode = "COURSE", courseCatalogId = "c1", courseCode = "INF-01",
        priority = "ALTA", title = "Borrador $id", content = "", allowComments = true,
        updatedAt = now.toInstant().toEpochMilli()
    )

    private val sources = WallSources(
        posts = listOf(backendPost),
        isTeacher = true,
        owner = "uid-a",
        demoPosts = DemoAnnouncements.posts(now),
    )

    private fun build(s: WallSources, view: WallViewOptions = WallViewOptions()) = PostUiStateBuilder.build(s, view, now)

    @Test
    fun `datos de prueba apagados solo muestra el backend`() {
        val state = build(sources)
        assertEquals(1, state.totalInView)
        assertFalse(state.demoEnabled)
        assertFalse(state.sections.flatMap { it.items }.single().isSimulated)
    }

    @Test
    fun `datos de prueba encendidos se suman y se marcan como simulados`() {
        val state = build(sources.copy(demoEnabled = true))
        val items = state.sections.flatMap { it.items }
        assertEquals(1 + DemoAnnouncements.count, items.size)
        assertEquals(DemoAnnouncements.count, items.count { it.isSimulated })
        assertEquals(DemoAnnouncements.count, state.demoCount)
        assertTrue(items.single { !it.isSimulated }.id == 15)
        // Los privados simulados muestran su destinatario
        assertTrue(items.filter { it.isPrivate }.all { it.privateLabel.startsWith("Para: ") })
        assertTrue(state.courseCodes.containsAll(listOf("INF-01", "INF-02", "INF-03", "INF-04")))
    }

    @Test
    fun `el estudiante nunca ve datos simulados`() {
        val state = build(sources.copy(isTeacher = false, demoEnabled = true))
        assertEquals(1, state.totalInView)
        assertFalse(state.demoEnabled)
    }

    @Test
    fun `el estado local cambia prioridad, archivado y comentarios`() {
        val local = mapOf(15 to LocalPostState(priority = Priority.ALTA, archived = true, commentsEnabled = false))
        val active = build(sources.copy(localStates = local))
        assertEquals(0, active.totalInView)
        assertEquals(1, active.archivedCount)

        val archived = build(sources.copy(localStates = local), WallViewOptions(wallView = WallView.ARCHIVED))
        val item = archived.sections.single().items.single()
        assertEquals(Priority.ALTA, item.priority)
        assertFalse(item.allowComments)
        assertTrue(item.isArchived)
    }

    @Test
    fun `los borradores son de la cuenta actual y no se mezclan con el muro`() {
        val s = sources.copy(drafts = listOf(draft("uid-a", 1), draft("uid-b", 2), draft("uid-a", 3)))
        val wall = build(s)
        assertEquals(listOf(3L, 1L), wall.drafts.map { it.id })
        assertTrue(wall.sections.flatMap { it.items }.none { it.title.startsWith("Borrador") })

        val draftsView = build(s, WallViewOptions(wallView = WallView.DRAFTS))
        assertTrue(draftsView.showDrafts)
        assertEquals(0, draftsView.totalInView)
        assertEquals(2, draftsView.draftCount)

        val otherAccount = build(s.copy(owner = "uid-b"))
        assertEquals(listOf(2L), otherAccount.drafts.map { it.id })
        val noAccount = build(s.copy(owner = null))
        assertTrue(noAccount.drafts.isEmpty())
        assertFalse(noAccount.canUseDrafts)
    }

    @Test
    fun `busqueda sobre los 45 anuncios`() {
        val state = build(sources.copy(demoEnabled = true), WallViewOptions(query = AnnouncementQuery(text = "examen")))
        assertTrue(state.resultCount in 4 until state.totalInView)
        val none = build(sources.copy(demoEnabled = true), WallViewOptions(query = AnnouncementQuery(text = "beca alimentaria")))
        assertEquals(0, none.resultCount)
    }
}
