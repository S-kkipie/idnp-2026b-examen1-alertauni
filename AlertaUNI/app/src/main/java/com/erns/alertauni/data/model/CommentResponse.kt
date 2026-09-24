package com.erns.alertauni.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CommentResponse (
    val id: Int,
    val post_id: Int,
    val author_name: String,
    val content: String,
    val email: String,
    val published_date: String
)
