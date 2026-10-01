package com.erns.alertauni.screen.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erns.alertauni.data.model.NotificacionEntity
import com.erns.alertauni.data.repository.NotificationRepository
import com.erns.alertauni.domain.manager.DataStoreHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val dataStoreHelper: DataStoreHelper
) : ViewModel() {

    sealed class NotificationUiState {
        object Loading : NotificationUiState()
        object Empty : NotificationUiState()
        data class Content(val notifications: List<NotificacionEntity>) : NotificationUiState()
    }

    /**
     * La bandeja observa Room: cuando MiFirebaseService inserta una notificación,
     * la lista se actualiza sola, sin recargar la pantalla.
     * WhileSubscribed(5000) detiene la observación si la pantalla deja de mostrarse
     * (y la conserva durante una rotación).
     */
    val uiState: StateFlow<NotificationUiState> = notificationRepository.observeNotifications()
        .map { list ->
            if (list.isEmpty()) NotificationUiState.Empty
            else NotificationUiState.Content(list)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NotificationUiState.Loading
        )

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    init {
        viewModelScope.launch {
            _username.value = dataStoreHelper.getFirstname() + " " + dataStoreHelper.getSurname()
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch { notificationRepository.deleteNotification(id) }
    }

    fun clearAll() {
        viewModelScope.launch { notificationRepository.clearAll() }
    }
}
