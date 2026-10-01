package com.erns.alertauni.data.repository

import com.erns.alertauni.data.model.NotificacionEntity
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeNotifications(): Flow<List<NotificacionEntity>>
    suspend fun saveNotification(title: String, message: String, date: String)
    suspend fun deleteNotification(id: Int)
    suspend fun clearAll()
}
