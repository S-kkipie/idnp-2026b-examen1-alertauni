package com.erns.alertauni.data.repository

import com.erns.alertauni.data.local.NotificacionDao
import com.erns.alertauni.data.model.NotificacionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificacionDao: NotificacionDao
) : NotificationRepository {

    override fun observeNotifications(): Flow<List<NotificacionEntity>> =
        notificacionDao.observeNotifications()

    override suspend fun saveNotification(title: String, message: String, date: String) {
        notificacionDao.insertar(
            NotificacionEntity(cursoId = title, mensaje = message, fecha = date)
        )
    }

    override suspend fun deleteNotification(id: Int) = notificacionDao.deleteById(id)

    override suspend fun clearAll() = notificacionDao.deleteAll()
}
