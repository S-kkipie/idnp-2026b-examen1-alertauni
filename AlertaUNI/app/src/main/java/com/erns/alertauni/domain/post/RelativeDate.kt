package com.erns.alertauni.domain.post

import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/** Fechas del muro: "hace 5 min", "ayer 14:30", "31 may" y grupos "Hoy", "Ayer", "Esta semana", "Mayo 2026". */
object RelativeDate {
    const val NO_DATE = "Sin fecha"

    private val MONTHS_SHORT =
        listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
    private val MONTHS = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    private val PARSER: DateTimeFormatter = DateTimeFormatterBuilder()
        .append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendLiteral('T')
        .append(DateTimeFormatter.ISO_LOCAL_TIME)
        .optionalStart().appendOffset("+HH:mm", "Z").optionalEnd()
        .toFormatter(Locale.ROOT)

    /**
     * Lee la fecha del backend. `service-posts` envía "2026-05-31 01:36:32" sin zona; las Edge Functions
     * formatean `created_at` en UTC, por eso [assumedZone] es UTC por defecto. También acepta ISO-8601
     * con zona ("2026-05-31T01:36:32.5Z", "...+00:00", "...+00").
     */
    fun parse(raw: String?, assumedZone: ZoneId = ZoneOffset.UTC): Instant? {
        val text = raw?.trim().orEmpty()
        if (text.length < 16) return null
        val iso = if (text[10] == ' ') text.replaceRange(10, 11, "T") else text
        return try {
            val parsed = PARSER.parse(iso)
            val local = LocalDateTime.from(parsed)
            if (parsed.isSupported(ChronoField.OFFSET_SECONDS)) {
                local.atOffset(ZoneOffset.from(parsed)).toInstant()
            } else {
                local.atZone(assumedZone).toInstant()
            }
        } catch (e: DateTimeParseException) {
            null
        }
    }

    /** Fecha corta relativa a [now] (en la zona de [now]). */
    fun format(instant: Instant, now: ZonedDateTime): String {
        val minutes = Duration.between(instant, now.toInstant()).toMinutes().coerceAtLeast(0)
        val dateTime = instant.atZone(now.zone)
        val day = dateTime.toLocalDate()
        val today = now.toLocalDate()
        val month = MONTHS_SHORT[day.monthValue - 1]
        return when {
            minutes < 1 -> "ahora"
            minutes < 60 -> "hace $minutes min"
            day == today -> "hace ${minutes / 60} h"
            day == today.minusDays(1) ->
                "ayer %02d:%02d".format(Locale.ROOT, dateTime.hour, dateTime.minute)
            day.year == today.year -> "${day.dayOfMonth} $month"
            else -> "${day.dayOfMonth} $month ${day.year}"
        }
    }

    fun format(raw: String?, now: ZonedDateTime): String =
        parse(raw)?.let { format(it, now) } ?: raw.orEmpty()

    /** Encabezado del grupo al que pertenece la fecha. La semana empieza el lunes. */
    fun groupLabel(instant: Instant?, now: ZonedDateTime): String {
        if (instant == null) return NO_DATE
        val day = instant.atZone(now.zone).toLocalDate()
        val today = now.toLocalDate()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return when {
            !day.isBefore(today) -> "Hoy"
            day == today.minusDays(1) -> "Ayer"
            !day.isBefore(monday) -> "Esta semana"
            else -> "${MONTHS[day.monthValue - 1]} ${day.year}"
        }
    }

    /** Ordena del más reciente al más antiguo y agrupa por [groupLabel]; los sin fecha van al final. */
    fun <T> group(items: List<T>, now: ZonedDateTime, instantOf: (T) -> Instant?): List<Pair<String, List<T>>> {
        val sorted = items.sortedWith(compareByDescending(nullsFirst<Instant>()) { it: T -> instantOf(it) })
        val groups = LinkedHashMap<String, MutableList<T>>()
        for (item in sorted) {
            groups.getOrPut(groupLabel(instantOf(item), now)) { mutableListOf() } += item
        }
        return groups.map { (label, list) -> label to list }
    }
}
