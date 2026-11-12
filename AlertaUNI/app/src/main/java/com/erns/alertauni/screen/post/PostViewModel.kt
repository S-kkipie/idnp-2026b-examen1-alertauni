package com.erns.alertauni.screen.post

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erns.alertauni.data.demo.DemoAnnouncements
import com.erns.alertauni.data.model.BackendException
import com.erns.alertauni.data.model.CatalogCourseEntity
import com.erns.alertauni.data.model.CourseCatalogRequest
import com.erns.alertauni.data.model.DraftEntity
import com.erns.alertauni.data.model.PostEntity
import com.erns.alertauni.data.model.PostPrivateRequest
import com.erns.alertauni.data.model.PostRequest
import com.erns.alertauni.data.repository.DraftRepository
import com.erns.alertauni.data.repository.PostLocalStateRepository
import com.erns.alertauni.data.repository.PostRepository
import com.erns.alertauni.data.repository.StudentRepository
import com.erns.alertauni.domain.manager.DataStoreHelper
import com.erns.alertauni.domain.post.AnnouncementQuery
import com.erns.alertauni.domain.post.Audience
import com.erns.alertauni.domain.post.DraftForm
import com.erns.alertauni.domain.post.DraftMapper
import com.erns.alertauni.domain.post.DraftRules
import com.erns.alertauni.domain.post.DraftSummary
import com.erns.alertauni.domain.post.LocalPostState
import com.erns.alertauni.domain.post.Priority
import com.erns.alertauni.domain.post.SearchableAnnouncement
import com.erns.alertauni.domain.post.TypeFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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
    /** Anuncio de DemoAnnouncements: nunca llama al backend. */
    val isSimulated: Boolean = false,
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
    val wallView: WallView = WallView.ACTIVE,
    val courseCodes: List<String> = emptyList(),
    val sections: List<AnnouncementSection> = emptyList(),
    val resultCount: Int = 0,
    /** Anuncios de la vista actual (activos o archivados) antes de buscar/filtrar. */
    val totalInView: Int = 0,
    val archivedCount: Int = 0,
    /** Hay una cuenta a la que asociar los borradores. */
    val canUseDrafts: Boolean = false,
    /** Borradores locales (Room) de la cuenta actual. */
    val drafts: List<DraftSummary> = emptyList(),
    val demoEnabled: Boolean = false,
    val demoCount: Int = 0,
) {
    val showArchived: Boolean get() = wallView == WallView.ARCHIVED
    val showDrafts: Boolean get() = wallView == WallView.DRAFTS
    val draftCount: Int get() = drafts.size
}

data class AnnouncementDraft(
    val courseCatalogId: String,
    val title: String,
    val content: String,
    val priority: Priority = Priority.MEDIA,
    val allowComments: Boolean = true,
    val recipient: StudentOption? = null,
    /** Borrador del que sale este anuncio; se borra cuando el envío funciona. */
    val draftId: Long? = null,
)

sealed interface PostEvent {
    data class Sent(val message: String) : PostEvent
    data class SendFailed(val draft: AnnouncementDraft) : PostEvent
    data class ArchivedChanged(val postId: Int, val archived: Boolean) : PostEvent
    data class CommentsChanged(val enabled: Boolean) : PostEvent
    data class DraftSaved(val updated: Boolean) : PostEvent
    data object DraftNotSaved : PostEvent
    data class DraftDeleted(val id: Long) : PostEvent
    data class DemoDataChanged(val enabled: Boolean, val count: Int) : PostEvent
}

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val studentRepository: StudentRepository,
    private val draftRepository: DraftRepository,
    private val localStateRepository: PostLocalStateRepository,
    private val dataStoreHelper: DataStoreHelper
) : ViewModel() {
    private val TAG = "PostViewModel"

    private data class Meta(val isTeacher: Boolean = false, val isLoading: Boolean = true, val loadFailed: Boolean = false)

    private data class Remote(
        val posts: List<PostEntity>,
        val students: List<StudentOption>,
        val courses: List<CatalogCourseEntity>,
        val meta: Meta,
    )

    private data class Local(
        val owner: String?,
        val drafts: List<DraftEntity>,
        val states: Map<Int, LocalPostState>,
        val demoEnabled: Boolean,
        val demoPosts: List<PostEntity>,
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
    private val view = MutableStateFlow(WallViewOptions())

    // Dueño de los datos locales: uid de la sesión (o correo). Sin dueño no se guarda nada.
    private val ownerAsync = viewModelScope.async {
        DraftRules.ownerOf(dataStoreHelper.getUserUid(), dataStoreHelper.getEmail())
    }
    private val owner: Flow<String?> = flow { emit(ownerAsync.await()) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val draftEntities: Flow<List<DraftEntity>> = owner.flatMapLatest { o ->
        if (o == null) flowOf(emptyList()) else draftRepository.observeDrafts(o)
    }

    // Prioridad, archivado y comentarios: Room (el backend aún no los guarda)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val localStates: StateFlow<Map<Int, LocalPostState>> = owner.flatMapLatest { o ->
        if (o == null) flowOf(emptyMap()) else localStateRepository.observe(o).map { LocalPostState.byPost(it, o) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val demoEnabled: StateFlow<Boolean> =
        dataStoreHelper.demoDataEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    private val demoPosts: Flow<List<PostEntity>> = demoEnabled.map { enabled ->
        if (enabled) DemoAnnouncements.posts(ZonedDateTime.now()) else emptyList()
    }

    private var lastDeletedDraft: DraftEntity? = null

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
            combine(_announcements, _students, _catalogCourses, meta, ::Remote),
            combine(owner, draftEntities, localStates, demoEnabled, demoPosts, ::Local),
            view,
            clock
        ) { remote, local, view, now ->
            PostUiStateBuilder.build(
                WallSources(
                    posts = remote.posts,
                    studentNames = remote.students.associate { it.studentId to it.shortName },
                    catalogCourseCodes = remote.courses.map { it.courseCode },
                    isTeacher = remote.meta.isTeacher,
                    isLoading = remote.meta.isLoading,
                    loadFailed = remote.meta.loadFailed,
                    owner = local.owner,
                    drafts = local.drafts,
                    localStates = local.states,
                    demoEnabled = local.demoEnabled,
                    demoPosts = local.demoPosts,
                ),
                view,
                now
            )
        }
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

    fun setShowArchived(show: Boolean) = setView(if (show) WallView.ARCHIVED else WallView.ACTIVE)

    fun setView(wallView: WallView) = view.update { it.copy(wallView = wallView) }

    // ---- acciones sobre un anuncio (Room, sólo en este dispositivo) ----

    fun setArchived(postId: Int, archived: Boolean, notify: Boolean = true) {
        if ((localStates.value[postId]?.archived ?: false) == archived) return
        viewModelScope.launch {
            val o = ownerAsync.await() ?: return@launch
            localStateRepository.setArchived(o, postId, archived)
            if (notify) _events.trySend(PostEvent.ArchivedChanged(postId, archived))
        }
    }

    fun setCommentsEnabled(postId: Int, enabled: Boolean) {
        viewModelScope.launch {
            val o = ownerAsync.await() ?: return@launch
            localStateRepository.setCommentsEnabled(o, postId, enabled)
            _events.trySend(PostEvent.CommentsChanged(enabled))
        }
    }

    // ---- borradores (Room) ----

    fun saveDraft(form: DraftForm) {
        viewModelScope.launch {
            val o = ownerAsync.await()
            if (o == null || !DraftRules.canSave(form)) {
                _events.trySend(PostEvent.DraftNotSaved)
                return@launch
            }
            draftRepository.saveDraft(DraftMapper.toEntity(form, o, System.currentTimeMillis()))
            _events.trySend(PostEvent.DraftSaved(updated = form.isSaved))
        }
    }

    fun deleteDraft(id: Long) {
        viewModelScope.launch {
            val o = ownerAsync.await() ?: return@launch
            val draft = draftRepository.getDraft(o, id) ?: return@launch
            draftRepository.deleteDraft(o, id)
            lastDeletedDraft = draft
            _events.trySend(PostEvent.DraftDeleted(id))
        }
    }

    fun undoDeleteDraft() {
        val draft = lastDeletedDraft ?: return
        lastDeletedDraft = null
        viewModelScope.launch { draftRepository.saveDraft(draft) }
    }

    // ---- datos de prueba (simulados, nunca van al backend) ----

    fun setDemoDataEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreHelper.setDemoDataEnabled(enabled)
            _events.trySend(PostEvent.DemoDataChanged(enabled, DemoAnnouncements.count))
        }
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
                ownerAsync.await()?.let { o ->
                    localStateRepository.setPriority(o, response.post_id, draft.priority)
                    draft.draftId?.let { draftRepository.deleteDraft(o, it) }
                }
                _announcements.update { posts ->
                    listOf(localPost(draft, response.post_id, response.created_at)) +
                            posts.filterNot { it.id == response.post_id }
                }
                // Que el anuncio nuevo se vea arriba, en "Hoy"
                view.value = WallViewOptions()
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

    private fun String.firstWord(): String = trim().substringBefore(' ')
    private fun String.squash(): String = trim().replace(Regex("\\s+"), " ")
}
