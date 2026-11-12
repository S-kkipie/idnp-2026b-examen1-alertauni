package com.erns.alertauni.domain.post

import com.erns.alertauni.data.model.PostLocalStateEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class DraftMapperTest {

    private val privateForm = DraftForm(
        id = 9,
        mode = RecipientMode.STUDENT,
        courseCatalogId = "cat-2",
        courseCode = "INF-02",
        recipientStudentId = "stu-7",
        recipientName = "Lucía Quispe",
        priority = Priority.ALTA,
        title = "Sobre tu trabajo final",
        content = "Revisa las observaciones.",
        allowComments = false,
    )

    @Test
    fun `ida y vuelta conserva todos los campos`() {
        val entity = DraftMapper.toEntity(privateForm, ownerUid = "uid-a", updatedAt = 42)
        assertEquals("uid-a", entity.ownerUid)
        assertEquals("STUDENT", entity.mode)
        assertEquals("ALTA", entity.priority)
        assertEquals(42, entity.updatedAt)
        assertEquals(privateForm, DraftMapper.toForm(entity))
    }

    @Test
    fun `un borrador para todo el curso no guarda destinatario`() {
        val entity = DraftMapper.toEntity(privateForm.copy(mode = RecipientMode.COURSE), "uid-a", 1)
        assertNull(entity.recipientStudentId)
        assertNull(entity.recipientName)
        assertEquals("cat-2", entity.courseCatalogId)
    }

    @Test
    fun `respeta los limites de la hoja`() {
        val long = DraftForm(title = "t".repeat(200), content = "c".repeat(900))
        val entity = DraftMapper.toEntity(long, "uid-a", 1)
        assertEquals(DraftRules.TITLE_MAX, entity.title.length)
        assertEquals(DraftRules.CONTENT_MAX, entity.content.length)
    }

    @Test
    fun `valores desconocidos en la base se leen con valores seguros`() {
        val entity = DraftMapper.toEntity(privateForm, "uid-a", 1).copy(mode = "OTRO", priority = "URGENTE")
        val form = DraftMapper.toForm(entity)
        assertEquals(RecipientMode.COURSE, form.mode)
        assertEquals(Priority.MEDIA, form.priority)
    }

    @Test
    fun `estado local sobre el valor del backend`() {
        val none = LocalPostState.NONE
        assertEquals(Priority.BAJA, none.priorityOr("BAJA"))
        assertEquals(true, none.commentsOr(true))
        assertFalse(none.archived)

        val local = LocalPostState.from(
            PostLocalStateEntity("uid-a", postId = 3, priority = "ALTA", archived = true, commentsEnabled = false)
        )
        assertEquals(Priority.ALTA, local.priorityOr("BAJA"))
        assertEquals(false, local.commentsOr(true))
        assertEquals(true, local.archived)
    }

    @Test
    fun `el estado local de otra cuenta no se aplica`() {
        val states = listOf(
            PostLocalStateEntity("uid-a", postId = 1, archived = true),
            PostLocalStateEntity("uid-b", postId = 2, archived = true),
        )
        assertEquals(setOf(1), LocalPostState.byPost(states, "uid-a").keys)
        assertEquals(emptySet<Int>(), LocalPostState.byPost(states, null).keys)
    }
}
