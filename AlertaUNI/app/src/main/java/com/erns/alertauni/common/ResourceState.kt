package com.erns.alertauni.common

sealed interface ResourceState<out T> {
    object Idle : ResourceState<Nothing>
    object Loading : ResourceState<Nothing>
    data class Success<out T>(val data: T) : ResourceState<T>
    data class Error(val message: String) : ResourceState<Nothing>
}