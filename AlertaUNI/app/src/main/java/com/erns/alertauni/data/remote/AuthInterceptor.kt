package com.erns.alertauni.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenProvider: AuthTokenProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = runBlocking {
            tokenProvider.getAuthToken()
        }
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token") // Añade la cabecera de session
            .build()
        return chain.proceed(newRequest)
    }
}