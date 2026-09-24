package com.erns.alertauni.common

import android.util.Log
import com.erns.alertauni.data.model.BackendError
import com.erns.alertauni.data.model.BackendException
import io.github.jan.supabase.exceptions.NotFoundRestException
import io.github.jan.supabase.exceptions.UnauthorizedRestException
import io.github.jan.supabase.functions.FunctionRegion
import io.github.jan.supabase.functions.Functions
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Envoltura segura para invocar Edge Functions de Supabase.
 * Centraliza la captura de errores de autorización y parseo de JSON.
 * suspend inline fun <reified T> Functions.safeInvoke(
 */
suspend inline fun <reified T : Any> Functions.safeInvoke(
    function: String,
    body: T,
    region: FunctionRegion = config.defaultRegion,
    headers: Headers = Headers.Empty
): HttpResponse {
    val response = try {
        this.invoke(function, body, region, headers)
    } catch (e: UnauthorizedRestException) {
        val errorRawText = e.error
        Log.d("UnauthorizedRestException", errorRawText)
        val backendError = try {
            Json.decodeFromString<BackendError>(errorRawText)
        } catch (parseException: SerializationException) {
            Log.e("UnauthorizedRestException", "Failed to parse: $errorRawText", parseException)
            throw Exception("Failed to parse backend error", parseException)
        }
        throw BackendException(backendError)
    } catch (e: NotFoundRestException) {
        val errorRawText = e.error
        Log.d("NotFoundRestException", errorRawText)
        val backendError = try {
            Json.decodeFromString<BackendError>(errorRawText)
        } catch (parseException: SerializationException) {
            Log.e("UnauthorizedRestException", "Failed to parse: $errorRawText", parseException)
            throw Exception("Failed to parse backend error", parseException)
        }
        throw BackendException(backendError)
    }

    return response
}

suspend inline fun Functions.safeInvoke(
    function: String,
    region: FunctionRegion = config.defaultRegion,
    headers: Headers = Headers.Empty
): HttpResponse {
    val response = try {
        this.invoke(function, region, headers)

    } catch (e: UnauthorizedRestException) {
        val errorRawText = e.error
        Log.d("UnauthorizedRestException", errorRawText)
        val backendError = try {
            Json.decodeFromString<BackendError>(errorRawText)
        } catch (parseException: SerializationException) {
            Log.e("UnauthorizedRestException", "Failed to parse: $errorRawText", parseException)
            throw Exception("Failed to parse backend error", parseException)
        }
        throw BackendException(backendError)
    } catch (e: NotFoundRestException) {
        val errorRawText = e.error
        Log.d("NotFoundRestException", errorRawText)
        val backendError = try {
            Json.decodeFromString<BackendError>(errorRawText)
        } catch (parseException: SerializationException) {
            Log.e("UnauthorizedRestException", "Failed to parse: $errorRawText", parseException)
            throw Exception("Failed to parse backend error", parseException)
        }
        throw BackendException(backendError)
    }
    return response
}