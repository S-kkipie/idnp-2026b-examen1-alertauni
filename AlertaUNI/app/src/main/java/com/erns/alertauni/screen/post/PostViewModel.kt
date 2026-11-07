package com.erns.alertauni.screen.post

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erns.alertauni.data.model.BackendException
import com.erns.alertauni.data.model.CatalogCourseEntity
import com.erns.alertauni.data.model.CourseCatalogRequest
import com.erns.alertauni.data.model.PostEntity
import com.erns.alertauni.data.model.PostPrivateRequest
import com.erns.alertauni.data.model.PostRequest
import com.erns.alertauni.data.repository.PostRepository
import com.erns.alertauni.data.repository.StudentRepository
import com.erns.alertauni.domain.manager.DataStoreHelper
import com.erns.alertauni.domain.post.AnnouncementFilter
import com.erns.alertauni.domain.post.AnnouncementQuery
import com.erns.alertauni.domain.post.Audience
import com.erns.alertauni.domain.post.Priority
import com.erns.alertauni.domain.post.RelativeDate
import com.erns.alertauni.domain.post.SearchableAnnouncement
import com.erns.alertauni.domain.post.TypeFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZonedDateTime
import javax.inject.Inject

/** Estudiante que puede recibir un anuncio privado. Aparece una vez por cada curso en que está inscrito. */
data class StudentOption(
    val studentId: String,
    val code: String,
    val fullName: String,
    val shortName: String,
    val courseCatalogId: String,
    val courseCode: String,
) {
    val key: String get() = "$courseCatalogId/$studentId"
}

/** Anuncio listo para dibujar en el muro. */
data class AnnouncementUi(
    val id: Int,
    val title: String,
    val content: String,
    val courseCode: String,
    val courseName: String,
    val isPrivate: Boolean,
    /** Nombre del destinatario si se conoce (sólo el docente carga la lista de estudiantes). */
    val recipientName: String?,
    /** "Para: Juan Pérez", "Para ti" o "Privado". */
    val privateLabel: String,
    val priority: Priority,
    val allowComments: Boolean,
    val commentCount: Int,
    val dateLabel: String,
    val publishedAt: Instant?,
    val isArchived: Boolean,
) {
    val searchable: SearchableAnnouncement
        get() = SearchableAnnouncement(title, content, courseCode, courseName, recipientName, isPrivate, priority)
}

data class AnnouncementSection(val label: String, val items: List<AnnouncementUi>)

data class PostUiState(
    val isTeacher: Boolean = false,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val query: AnnouncementQuery = AnnouncementQuery(),
    val showArchived: Boolean = false,
    val courseCodes: List<String> = emptyList(),
    val sections: List<AnnouncementSection> = emptyList(),
    val resultCount: Int = 0,
    /** Anuncios de la vista actual (activos o archivados) antes de buscar/filtrar. */
    val totalInView: Int = 0,
    val archivedCount: Int = 0,
)

data class AnnouncementDraft(
    val courseCatalogId: String,
    val title: String,
    val content: String,
    val priority: Priority = Priority.MEDIA,
    val allowComments: Boolean = true,
    val recipient: StudentOption? = null,
)

sealed interface PostEvent {
    data class Sent(val message: String) : PostEvent
    data class SendFailed(val draft: AnnouncementDraft) : PostEvent
    data class ArchivedChanged(val postId: Int, val archived: Boolean) : PostEvent
    data class CommentsChanged(val enabled: Boolean) : PostEvent
}

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val studentRepository: StudentRepository,
    private val dataStoreHelper: DataStoreHelper
) : ViewModel() {
    private val TAG = "PostViewModel"

    private data class Meta(val isTeacher: Boolean = false, val isLoading: Boolean = true, val loadFailed: Boolean = false)
    private data class ViewOptions(val query: AnnouncementQuery = AnnouncementQuery(), val showArchived: Boolean = false)

    // Prototipo: el backend aún no guarda archivado, prioridad ni cambios de comentarios
    private data class LocalEdits(
        val archived: Set<Int> = emptySet(),
        val comments: Map<Int, Boolean> = emptyMap(),
        val priorities: Map<Int, Priority> = emptyMap(),
    )

    private data class Sources(
        val posts: List<PostEntity>,
        val students: List<StudentOption>,
        val courses: List<CatalogCourseEntity>,
        val meta: Meta,
    )

    private val _announcements = MutableStateFlow<List<PostEntity>>(emptyList())
    val announcements: StateFlow<List<PostEntity>> = _announcements

    private val _catalogCourses = MutableStateFlow<List<CatalogCourseEntity>>(emptyList())
    val catalogCourses: StateFlow<List<CatalogCourseEntity>> = _catalogCourses

    private val _students = MutableStateFlow<List<StudentOption>>(emptyList())
    val students: StateFlow<List<StudentOption>> = _students

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val meta = MutableStateFlow(Meta())
    private val view = MutableStateFlow(ViewOptions())
    private val edits = MutableStateFlow(LocalEdits())

    private val _events = Channel<PostEvent>(Channel.BUFFERED)
    val events: Flow<PostEvent> = _events.receiveAsFlow()

    // Refresca "hace 5 min" mientras la pantalla está visible
    private val clock: Flow<ZonedDateTime> = flow {
        while (true) {
            emit(ZonedDateTime.now())
            delay(60_000)
        }
    }

    val uiState: StateFlow<PostUiState> =
        combine(
            combine(_announcements, _students, _catalogCourses, meta, ::Sources),
            view,
            edits,
            clock
        ) { sources, view, edits, now -> buildState(sources, view, edits, now) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PostUiState())

    init {
        loadUserInfo()
        loadPosts()
        loadCoursesAndStudents()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            _username.value = dataStoreHelper.getFirstname() + " " + dataStoreHelper.getSurname()
        }
    }

    fun refresh() = loadPosts()

    private fun loadPosts() {
        viewModelScope.launch {
            if (_announcements.value.isEmpty()) meta.update { it.copy(isLoading = true, loadFailed = false) }
            repository.getPosts().onSuccess { posts ->
                _announcements.value = posts
                meta.update { it.copy(isLoading = false, loadFailed = false) }
            }.onFailure { exception ->
                // Sin anuncios o sin cursos no es un error para el usuario
                val empty = exception is BackendException || exception.message == "No posts found"
                if (exception is BackendException) {
                    Log.d(TAG, "Error controlado: ${exception.errorData.code} -> ${exception.errorData.message}")
                } else {
                    Log.d(TAG, "Error común: ${exception.message}")
                }
                meta.update { it.copy(isLoading = false, loadFailed = !empty) }
            }
        }
    }

    private fun loadCoursesAndStudents() {
        viewModelScope.launch {
            val isTeacher = dataStoreHelper.getUserType() == "PROFESSOR"
            meta.update { it.copy(isTeacher = isTeacher) }
            repository.getCourseCatalog().onSuccess { courses ->
                _catalogCourses.value = courses
                if (isTeacher) _students.value = loadStudents(courses)
            }.onFailure {
                Log.d(TAG, "Error loading course catalog: ${it.message}")
            }
        }
    }

    private suspend fun loadStudents(courses: List<CatalogCourseEntity>): List<StudentOption> = coroutineScope {
        courses.map { course ->
            async {
                studentRepository.courseStudent(CourseCatalogRequest(course.id))
                    .onFailure { Log.d(TAG, "Error loading students of ${course.courseCode}: ${it.message}") }
                    .getOrDefault(emptyList())
                    .map { student ->
                        StudentOption(
                            studentId = student.student_id,
                            code = student.codigo,
                            fullName = "${student.firstname} ${student.surname}".squash(),
                            shortName = "${student.firstname.firstWord()} ${student.surname.firstWord()}".squash(),
                            courseCatalogId = course.id,
                            courseCode = course.courseCode,
                        )
                    }
            }
        }.awaitAll().flatten()
    }

    // ---- búsqueda y filtros ----

    fun onSearchChange(text: String) = view.update { it.copy(query = it.query.copy(text = text)) }

    fun toggleType(type: TypeFilter) = view.update {
        val next = if (it.query.type == type) TypeFilter.ALL else type
        it.copy(query = it.query.copy(type = next))
    }

    fun toggleOnlyHigh() = view.update { it.copy(query = it.query.copy(onlyHigh = !it.query.onlyHigh)) }

    fun toggleCourse(courseCode: String) = view.update {
        val next = if (it.query.courseCode == courseCode) null else courseCode
        it.copy(query = it.query.copy(courseCode = next))
    }

    fun clearFilters() = view.update { it.copy(query = AnnouncementQuery()) }

    fun setShowArchived(show: Boolean) = view.update { it.copy(showArchived = show) }

    // ---- acciones sobre un anuncio (sólo en memoria) ----

    fun setArchived(postId: Int, archived: Boolean, notify: Boolean = true) {
        val current = edits.value.archived
        if ((postId in current) == archived) return
        edits.update { it.copy(archived = if (archived) it.archived + postId else it.archived - postId) }
        if (notify) _events.trySend(PostEvent.ArchivedChanged(postId, archived))
    }

    fun setCommentsEnabled(postId: Int, enabled: Boolean) {
        edits.update { it.copy(comments = it.comments + (postId to enabled)) }
        _events.trySend(PostEvent.CommentsChanged(enabled))
    }

    // ---- envío ----

    fun audienceFor(courseCatalogId: String, recipient: StudentOption?): Audience {
        val code = _catalogCourses.value.firstOrNull { it.id == courseCatalogId }?.courseCode.orEmpty()
        val count = _students.value.count { it.courseCatalogId == courseCatalogId }.takeIf { it > 0 }
        return Audience(code, count, recipient?.shortName)
    }

    // Firma anterior (AddPostDialog) con los datos nuevos como opcionales
    fun sendPost(
        courseCatalogId: String,
        titlePost: String,
        contentPost: String,
        priority: Priority = Priority.MEDIA,
        allowComments: Boolean = true,
        recipient: StudentOption? = null,
    ) = send(AnnouncementDraft(courseCatalogId, titlePost.trim(), contentPost.trim(), priority, allowComments, recipient))

    fun send(draft: AnnouncementDraft) {
        viewModelScope.launch {
            val result = if (draft.recipient == null) {
                repository.sendPost(
                    PostRequest(
                        course_catalog_id = draft.courseCatalogId,
                        title = draft.title,
                        content = draft.content,
                        is_private = false,
                        allow_comments = draft.allowComments,
                        priority = draft.priority.code
                    )
                )
            } else {
                repository.sendPostPrivate(
                    PostPrivateRequest(
                        courseCatalogId = draft.courseCatalogId,
                        title = draft.title,
                        content = draft.content,
                        receiverPrivate = draft.recipient.studentId,
                        allowComments = draft.allowComments,
                        priority = draft.priority.code
                    )
                )
            }
            result.onSuccess { response ->
                edits.update { it.copy(priorities = it.priorities + (response.post_id to draft.priority)) }
                _announcements.update { posts ->
                    listOf(localPost(draft, response.post_id, response.created_at)) +
                            posts.filterNot { it.id == response.post_id }
                }
                // Que el anuncio nuevo se vea arriba, en "Hoy"
                view.value = ViewOptions()
                _events.trySend(PostEvent.Sent(audienceFor(draft.courseCatalogId, draft.recipient).sentMessage))
                loadPosts()
            }.onFailure {
                Log.d(TAG, it.message.toString())
                _events.trySend(PostEvent.SendFailed(draft))
            }
        }
    }

    private fun localPost(draft: AnnouncementDraft, id: Int, createdAt: String): PostEntity {
        val course = _catalogCourses.value.firstOrNull { it.id == draft.courseCatalogId }
        return PostEntity(
            id = id,
            authorName = _username.value,
            authorType = if (meta.value.isTeacher) 1 else 2,
            courseCode = course?.courseCode.orEmpty(),
            courseName = course?.courseName.orEmpty(),
            title = draft.title,
            content = draft.content,
            allowComments = draft.allowComments,
            countComments = 0,
            isPrivate = draft.recipient != null,
            date = createdAt,
            receiverPrivate = draft.recipient?.studentId,
            priority = draft.priority.code
        )
    }

    private fun buildState(sources: Sources, view: ViewOptions, edits: LocalEdits, now: ZonedDateTime): PostUiState {
        val isTeacher = sources.meta.isTeacher
        val names = sources.students.associate { it.studentId to it.shortName }
        val all = sources.posts.map { post ->
            val recipientName = if (post.isPrivate) post.receiverPrivate?.let { names[it] } else null
            val publishedAt = RelativeDate.parse(post.date)
            AnnouncementUi(
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
                priority = edits.priorities[post.id] ?: Priority.fromCode(post.priority),
                allowComments = edits.comments[post.id] ?: post.allowComments,
                commentCount = post.countComments,
                dateLabel = publishedAt?.let { RelativeDate.format(it, now) } ?: post.date,
                publishedAt = publishedAt,
                isArchived = post.id in edits.archived,
            )
        }
        val inView = all.filter { it.isArchived == view.showArchived }
        val visible = AnnouncementFilter.filter(inView, view.query) { it.searchable }
        val sections = RelativeDate.group(visible, now) { it.publishedAt }
            .map { (label, items) -> AnnouncementSection(label, items) }
        val courseCodes = (sources.courses.map { it.courseCode } + sources.posts.map { it.courseCode })
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
        return PostUiState(
            isTeacher = isTeacher,
            isLoading = sources.meta.isLoading,
            loadFailed = sources.meta.loadFailed,
            query = view.query,
            showArchived = view.showArchived,
            courseCodes = courseCodes,
            sections = sections,
            resultCount = visible.size,
            totalInView = inView.size,
            archivedCount = all.count { it.isArchived },
        )
    }

    private fun String.firstWord(): String = trim().substringBefore(' ')
    private fun String.squash(): String = trim().replace(Regex("\\s+"), " ")
}
