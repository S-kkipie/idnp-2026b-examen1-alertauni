package com.erns.alertauni.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.erns.alertauni.data.model.NotificacionEntity

@Database(entities = [NotificacionEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificacionDao(): NotificacionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notificaciones-db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
