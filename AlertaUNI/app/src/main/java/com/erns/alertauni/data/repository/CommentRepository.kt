package com.erns.alertauni.data.repository

import com.erns.alertauni.data.model.CommentDto
import com.erns.alertauni.data.model.CommentEntity
import com.erns.alertauni.data.model.CommentRequest
import com.erns.alertauni.data.model.CommentResponse

interface CommentRepository {
    suspend fun sendComment(commentRequest: CommentRequest): Result<Boolean>
    suspend fun getComments(commentDto: CommentDto): Result<List<CommentEntity>>
}