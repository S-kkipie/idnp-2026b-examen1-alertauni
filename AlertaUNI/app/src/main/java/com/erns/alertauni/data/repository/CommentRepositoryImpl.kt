package com.erns.alertauni.data.repository

import android.util.Log
import com.erns.alertauni.data.model.CommentDto
import com.erns.alertauni.data.model.CommentEntity
import com.erns.alertauni.data.model.CommentRequest
import com.erns.alertauni.data.remote.CommentDataSource
import com.erns.alertauni.domain.manager.DataStoreHelper
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val commentDataSource: CommentDataSource,
    private val dataStoreHelper: DataStoreHelper,
) : CommentRepository {
    override suspend fun sendComment(
        commentRequest: CommentRequest
    ): Result<Boolean> {
        return try {
            val success = commentDataSource.callInsertEndpoint(commentRequest)
            if (success)
                Result.success(success)
            else
                Result.failure(Exception("Failed to insert record via Edge Function"))
        } catch (e: Exception) {
            Log.e("CommentRepositoryImpl", "Error sending comment: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getComments(commentDto: CommentDto): Result<List<CommentEntity>> {
        return try {
            val success = commentDataSource.callCommentsEndpoint(commentDto)
            if (success.data.isNotEmpty()) {
                val comments = success.data.map { response ->
                    val isMine = response.email == dataStoreHelper.getEmail()
                    CommentEntity(
                        id = response.id,
                        postId = response.post_id,
                        authorName = response.author_name,
                        content = response.content,
                        email = response.email,
                        isMine = isMine,
                        publishedDate = response.published_date
                    )
                }
                Result.success(comments)
            } else {
                Result.failure(Exception("No comments found"))
            }
        } catch (e: Exception) {
            Log.e("CommentRepositoryImpl", "Error fetching comments: ${e.message}")
            Result.failure(e)
        }
    }
}