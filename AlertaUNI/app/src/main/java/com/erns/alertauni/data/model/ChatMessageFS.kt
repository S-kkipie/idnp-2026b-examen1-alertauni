package com.erns.alertauni.data.model

data class ChatMessageFS (
    val id: String = "",
    val topicId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val status: String = ""
)
