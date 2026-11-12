package com.erns.alertauni.screen.post

import com.erns.alertauni.data.demo.DemoAnnouncements
import com.erns.alertauni.data.model.DraftEntity
import com.erns.alertauni.data.model.PostEntity
import com.erns.alertauni.domain.post.AnnouncementFilter
import com.erns.alertauni.domain.post.AnnouncementQuery
import com.erns.alertauni.domain.post.DraftRules
import com.erns.alertauni.domain.post.LocalPostState
import com.erns.alertauni.domain.post.RelativeDate
import java.time.ZonedDateTime

enum class WallView { ACTIVE, ARCHIVED, DRAFTS }

data class WallViewOptions(val query: AnnouncementQuery = AnnouncementQuery(), val wallView: WallView = WallView.ACTIVE)

/** Todo lo que el muro combina: backend, Room y datos simulados. */
data class WallSources(
    /** Backend (Supabase). */
    val posts: List<PostEntity> = emptyList(),
    val studentNames: Map<String, String> = emptyMap(),
    val catalogCourseCodes: List<String> = emptyList(),
    val isTeacher: Boolean = false,
    val isLoading: Boolean = false,
    val loadFailed: Boolean = false,
    /** Room, de la cuenta [owner]. */
    val owner: String? = null,
    val drafts: List<DraftEntity> = emptyList(),
    val localStates: Map<Int, LocalPostState> = emptyMap(),
    /** Simulados: sólo se muestran al docente con el interruptor encendido. */
    val demoEnabled: Boolean = false,
    val demoPosts: List<PostEntity> = emptyList(),
)

/** Arma el estado del muro sin Android ni repositorios: la usa el ViewModel y las capturas de pantalla. */
object PostUiStateBuilder {

    fun build(sources: WallSources, view: WallViewOptions, now: ZonedDateTime): PostUiState {
        val isTeacher = sources.isTeacher
        val showDemo = isTeacher && sources.demoEnabled
        val demo = if (showDemo) sources.demoPosts else emptyList()
        val names = sources.studentNames + if (showDemo) DemoAnnouncements.studentNames else emptyMap()
        val all = (sources.posts + demo).map { post -> toUi(post, isTeacher, names, sources.localStates, now) }

        val inView = when (view.wallView) {
            WallView.ACTIVE -> all.filterNot { it.isArchived }
            WallView.ARCHIVED -> all.filter { it.isArchived }
            WallView.DRAFTS -> emptyList()
        }
        val visible = AnnouncementFilter.filter(inView, view.query) { it.searchable }
        val sections = RelativeDate.group(visible, now) { it.publishedAt }
            .map { (label, items) -> AnnouncementSection(label, items) }
        val courseCodes = (sources.catalogCourseCodes + all.map { it.courseCode })
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
        return PostUiState(
            isTeacher = isTeacher,
            isLoading = sources.isLoading,
            loadFailed = sources.loadFailed,
            query = view.query,
            wallView = view.wallView,
            courseCodes = courseCodes,
            sections = sections,
            resultCount = visible.size,
            totalInView = inView.size,
            archivedCount = all.count { it.isArchived },
            canUseDrafts = sources.owner != null,
            drafts = DraftRules.visibleFor(sources.drafts, sources.owner, now),
            demoEnabled = showDemo,
            demoCount = demo.size,
        )
    }

    private fun toUi(
        post: PostEntity,
        isTeacher: Boolean,
        names: Map<String, String>,
        localStates: Map<Int, LocalPostState>,
        now: ZonedDateTime,
    ): AnnouncementUi {
        val local = localStates[post.id] ?: LocalPostState.NONE
        val recipientName = if (post.isPrivate) post.receiverPrivate?.let { names[it] } else null
        val publishedAt = RelativeDate.parse(post.date)
        return AnnouncementUi(
            id = post.id,
            title = post.title,
            content = post.content,
            courseCode = post.courseCode,
            courseName = post.courseName.orEmpty(),
            isPrivate = post.isPrivate,
            recipientName = recipientName,
            privateLabel = when {
                recipientName != null -> "Para: $recipientName"
                // El estudiante sólo ve los privados que recibió (get_posts_fun)
                !isTeacher -> "Para ti"
                else -> "Privado"
            },
            priority = local.priorityOr(post.priority),
            allowComments = local.commentsOr(post.allowComments),
            commentCount = post.countComments,
            dateLabel = publishedAt?.let { RelativeDate.format(it, now) } ?: post.date,
            publishedAt = publishedAt,
            isArchived = local.archived,
            isSimulated = DemoAnnouncements.isSimulated(post.id),
        )
    }
}
