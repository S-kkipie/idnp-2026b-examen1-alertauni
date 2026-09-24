package com.erns.alertauni.screen.course

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erns.alertauni.data.model.ClassCodeRequest
import com.erns.alertauni.data.model.CourseCatalogEntity
import com.erns.alertauni.data.model.CourseEnrollRequest
import com.erns.alertauni.data.model.GoogleAccountRequest
import com.erns.alertauni.data.model.StudentEnrollment
import com.erns.alertauni.data.repository.CourseRepository
import com.erns.alertauni.data.repository.StudentRepository
import com.erns.alertauni.domain.manager.DataStoreHelper
import com.erns.alertauni.screen.post.PostViewModel.PostState
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

    sealed class CourseState {
        object Loading : CourseState()
        object Saved : CourseState()
        object UnSaved : CourseState()
    }

    private val _courseState = MutableStateFlow<CourseState>(CourseState.Loading)
    val courseState: StateFlow<CourseState> = _courseState
    private val _studentEnrollmentList = MutableStateFlow<List<StudentEnrollment>>(emptyList())
    val studentEnrollmentList: StateFlow<List<StudentEnrollment>> =
        _studentEnrollmentList.asStateFlow()
    private val _studentEnrollment = MutableStateFlow<StudentEnrollment?>(null)
    val studentEnrollment: StateFlow<StudentEnrollment?> =
        _studentEnrollment.asStateFlow()
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
            studentRepository.getStudentEnrollment()
                .onSuccess {
                    _studentEnrollmentList.value = it

                }.onFailure {
                    Log.d(TAG, it.message.toString())
                }
        }

    }

    fun findCourse(classCode: String) {
        viewModelScope.launch {
            studentRepository.findCourse(ClassCodeRequest(classCode = classCode)).onSuccess {
                Log.d(TAG, "succefull")
                _studentEnrollment.value = it
            }.onFailure {
                Log.d(TAG, it.message.toString())
                _studentEnrollment.value = null
            }
        }
    }

    fun clearEnrollment() {
        _studentEnrollment.value = null
    }

    fun courseEnroll(courseCatalogId: String) {
        viewModelScope.launch {
            studentRepository.courseEnroll(CourseEnrollRequest(courseCatalogId = courseCatalogId))
                .onSuccess {
                    Log.d(TAG, "Created " + it.created)
                    getCourses()
                    _courseState.value = CourseState.Saved
                }.onFailure {
                    Log.d(TAG, it.message.toString())
                    _courseState.value = CourseState.UnSaved
                }
        }
    }
}