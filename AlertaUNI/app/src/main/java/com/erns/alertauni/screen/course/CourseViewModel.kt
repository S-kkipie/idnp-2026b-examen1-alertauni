package com.erns.alertauni.screen.course

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erns.alertauni.data.model.BackendException
import com.erns.alertauni.data.model.ClassCodeActionRequest
import com.erns.alertauni.data.model.ClassCodeInfo
import com.erns.alertauni.data.model.ClassCodeRequest
import com.erns.alertauni.data.model.CourseEnrollRequest
import com.erns.alertauni.data.model.CourseEnrollResponse
import com.erns.alertauni.data.model.EnrollmentRequest
import com.erns.alertauni.data.model.StudentEnrollment
import com.erns.alertauni.data.repository.CourseRepository
import com.erns.alertauni.data.repository.StudentRepository
import com.erns.alertauni.domain.course.JoinCode
import com.erns.alertauni.domain.manager.DataStoreHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val courseRepository: CourseRepository,
    private val dataStoreHelper: DataStoreHelper
) : ViewModel() {
    private val TAG = "CourseViewModel"

    /**
     * Estados del proceso de incorporación a un curso (código escrito o QR escaneado).
     * Cada estado corresponde a una variante visual del diálogo AddCourseDialog.
     */
    sealed class EnrollUiState {
        object Idle : EnrollUiState()
        object Searching : EnrollUiState()
        data class CourseFound(val course: StudentEnrollment) : EnrollUiState()
        data class NotFound(val message: String) : EnrollUiState()
        data class AlreadyEnrolled(val course: StudentEnrollment) : EnrollUiState()
        data class Enrolling(val course: StudentEnrollment) : EnrollUiState()
        data class Enrolled(val course: StudentEnrollment) : EnrollUiState()
        data class PendingApproval(val course: StudentEnrollment) : EnrollUiState()
        data class Error(val message: String, val course: StudentEnrollment? = null) :
            EnrollUiState()
    }

    /** Estado del código de inscripción que el docente comparte con su curso. */
    sealed class ClassCodeUiState {
        object Hidden : ClassCodeUiState()
        data class Loading(val course: StudentEnrollment) : ClassCodeUiState()
        data class Ready(
            val course: StudentEnrollment,
            val info: ClassCodeInfo,
            val isUpdating: Boolean = false,
            val requests: List<EnrollmentRequest> = emptyList(),
            val isLoadingRequests: Boolean = true,
            val reviewingIds: Set<Long> = emptySet(),
            val message: String? = null
        ) : ClassCodeUiState()

        data class Error(val course: StudentEnrollment, val message: String) : ClassCodeUiState()
    }

    private val _enrollUiState = MutableStateFlow<EnrollUiState>(EnrollUiState.Idle)
    val enrollUiState: StateFlow<EnrollUiState> = _enrollUiState.asStateFlow()

    private val _classCodeUiState = MutableStateFlow<ClassCodeUiState>(ClassCodeUiState.Hidden)
    val classCodeUiState: StateFlow<ClassCodeUiState> = _classCodeUiState.asStateFlow()

    private val _isLoadingCourses = MutableStateFlow(true)
    val isLoadingCourses: StateFlow<Boolean> = _isLoadingCourses.asStateFlow()
    private val _studentEnrollmentList = MutableStateFlow<List<StudentEnrollment>>(emptyList())
    val studentEnrollmentList: StateFlow<List<StudentEnrollment>> =
        _studentEnrollmentList.asStateFlow()
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username
    private val _isProfessor = MutableStateFlow(false)
    val isProfessor: StateFlow<Boolean> = _isProfessor.asStateFlow()

    init {
        loadUserInfo()
        getCourses()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            _username.value = dataStoreHelper.getFirstname() + " " + dataStoreHelper.getSurname()
            _isProfessor.value = dataStoreHelper.getUserType() == "PROFESSOR"
        }
    }

    private fun getCourses() {
        viewModelScope.launch {
            _isLoadingCourses.value = true
            studentRepository.getStudentEnrollment()
                .onSuccess {
                    _studentEnrollmentList.value = it
                }.onFailure {
                    Log.d(TAG, it.message.toString())
                }
            _isLoadingCourses.value = false
        }
    }

    // region Estudiante: incorporación al curso

    fun findCourse(classCode: String) {
        val code = JoinCode.parse(classCode)
        if (code == null) {
            _enrollUiState.value = EnrollUiState.NotFound(
                if (classCode.isBlank()) "Ingrese el código del curso"
                else "El código tiene un formato inválido. Ejemplo: IDNP-7K3Q"
            )
            return
        }
        if (_enrollUiState.value is EnrollUiState.Searching) return

        _enrollUiState.value = EnrollUiState.Searching
        viewModelScope.launch {
            studentRepository.findCourse(ClassCodeRequest(classCode = code)).onSuccess { course ->
                val alreadyEnrolled = _studentEnrollmentList.value.any {
                    it.course_catalog_id == course.course_catalog_id
                }
                _enrollUiState.value = if (alreadyEnrolled) {
                    EnrollUiState.AlreadyEnrolled(course)
                } else {
                    EnrollUiState.CourseFound(course)
                }
            }.onFailure {
                Log.d(TAG, it.message.toString())
                _enrollUiState.value = EnrollUiState.NotFound(
                    "No se encontró un curso con el código \"$code\" o su inscripción está cerrada"
                )
            }
        }
    }

    /** Resultado del escáner: el QR contiene el mismo código que se puede escribir. */
    fun onQrScanned(rawValue: String?) {
        val code = JoinCode.parse(rawValue)
        if (code == null) {
            _enrollUiState.value =
                EnrollUiState.NotFound("El QR escaneado no corresponde a un curso de AlertaUNI")
            return
        }
        findCourse(code)
    }

    fun onScanError() {
        _enrollUiState.value = EnrollUiState.Error(
            "No se pudo abrir el escáner. Ingrese el código manualmente."
        )
    }

    fun courseEnroll() {
        val course = when (val state = _enrollUiState.value) {
            is EnrollUiState.CourseFound -> state.course
            is EnrollUiState.Error -> state.course ?: return
            else -> return // Evita doble envío o inscripción sin curso válido
        }

        _enrollUiState.value = EnrollUiState.Enrolling(course)
        viewModelScope.launch {
            studentRepository.courseEnroll(CourseEnrollRequest(courseCatalogId = course.course_catalog_id))
                .onSuccess {
                    Log.d(TAG, "Created " + it.created)
                    if (it.status == CourseEnrollResponse.STATUS_PENDING) {
                        // No figura en la lista oficial: el docente debe aprobar la solicitud
                        _enrollUiState.value = EnrollUiState.PendingApproval(course)
                    } else {
                        getCourses()
                        _enrollUiState.value = EnrollUiState.Enrolled(course)
                    }
                }.onFailure {
                    Log.d(TAG, it.message.toString())
                    val message = if (it is BackendException) it.errorData.message
                    else "No se pudo completar la inscripción. Verifique su conexión e intente nuevamente."
                    _enrollUiState.value = EnrollUiState.Error(message, course)
                }
        }
    }

    fun clearEnrollment() {
        _enrollUiState.value = EnrollUiState.Idle
    }

    // endregion

    // region Docente: código de inscripción

    fun openClassCode(course: StudentEnrollment) {
        _classCodeUiState.value = ClassCodeUiState.Loading(course)
        runClassCodeAction(course, ClassCodeActionRequest.GET)
    }

    fun regenerateClassCode() = updateClassCode(ClassCodeActionRequest.REGENERATE)

    fun setEnrollmentOpen(open: Boolean) =
        updateClassCode(if (open) ClassCodeActionRequest.OPEN else ClassCodeActionRequest.CLOSE)

    fun closeClassCode() {
        _classCodeUiState.value = ClassCodeUiState.Hidden
    }

    private fun updateClassCode(action: String) {
        val state = _classCodeUiState.value as? ClassCodeUiState.Ready ?: return
        if (state.isUpdating) return
        _classCodeUiState.value = state.copy(isUpdating = true)
        runClassCodeAction(state.course, action)
    }

    private fun runClassCodeAction(course: StudentEnrollment, action: String) {
        viewModelScope.launch {
            courseRepository.classCode(ClassCodeActionRequest(course.course_catalog_id, action))
                .onSuccess { info ->
                    val previous = _classCodeUiState.value as? ClassCodeUiState.Ready
                    _classCodeUiState.value = previous?.copy(info = info, isUpdating = false)
                        ?: ClassCodeUiState.Ready(course, info)
                    if (previous == null) loadEnrollmentRequests(course)
                }.onFailure {
                    Log.d(TAG, it.message.toString())
                    val message = if (it is BackendException) it.errorData.message
                    else "No se pudo obtener el código del curso."
                    val previous = _classCodeUiState.value as? ClassCodeUiState.Ready
                    // Si falla una acción (regenerar, abrir/cerrar) se conserva lo ya mostrado
                    _classCodeUiState.value = previous?.copy(isUpdating = false, message = message)
                        ?: ClassCodeUiState.Error(course, message)
                }
        }
    }

    // Solicitudes de estudiantes que no figuran en la lista oficial

    private fun loadEnrollmentRequests(course: StudentEnrollment) {
        viewModelScope.launch {
            val result = courseRepository.getEnrollmentRequests(course.course_catalog_id)
            updateReady { state ->
                result.fold(
                    onSuccess = { state.copy(requests = it, isLoadingRequests = false) },
                    onFailure = {
                        Log.d(TAG, it.message.toString())
                        state.copy(
                            isLoadingRequests = false,
                            message = "No se pudieron cargar las solicitudes pendientes"
                        )
                    }
                )
            }
        }
    }

    fun approveRequest(request: EnrollmentRequest) = reviewRequest(request, approve = true)

    fun rejectRequest(request: EnrollmentRequest) = reviewRequest(request, approve = false)

    private fun reviewRequest(request: EnrollmentRequest, approve: Boolean) {
        val state = _classCodeUiState.value as? ClassCodeUiState.Ready ?: return
        if (request.requestId in state.reviewingIds) return // evita doble toque
        _classCodeUiState.value = state.copy(reviewingIds = state.reviewingIds + request.requestId)

        viewModelScope.launch {
            val result = courseRepository.reviewEnrollmentRequest(
                request.courseCatalogId, request.requestId, approve
            )
            updateReady { current ->
                val reviewing = current.reviewingIds - request.requestId
                result.fold(
                    onSuccess = {
                        current.copy(
                            requests = current.requests.filterNot { it.requestId == request.requestId },
                            reviewingIds = reviewing,
                            info = current.info.copy(
                                pendingCount = (current.info.pendingCount - 1).coerceAtLeast(0),
                                enrolledCount = current.info.enrolledCount + if (approve) 1 else 0
                            ),
                            message = if (approve) "${request.displayName} fue incorporado al curso"
                            else "Solicitud de ${request.displayName} rechazada"
                        )
                    },
                    onFailure = {
                        Log.d(TAG, it.message.toString())
                        current.copy(
                            reviewingIds = reviewing,
                            message = "No se pudo procesar la solicitud. Intente nuevamente."
                        )
                    }
                )
            }
        }
    }

    fun clearClassCodeMessage() = updateReady { it.copy(message = null) }

    private inline fun updateReady(transform: (ClassCodeUiState.Ready) -> ClassCodeUiState.Ready) {
        val state = _classCodeUiState.value as? ClassCodeUiState.Ready ?: return
        _classCodeUiState.value = transform(state)
    }

    // endregion
}
