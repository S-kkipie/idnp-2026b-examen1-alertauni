package com.erns.alertauni.data.di

import android.content.Context
import com.erns.alertauni.data.local.AppDatabase
import com.erns.alertauni.data.local.NotificacionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Una sola instancia de la base de datos para toda la app (UI y servicio FCM)
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    fun provideNotificacionDao(database: AppDatabase): NotificacionDao =
        database.notificacionDao()
}
