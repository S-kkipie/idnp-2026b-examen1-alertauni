package com.erns.alertauni.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.erns.alertauni.data.model.DraftEntity
import com.erns.alertauni.data.model.NotificacionEntity
import com.erns.alertauni.data.model.PostLocalStateEntity

@Database(
    entities = [NotificacionEntity::class, DraftEntity::class, PostLocalStateEntity::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificacionDao(): NotificacionDao
    abstract fun draftDao(): DraftDao
    abstract fun postLocalStateDao(): PostLocalStateDao

    companion object {
        const val NAME = "notificaciones-db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = build(context, NAME)
                INSTANCE = instance
                instance
            }
        }

        // Migración real: no se usa fallbackToDestructiveMigration para no borrar las notificaciones
        fun build(context: Context, name: String): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, name)
                .addMigrations(MIGRATION_2_3)
                .build()
    }
}
