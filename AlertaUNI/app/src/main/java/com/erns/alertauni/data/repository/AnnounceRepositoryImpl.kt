package com.erns.alertauni.data.repository

import com.erns.alertauni.data.model.PostPrivateRequest
import com.erns.alertauni.data.model.PostPrivateResponse
import com.erns.alertauni.data.model.PostRequest
import com.erns.alertauni.data.remote.AnnounceDataSource
import javax.inject.Inject

class AnnounceRepositoryImpl @Inject constructor
    (
    private val announceDataSource: AnnounceDataSource
) : AnnounceRepository {
    override suspend fun addPostPrivate(postPrivateRequest: PostPrivateRequest): Result<PostPrivateResponse> {
        return try {
            val success = announceDataSource.addPostPrivate(postPrivateRequest)
            Result.success(success.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addPostPublic(postRequest: PostRequest): Result<PostPrivateResponse> {
        return try {
            val success = announceDataSource.addPostPublic(postRequest)
            Result.success(success.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}