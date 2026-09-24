package com.erns.alertauni.data.remote

interface AuthTokenProvider {
    suspend fun getAuthToken(): String
}