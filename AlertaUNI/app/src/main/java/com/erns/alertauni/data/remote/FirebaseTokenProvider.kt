package com.erns.alertauni.data.remote

import com.google.firebase.auth.FirebaseAuth
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseTokenProvider : AuthTokenProvider {
    override suspend fun getAuthToken(): String {
        return suspendCoroutine { continuation ->
            FirebaseAuth.getInstance().currentUser
                ?.getIdToken(true)
                ?.addOnSuccessListener { result ->
                    continuation.resume(result.token ?: "")
                }
                ?.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
                ?: continuation.resumeWithException(IllegalStateException("Usuario no autenticado"))
        }
    }
}