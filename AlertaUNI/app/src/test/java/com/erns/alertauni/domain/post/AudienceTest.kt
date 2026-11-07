package com.erns.alertauni.domain.post

import org.junit.Assert.assertEquals
import org.junit.Test

class AudienceTest {

    @Test
    fun `todo un curso dice cuantos estudiantes reciben`() {
        val audience = Audience(courseCode = "INF-01", studentCount = 32)
        assertEquals("Enviar a 32 estudiantes de INF-01", audience.sendLabel)
        assertEquals("Anuncio enviado a 32 estudiantes de INF-01", audience.sentMessage)
    }

    @Test
    fun `singular con un estudiante`() {
        assertEquals("Enviar a 1 estudiante de INF-02", Audience("INF-02", 1).sendLabel)
    }

    @Test
    fun `un estudiante muestra solo su nombre`() {
        val audience = Audience(courseCode = "INF-01", studentCount = 32, recipientName = "Juan Pérez")
        assertEquals("Enviar sólo a Juan Pérez", audience.sendLabel)
        assertEquals("Anuncio enviado a Juan Pérez", audience.sentMessage)
    }

    @Test
    fun `sin numero de estudiantes conocido`() {
        assertEquals("Publicar en INF-03", Audience("INF-03").sendLabel)
        assertEquals("Publicar en INF-03", Audience("INF-03", 0).sendLabel)
        assertEquals("Anuncio publicado en INF-03", Audience("INF-03", null).sentMessage)
    }
}
