package com.erns.alertauni.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.erns.alertauni.R
import com.erns.alertauni.data.repository.NotificationRepository
import com.erns.alertauni.domain.manager.DataStoreHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MiFirebaseService : FirebaseMessagingService() {

    @Inject
    lateinit var dataStoreHelper: DataStoreHelper

    @Inject
    lateinit var notificationRepository: NotificationRepository

    // Scope ligado al ciclo de vida del servicio; se cancela en onDestroy
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nuevo token: $token")
        serviceScope.launch {
            dataStoreHelper.saveFCMToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val titulo = remoteMessage.notification?.title ?: ""
        val body = remoteMessage.notification?.body ?: ""

        if (body.isNotEmpty()) {
            try {
                val json = JSONObject(body)
                val mensaje = json.getString("mensaje")
                val fechahora = json.getString("fechahora")

                mostrarNotificacion(titulo, mensaje)
                saveNotificationToDatabase(titulo, mensaje, fechahora)

                Log.d("FCM", "Mensaje: $mensaje")
                Log.d("FCM", "FechaHora: $fechahora")
            } catch (e: Exception) {
                Log.e("FCM", "Error parseando JSON de notificación", e)
                mostrarNotificacion(titulo, body)
                // Mensaje en texto plano: también se guarda en la bandeja con la hora local
                val fechaLocal = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date())
                saveNotificationToDatabase(titulo, body, fechaLocal)
            }
        } else {
            Log.d("FCM", "El cuerpo del mensaje está vacío")
        }
    }

    private fun mostrarNotificacion(titulo: String, mensaje: String) {
        val canalId = "moodle_notificaciones"
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Build.VERSION_CODES.O is 26, minSdk is 28, so this is always true but good to keep for clarity 
        // OR we can just remove the check if we want to follow the lint advice strictly.
        // The lint says "SDK_INT is always >= 28".
        val canal = NotificationChannel(
            canalId,
            "Actualizaciones Moodle",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(canal)

        val notificacion = NotificationCompat.Builder(this, canalId)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setSmallIcon(R.drawable.ic_launcher_background) // usa tu ícono
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notificacion)
    }

    private fun saveNotificationToDatabase(titulo: String, mensaje: String, fechaHora: String) {
        // Se reutiliza la instancia única de Room provista por Hilt (DatabaseModule)
        serviceScope.launch {
            notificationRepository.saveNotification(titulo, mensaje, fechaHora)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
