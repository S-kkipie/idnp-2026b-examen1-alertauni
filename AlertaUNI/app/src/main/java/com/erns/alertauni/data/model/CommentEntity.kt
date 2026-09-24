package com.erns.alertauni.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CommentEntity (
    val id: Int,
    val postId: Int,
    val authorName: String,
    val content: String,
    val email: String,
    val isMine: Boolean,
    val publishedDate: String
)