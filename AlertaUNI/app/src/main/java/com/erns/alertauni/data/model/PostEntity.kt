package com.erns.alertauni.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class PostEntity(
    @SerialName("post_id") val id: Int,
    @SerialName("author_name") val authorName: String? = "",
    @SerialName("author_type") val authorType: Int,
    @SerialName("course_code") val courseCode: String,
    @SerialName("course_name") val courseName: String? = "",
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("allow_comments") val allowComments: Boolean,
    @SerialName("count_comments") val countComments: Int,
    @SerialName("is_private") val isPrivate: Boolean,
    @SerialName("published") val date: String,
    @SerialName("receiver_private") val receiverPrivate: String?=""
)