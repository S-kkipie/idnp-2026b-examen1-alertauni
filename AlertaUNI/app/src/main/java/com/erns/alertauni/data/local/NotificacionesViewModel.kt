package com.erns.alertauni.data.local

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.erns.alertauni.data.model.NotificacionEntity

class NotificacionesViewModel(application: Application) : AndroidViewModel(application) {
    val notificaciones: LiveData<List<NotificacionEntity>>

    init {
        val dao = AppDatabase.getInstance(application).notificacionDao()
        notificaciones = dao.getNotifications()
    }
}
