package com.erns.alertauni.data.model

import com.erns.alertauni.domain.post.Priority
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/** El campo opcional `priority` no debe romper el formato que ya entiende el backend. */
class PostWireFormatTest {

    // Misma configuración que usa supabase-kt
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    private val postJson = """
        {"post_id":7,"author_name":"Ernesto","author_type":1,"course_code":"INF-01",
         "course_name":"Ingeniería de Software","title":"Tareas","content":"Página 12",
         "allow_comments":true,"count_comments":3,"is_private":false,
         "published":"2026-05-30 04:44:09","receiver_private":null}
    """.trimIndent()

    @Test
    fun `anuncio sin prioridad se lee como media`() {
        val post = json.decodeFromString<PostEntity>(postJson)
        assertNull(post.priority)
        assertEquals(Priority.MEDIA, Priority.fromCode(post.priority))
    }

    @Test
    fun `anuncio con prioridad del backend futuro`() {
        val post = json.decodeFromString<PostEntity>(postJson.replace("\"receiver_private\":null", "\"priority\":\"ALTA\""))
        assertEquals(Priority.ALTA, Priority.fromCode(post.priority))
    }

    @Test
    fun `peticion sin prioridad mantiene el formato anterior`() {
        val body = json.encodeToString(PostRequest("c1", "Título", "Contenido", false, true))
        assertFalse(json.parseToJsonElement(body).jsonObject.containsKey("priority"))
    }

    @Test
    fun `peticiones con prioridad la envian como codigo`() {
        val public = json.encodeToString(PostRequest("c1", "T", "C", false, true, Priority.ALTA.code))
        val private = json.encodeToString(PostPrivateRequest("c1", "T", "C", "uuid-1", false, Priority.BAJA.code))
        assertEquals("\"ALTA\"", json.parseToJsonElement(public).jsonObject["priority"].toString())
        assertEquals("\"BAJA\"", json.parseToJsonElement(private).jsonObject["priority"].toString())
        assertEquals("\"uuid-1\"", json.parseToJsonElement(private).jsonObject["receiver_private"].toString())
    }
}
