package com.erns.alertauni.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostPrivateRequest (
@SerialName("course_catalog_id") val courseCatalogId: String,
@SerialName("title") val title: String,
@SerialName("content") val content: String,
@SerialName("receiver_private") val receiverPrivate: String,
@SerialName("allow_comments") val allowComments: Boolean
)