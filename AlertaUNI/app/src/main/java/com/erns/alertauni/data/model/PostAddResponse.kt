package com.erns.alertauni.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PostAddResponse(
    val post_id:Int,
    val created_at:String
)
