package com.erns.alertauni.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SupabaseListResponse<T>(
    val data: List<T>
)