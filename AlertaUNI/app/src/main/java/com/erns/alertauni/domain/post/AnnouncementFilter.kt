package com.erns.alertauni.domain.post

import java.text.Normalizer

enum class TypeFilter { ALL, PUBLIC, PRIVATE }

/** Búsqueda + filtros del muro. Los filtros se combinan con Y. */
data class AnnouncementQuery(
    val text: String = "",
    val type: TypeFilter = TypeFilter.ALL,
    val onlyHigh: Boolean = false,
    val courseCode: String? = null,
) {
    val isActive: Boolean
        get() = text.isNotBlank() || type != TypeFilter.ALL || onlyHigh || courseCode != null
}

/** Lo mínimo que la búsqueda necesita saber de un anuncio. */
data class SearchableAnnouncement(
    val title: String,
    val content: String,
    val courseCode: String,
    val courseName: String = "",
    val recipientName: String? = null,
    val isPrivate: Boolean = false,
    val priority: Priority = Priority.MEDIA,
)

object AnnouncementFilter {
    private val WHITESPACE = Regex("\\s+")

    /** Minúsculas y sin tildes: "Evaluación" -> "evaluacion". */
    fun normalize(text: String): String = fold(text).first

    /** Palabras de la búsqueda; todas deben aparecer (en cualquier campo). */
    fun terms(text: String): List<String> =
        normalize(text).split(WHITESPACE).filter { it.isNotEmpty() }

    fun matches(item: SearchableAnnouncement, query: AnnouncementQuery): Boolean {
        when (query.type) {
            TypeFilter.PUBLIC -> if (item.isPrivate) return false
            TypeFilter.PRIVATE -> if (!item.isPrivate) return false
            TypeFilter.ALL -> Unit
        }
        if (query.onlyHigh && item.priority != Priority.ALTA) return false
        if (query.courseCode != null && !item.courseCode.equals(query.courseCode, ignoreCase = true)) {
            return false
        }
        val terms = terms(query.text)
        if (terms.isEmpty()) return true
        val haystack = normalize(
            listOf(item.title, item.content, item.courseCode, item.courseName, item.recipientName.orEmpty())
                .joinToString("\n")
        )
        return terms.all { haystack.contains(it) }
    }

    fun <T> filter(items: List<T>, query: AnnouncementQuery, fields: (T) -> SearchableAnnouncement): List<T> =
        items.filter { matches(fields(it), query) }

    /**
     * Tramos de [text] (índices del texto original) que coinciden con alguna palabra de [query],
     * ignorando mayúsculas y tildes. Ordenados y sin solaparse; sirven para resaltar.
     */
    fun highlightRanges(text: String, query: String): List<IntRange> {
        val terms = terms(query)
        if (terms.isEmpty() || text.isEmpty()) return emptyList()
        val (folded, origin) = fold(text)
        val found = mutableListOf<IntRange>()
        for (term in terms) {
            var from = 0
            while (true) {
                val i = folded.indexOf(term, from)
                if (i < 0) break
                var end = origin[i + term.length - 1] + 1
                while (end < text.length && isMark(text[end])) end++
                found += origin[i] until end
                from = i + 1
            }
        }
        return merge(found)
    }

    /** Texto plegado y, para cada carácter plegado, su índice en el original. */
    private fun fold(text: String): Pair<String, IntArray> {
        val out = StringBuilder(text.length)
        val origin = ArrayList<Int>(text.length)
        text.forEachIndexed { index, c ->
            for (d in Normalizer.normalize(c.toString(), Normalizer.Form.NFD)) {
                if (isMark(d)) continue
                out.append(d.lowercaseChar())
                origin += index
            }
        }
        return out.toString() to origin.toIntArray()
    }

    private fun isMark(c: Char): Boolean =
        Character.getType(c) == Character.NON_SPACING_MARK.toInt()

    private fun merge(ranges: List<IntRange>): List<IntRange> {
        if (ranges.isEmpty()) return ranges
        val sorted = ranges.sortedBy { it.first }
        val merged = mutableListOf(sorted.first())
        for (r in sorted.drop(1)) {
            val last = merged.last()
            if (r.first <= last.last + 1) {
                merged[merged.lastIndex] = last.first..maxOf(last.last, r.last)
            } else {
                merged += r
            }
        }
        return merged
    }
}
