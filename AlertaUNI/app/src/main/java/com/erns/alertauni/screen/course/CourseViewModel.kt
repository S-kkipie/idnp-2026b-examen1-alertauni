package com.erns.alertauni.screen.course

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erns.alertauni.data.model.BackendException
import com.erns.alertauni.data.model.ClassCodeRequest
import com.erns.alertauni.data.model.CourseEnrollRequest
import com.erns.alertauni.data.model.StudentEnrollment
import com.erns.alertauni.data.repository.StudentRepository
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
    private val dataStoreHelper: DataStoreHelper
) : ViewModel() {
    private val TAG = "CourseViewModel"

    /**
     * Estados del proceso de incorporación a un curso mediante código de clase.
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
        data class Error(val message: String, val course: StudentEnrollment? = null) :
            EnrollUiState()
    }

    private val _enrollUiState = MutableStateFlow<EnrollUiState>(EnrollUiState.Idle)
    val enrollUiState: StateFlow<EnrollUiState> = _enrollUiState.asStateFlow()

    private val _isLoadingCourses = MutableStateFlow(true)
    val isLoadingCourses: StateFlow<Boolean> = _isLoadingCourses.asStateFlow()
    private val _studentEnrollmentList = MutableStateFlow<List<StudentEnrollment>>(emptyList())
    val studentEnrollmentList: StateFlow<List<StudentEnrollment>> =
        _studentEnrollmentList.asStateFlow()
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    init {
        loadUserInfo()
        getCourses()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            _username.value = dataStoreHelper.getFirstname() + " " + dataStoreHelper.getSurname()
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

    fun findCourse(classCode: String) {
        val code = classCode.trim()
        if (code.isEmpty()) {
            _enrollUiState.value = EnrollUiState.NotFound("Ingrese el código del curso")
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
                    getCourses()
                    _enrollUiState.value = EnrollUiState.Enrolled(course)
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
}
