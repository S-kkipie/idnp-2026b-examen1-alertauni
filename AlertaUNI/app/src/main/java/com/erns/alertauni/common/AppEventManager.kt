package com.erns.alertauni.common

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AppEventManager {
    private val _globalEvents = MutableSharedFlow<AppEvent>(extraBufferCapacity = 1)
    val globalEvents = _globalEvents.asSharedFlow()

    suspend fun emit(event: AppEvent) {
        _globalEvents.emit(event)
    }
}

sealed interface AppEvent {
    data object NavigateToCompleteProfile : AppEvent
    data class ShowToast(val message: String) : AppEvent
}