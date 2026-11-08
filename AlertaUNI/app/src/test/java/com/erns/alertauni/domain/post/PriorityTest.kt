package com.erns.alertauni.domain.post

import org.junit.Assert.assertEquals
import org.junit.Test

class PriorityTest {

    @Test
    fun `codigos conocidos sin importar mayusculas ni espacios`() {
        assertEquals(Priority.ALTA, Priority.fromCode("ALTA"))
        assertEquals(Priority.BAJA, Priority.fromCode(" baja "))
        assertEquals(Priority.MEDIA, Priority.fromCode("Media"))
    }

    @Test
    fun `sin prioridad o desconocida se asume media`() {
        assertEquals(Priority.MEDIA, Priority.fromCode(null))
        assertEquals(Priority.MEDIA, Priority.fromCode(""))
        assertEquals(Priority.MEDIA, Priority.fromCode("URGENTE"))
    }

    @Test
    fun `el codigo enviado al backend se vuelve a leer igual`() {
        Priority.entries.forEach { assertEquals(it, Priority.fromCode(it.code)) }
    }

    @Test
    fun `cada prioridad tiene etiqueta de texto`() {
        assertEquals(listOf("Alta", "Media", "Baja"), Priority.entries.map { it.label })
    }
}
