package com.erns.alertauni.domain.course

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JoinCodeTest {

    @Test
    fun `codigo escrito a mano se acepta sin espacios`() {
        assertEquals("IDNP-7K3Q", JoinCode.parse("  IDNP-7K3Q "))
    }

    @Test
    fun `contenido del QR devuelve el codigo`() {
        val qr = JoinCode.toQrContent("IDNP-7K3Q")
        assertEquals("alertauni://join?code=IDNP-7K3Q", qr)
        assertEquals("IDNP-7K3Q", JoinCode.parse(qr))
    }

    @Test
    fun `QR con parametros adicionales`() {
        assertEquals("ABC123", JoinCode.parse("alertauni://join?v=1&code=ABC123"))
    }

    @Test
    fun `QR de otra aplicacion se rechaza`() {
        assertNull(JoinCode.parse("https://www.unsa.edu.pe"))
        assertNull(JoinCode.parse("alertauni://post?id=10"))
        assertNull(JoinCode.parse("WIFI:S:aula;T:WPA;P:1234;;"))
    }

    @Test
    fun `texto vacio o con caracteres invalidos se rechaza`() {
        assertNull(JoinCode.parse(""))
        assertNull(JoinCode.parse(null))
        assertNull(JoinCode.parse("ID NP"))
        assertNull(JoinCode.parse("AB"))
    }
}
