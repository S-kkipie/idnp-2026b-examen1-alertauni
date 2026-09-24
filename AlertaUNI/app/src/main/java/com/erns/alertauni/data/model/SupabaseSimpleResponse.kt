package com.erns.alertauni.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SupabaseSimpleResponse<T>(
    val data: T
)
