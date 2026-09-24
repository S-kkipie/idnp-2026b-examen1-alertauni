package com.erns.alertauni.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PostRequest(
    val course_catalog_id: String,
    val title: String,
    val content: String,
    val is_private: Boolean,
    val allow_comments: Boolean
)
