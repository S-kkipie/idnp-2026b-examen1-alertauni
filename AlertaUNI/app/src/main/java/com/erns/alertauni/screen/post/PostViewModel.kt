package com.erns.alertauni.screen.post

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erns.alertauni.data.model.BackendException
import com.erns.alertauni.data.model.PostEntity
import com.erns.alertauni.data.model.CatalogCourseEntity
import com.erns.alertauni.data.model.PostRequest
import com.erns.alertauni.data.repository.PostRepository
import com.erns.alertauni.domain.manager.DataStoreHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val dataStoreHelper: DataStoreHelper
) : ViewModel() {
    private val TAG = "PostViewModel"

    sealed class PostState {
        object Loading : PostState()
        object Saved : PostState()
        object UnSaved : PostState()
    }

    private val _postState = MutableStateFlow<PostState>(PostState.Loading)
    val postState: StateFlow<PostState> = _postState
    private val _announcements = MutableStateFlow<List<PostEntity>>(emptyList())
    val announcements: StateFlow<List<PostEntity>> = _announcements

    private val _catalogCourses = MutableStateFlow<List<CatalogCourseEntity>>(emptyList())
    val catalogCourses: StateFlow<List<CatalogCourseEntity>> = _catalogCourses

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    init {
        loadUserInfo()
        getCourseCatalog()
        getPost()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            _username.value = dataStoreHelper.getFirstname() + " " + dataStoreHelper.getSurname()
        }
    }

    private fun getPost() {
        viewModelScope.launch {
            repository.getPosts().onSuccess { posts ->
                _announcements.value = posts
            }.onFailure { exception ->
                if (exception is BackendException) {
                    Log.d(
                        TAG,
                        "Error controlado: ${exception.errorData.code} -> ${exception.errorData.message}"
                    )
                } else {
                    Log.d(TAG, "Error común: ${exception.message}")
                }
            }

        }

    }

    fun sendPost(
        courseCatalogId: String,
        titlePost: String,
        contentPost: String
    ) {
        viewModelScope.launch {
            val postRequest = PostRequest(
                course_catalog_id = courseCatalogId,
                title = titlePost,
                content = contentPost,
                is_private = false,
                allow_comments = true
            )

            repository.sendPost(postRequest).onSuccess { postAddResponse ->
                _postState.value = PostState.Saved

            }.onFailure {
                Log.d(TAG, it.message.toString())
                _postState.value = PostState.UnSaved
            }

        }
    }

    private fun getCourseCatalog() {
        viewModelScope.launch {
            repository.getCourseCatalog().onSuccess { courses ->
                _catalogCourses.value = courses
            }.onFailure {
                Log.d(TAG, "Error loading course catalog: ${it.message}")
            }
        }

    }


}