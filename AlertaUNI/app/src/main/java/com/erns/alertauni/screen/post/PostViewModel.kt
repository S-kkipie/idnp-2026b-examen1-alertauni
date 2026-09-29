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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val dataStoreHelper: DataStoreHelper
) : ViewModel() {
    private val TAG = "PostViewModel"

    sealed class PostState {
        object Idle : PostState()
        object Sending : PostState()
        object Saved : PostState()
        object UnSaved : PostState()
    }

    /**
     * Estado del tablero de anuncios. Un único objeto inmutable evita combinaciones
     * inconsistentes (p. ej. "cargando" y "error" a la vez).
     */
    data class BoardUiState(
        val posts: List<PostEntity> = emptyList(),
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val errorMessage: String? = null
    )

    private val _postState = MutableStateFlow<PostState>(PostState.Idle)
    val postState: StateFlow<PostState> = _postState

    private val _boardUiState = MutableStateFlow(BoardUiState())
    val boardUiState: StateFlow<BoardUiState> = _boardUiState.asStateFlow()

    // Eventos de un solo disparo (mensajes para el Snackbar)
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val _catalogCourses = MutableStateFlow<List<CatalogCourseEntity>>(emptyList())
    val catalogCourses: StateFlow<List<CatalogCourseEntity>> = _catalogCourses

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _userType = MutableStateFlow("")
    val userType: StateFlow<String> = _userType

    init {
        loadUserInfo()
        getCourseCatalog()
        getPost()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            _username.value = dataStoreHelper.getFirstname() + " " + dataStoreHelper.getSurname()
            _userType.value = dataStoreHelper.getUserType() ?: ""
        }
    }

    fun refresh() {
        if (_boardUiState.value.isRefreshing) return
        _boardUiState.update { it.copy(isRefreshing = true) }
        getPost()
    }

    private fun getPost() {
        viewModelScope.launch {
            repository.getPosts().onSuccess { posts ->
                _boardUiState.update {
                    it.copy(posts = posts, isLoading = false, isRefreshing = false, errorMessage = null)
                }
            }.onFailure { exception ->
                val message = if (exception is BackendException) {
                    Log.d(
                        TAG,
                        "Error controlado: ${exception.errorData.code} -> ${exception.errorData.message}"
                    )
                    exception.errorData.message
                } else {
                    Log.d(TAG, "Error común: ${exception.message}")
                    "No se pudieron cargar los anuncios. Verifique su conexión."
                }
                // Se conservan los anuncios ya cargados para no dejar la pantalla vacía
                _boardUiState.update {
                    it.copy(isLoading = false, isRefreshing = false, errorMessage = message)
                }
            }
        }
    }

    fun sendPost(
        courseCatalogId: String,
        titlePost: String,
        contentPost: String
    ) {
        if (_postState.value is PostState.Sending) return
        _postState.value = PostState.Sending

        viewModelScope.launch {
            val postRequest = PostRequest(
                course_catalog_id = courseCatalogId,
                title = titlePost.trim(),
                content = contentPost.trim(),
                is_private = false,
                allow_comments = true
            )

            repository.sendPost(postRequest).onSuccess {
                _postState.value = PostState.Saved
                _messages.emit("Anuncio publicado")
                getPost() // El nuevo anuncio aparece sin reiniciar la pantalla
            }.onFailure {
                Log.d(TAG, it.message.toString())
                _postState.value = PostState.UnSaved
                _messages.emit("No se pudo publicar el anuncio. Intente nuevamente.")
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
