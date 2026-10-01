package com.erns.alertauni.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.erns.alertauni.data.model.NotificacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificacionDao {
    @Insert
    suspend fun insertar(notificacionEntity: NotificacionEntity)

    // Flow: Room vuelve a emitir la lista cada vez que la tabla cambia
    @Query("SELECT * FROM notificaciones ORDER BY fecha DESC, id DESC")
    fun observeNotifications(): Flow<List<NotificacionEntity>>

    @Query("DELETE FROM notificaciones WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM notificaciones")
    suspend fun deleteAll()
}
