package com.erns.alertauni.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CourseCatalogEntity(
    val id:String,
    val code: String,
    val name: String
)
