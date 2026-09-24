package com.erns.alertauni.screen.post

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erns.alertauni.data.model.CommentDto
import com.erns.alertauni.data.model.CommentEntity
import com.erns.alertauni.data.model.CommentRequest
import com.erns.alertauni.data.repository.CommentRepository
import com.erns.alertauni.domain.manager.DataStoreHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostCommentViewModel @Inject constructor(
    private val repository: CommentRepository,
    //private val sessionRepository: SessionRepository,
    private val savedStateHandle: SavedStateHandle,
    private val dataStoreHelper: DataStoreHelper
) : ViewModel() {
    private val TAG = "CommentViewModel"

    sealed class CommentState {
        object Loading : CommentState()
        object Saved : CommentState()
        object UnSaved : CommentState()
    }

    val _postId: String =
        savedStateHandle["postId"] ?: throw IllegalArgumentException("Posts ID is required")

    private val _commentState = MutableStateFlow<CommentState>(CommentState.Loading)
    val commentState: StateFlow<CommentState> = _commentState
    private val _comments = MutableStateFlow<List<CommentEntity>>(emptyList())
    val comments: StateFlow<List<CommentEntity>> = _comments
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    init {
        loadUserInfo()
        getComments(_postId.toInt())
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            _username.value = dataStoreHelper.getFirstname() + " " + dataStoreHelper.getSurname()
        }
    }

    private fun getComments(postId: Int) {
        viewModelScope.launch {
            val commentDto = CommentDto(post_id = postId)

            repository.getComments(commentDto).onSuccess { comments ->
                _comments.value = comments
            }.onFailure {
                Log.d("CommentViewModel", "Error loading comments: ${it.message}")
            }
        }
    }

    fun sendComment(comment: String, isPrivate: Boolean = false) {
        Log.d("CommentViewModel", "Sending comment: $comment for postId: ${_postId}")

        val commentRequest = CommentRequest(
            post_id = _postId,
            content = comment,
            is_private = isPrivate
        )

        viewModelScope.launch {
            //_commentState.value = ResourceState.Loading
            repository.sendComment(commentRequest).onSuccess { result ->
                if (result) {
                    _commentState.value = CommentState.Saved
                    val commentEntity = CommentEntity(
                        id = 0, // El ID se asignará en el backend
                        postId = _postId.toInt(),
                        authorName = "Tú",
                        content = comment,
                        email = "", // No es necesario para comentarios locales
                        isMine = true,
                        publishedDate = "Ahora"
                    )
                    _comments.update { currentList ->
                        currentList + commentEntity
                    }

                } else {
                    _commentState.value = CommentState.UnSaved
                }
            }.onFailure {
                Log.d(TAG, it.message.toString())
            }
        }

    }
}