package com.erns.alertauni.data.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

//    // 1. Provee la instancia de AuthService (Retrofit)
//    @Provides
//    @Singleton
//    fun provideAuthApiService(): AuthApiService {
//        // Usa tu factoría para crear el servicio
//        return RetrofitClientFactory.createService<AuthApiService>()
//    }

}