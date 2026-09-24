package com.erns.alertauni.screen.contact

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erns.alertauni.data.model.CourseCatalogRequest
import com.erns.alertauni.data.model.PostPrivateRequest
import com.erns.alertauni.data.model.PostPrivateResponse
import com.erns.alertauni.data.model.StudentEnrollment
import com.erns.alertauni.data.model.StudentEntity
import com.erns.alertauni.data.repository.AnnounceRepository
import com.erns.alertauni.data.repository.ContactRepository
import com.erns.alertauni.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val studentRepository: StudentRepository,
    private val announceRepository: AnnounceRepository
) : ViewModel() {
    private val TAG = "ContactViewModel"
    private val _students = MutableStateFlow<List<StudentEntity>>(emptyList())
    val students: StateFlow<List<StudentEntity>> = _students

    private val _studentEnrollmentList = MutableStateFlow<List<StudentEnrollment>>(emptyList())
    val studentEnrollmentList: StateFlow<List<StudentEnrollment>> =
        _studentEnrollmentList.asStateFlow()

    init {
        getCourses()
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

    fun getStudentByCourse(courseCatalogRequest: CourseCatalogRequest) {
        viewModelScope.launch {
            contactRepository.getStudentsByCourse(courseCatalogRequest).onSuccess {
                _students.value = it
            }.onFailure {
                Log.d(TAG, it.message.toString())
            }
        }
    }

    fun addPostPrivate(postPrivateRequest: PostPrivateRequest) {
        viewModelScope.launch {
            announceRepository.addPostPrivate(postPrivateRequest).onSuccess {
                Log.d(TAG, "Success: ${it.toString()}")
            }.onFailure {
                Log.d(TAG, "Failure: ${it.toString()}")
            }
        }
    }
}