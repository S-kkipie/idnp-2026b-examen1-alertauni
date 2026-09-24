package com.erns.alertauni.data.di

import android.app.Application
import android.content.Context
import com.erns.alertauni.domain.manager.DataStoreHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Provee la instancia de DataStoreHelper
    @Provides
    @Singleton
    fun provideTokenManager(
        // Hilt usa @ApplicationContext para darte el contexto
        @ApplicationContext context: Context
    ): DataStoreHelper {
        // Debemos convertir Context a Application si DataStoreHelper lo requiere
        return DataStoreHelper(context.applicationContext as Application)
    }

/*
    @Provides
    @Singleton
    fun provideOneTapClient(@ApplicationContext context: Context): SignInClient {
        // Hilt inyecta el Contexto de la Aplicación aquí.
        return Identity.getSignInClient(context)
    }
*/
}