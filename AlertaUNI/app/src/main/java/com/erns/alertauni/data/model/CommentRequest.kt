package com.erns.alertauni.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CommentRequest (
    val post_id: String,
    val content: String,
    val is_private: Boolean
)