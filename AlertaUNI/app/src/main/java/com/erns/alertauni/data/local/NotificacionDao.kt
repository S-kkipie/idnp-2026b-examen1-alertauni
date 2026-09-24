package com.erns.alertauni.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.erns.alertauni.data.model.NotificacionEntity

@Dao
interface NotificacionDao {
    @Insert
    suspend fun insertar(notificacionEntity: NotificacionEntity)

    @Query("SELECT * FROM notificaciones ORDER BY fecha DESC")
    fun getNotifications(): LiveData<List<NotificacionEntity>>
}
