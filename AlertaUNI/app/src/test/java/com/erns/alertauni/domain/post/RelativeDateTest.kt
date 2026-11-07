package com.erns.alertauni.domain.post

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class RelativeDateTest {

    private val lima: ZoneId = ZoneId.of("America/Lima")

    // Miércoles 3 de junio de 2026, 10:00 en Lima (15:00 UTC)
    private val now: ZonedDateTime = ZonedDateTime.of(2026, 6, 3, 10, 0, 0, 0, lima)

    private fun lima(y: Int, mo: Int, d: Int, h: Int, mi: Int): Instant =
        LocalDateTime.of(y, mo, d, h, mi).atZone(lima).toInstant()

    @Test
    fun `formato del backend sin zona se interpreta en UTC`() {
        assertEquals(Instant.parse("2026-05-31T01:36:32Z"), RelativeDate.parse("2026-05-31 01:36:32"))
    }

    @Test
    fun `acepta ISO con zona y fracciones`() {
        assertEquals(Instant.parse("2026-05-31T01:36:32.500Z"), RelativeDate.parse("2026-05-31T01:36:32.5Z"))
        assertEquals(Instant.parse("2026-05-31T06:36:32Z"), RelativeDate.parse("2026-05-31T01:36:32-05:00"))
        assertEquals(Instant.parse("2026-05-31T01:36:32.123456Z"), RelativeDate.parse("2026-05-31 01:36:32.123456+00"))
        assertEquals(Instant.parse("2026-05-31T01:36:00Z"), RelativeDate.parse("2026-05-31 01:36"))
    }

    @Test
    fun `zona supuesta configurable`() {
        assertEquals(lima(2026, 5, 31, 1, 36), RelativeDate.parse("2026-05-31 01:36:00", lima))
    }

    @Test
    fun `texto invalido devuelve null`() {
        assertNull(RelativeDate.parse(null))
        assertNull(RelativeDate.parse(""))
        assertNull(RelativeDate.parse("ayer"))
        assertNull(RelativeDate.parse("2026-13-45 99:00:00"))
    }

    @Test
    fun `minutos y horas del mismo dia`() {
        assertEquals("ahora", RelativeDate.format(now.toInstant().minusSeconds(20), now))
        assertEquals("hace 5 min", RelativeDate.format(now.toInstant().minusSeconds(5 * 60), now))
        assertEquals("hace 59 min", RelativeDate.format(now.toInstant().minusSeconds(59 * 60 + 59), now))
        assertEquals("hace 2 h", RelativeDate.format(lima(2026, 6, 3, 7, 30), now))
    }

    @Test
    fun `fecha futura por desfase de reloj se muestra como ahora`() {
        assertEquals("ahora", RelativeDate.format(now.toInstant().plusSeconds(90), now))
    }

    @Test
    fun `menos de una hora aunque cruce la medianoche`() {
        val justAfterMidnight = ZonedDateTime.of(2026, 6, 3, 0, 10, 0, 0, lima)
        assertEquals("hace 20 min", RelativeDate.format(lima(2026, 6, 2, 23, 50), justAfterMidnight))
        assertEquals("ayer 22:05", RelativeDate.format(lima(2026, 6, 2, 22, 5), justAfterMidnight))
    }

    @Test
    fun `ayer con hora y fechas antiguas`() {
        assertEquals("ayer 14:30", RelativeDate.format(lima(2026, 6, 2, 14, 30), now))
        assertEquals("31 may", RelativeDate.format(lima(2026, 5, 31, 20, 0), now))
        assertEquals("1 ene", RelativeDate.format(lima(2026, 1, 1, 8, 0), now))
        assertEquals("31 may 2025", RelativeDate.format(lima(2025, 5, 31, 20, 0), now))
    }

    @Test
    fun `fecha del backend en UTC se muestra en hora local`() {
        // 01:36 UTC del 3 de junio = 20:36 del 2 de junio en Lima
        assertEquals("ayer 20:36", RelativeDate.format("2026-06-03 01:36:32", now))
        assertEquals("sin formato", RelativeDate.format("sin formato", now))
    }

    @Test
    fun `grupos por fecha`() {
        assertEquals("Hoy", RelativeDate.groupLabel(lima(2026, 6, 3, 0, 1), now))
        assertEquals("Ayer", RelativeDate.groupLabel(lima(2026, 6, 2, 23, 59), now))
        assertEquals("Esta semana", RelativeDate.groupLabel(lima(2026, 6, 1, 9, 0), now))
        assertEquals("Mayo 2026", RelativeDate.groupLabel(lima(2026, 5, 31, 9, 0), now))
        assertEquals("Diciembre 2025", RelativeDate.groupLabel(lima(2025, 12, 24, 9, 0), now))
        assertEquals(RelativeDate.NO_DATE, RelativeDate.groupLabel(null, now))
    }

    @Test
    fun `lunes no tiene grupo esta semana aparte de hoy`() {
        val monday = ZonedDateTime.of(2026, 6, 1, 9, 0, 0, 0, lima)
        assertEquals("Ayer", RelativeDate.groupLabel(lima(2026, 5, 31, 9, 0), monday))
        assertEquals("Mayo 2026", RelativeDate.groupLabel(lima(2026, 5, 30, 9, 0), monday))
    }

    @Test
    fun `agrupa ordenando del mas reciente al mas antiguo`() {
        val items = listOf(
            "viejo" to lima(2026, 5, 20, 9, 0),
            "sin fecha" to null,
            "hoy" to lima(2026, 6, 3, 9, 0),
            "ayer" to lima(2026, 6, 2, 9, 0),
            "hoy temprano" to lima(2026, 6, 3, 7, 0),
        )
        val groups = RelativeDate.group(items, now) { it.second }
        assertEquals(listOf("Hoy", "Ayer", "Mayo 2026", RelativeDate.NO_DATE), groups.map { it.first })
        assertEquals(listOf("hoy", "hoy temprano"), groups[0].second.map { it.first })
    }
}
