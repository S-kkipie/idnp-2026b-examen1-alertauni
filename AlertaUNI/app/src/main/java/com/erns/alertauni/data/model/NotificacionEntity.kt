package com.erns.alertauni.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notificaciones")
data class NotificacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cursoId: String,
    val mensaje: String,
    val fecha: String
)