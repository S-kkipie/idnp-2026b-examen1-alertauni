package com.erns.alertauni.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.erns.alertauni.data.model.PostLocalStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostLocalStateDao {
    @Query("SELECT * FROM post_local_state WHERE ownerUid = :ownerUid")
    fun observeByOwner(ownerUid: String): Flow<List<PostLocalStateEntity>>

    @Query("SELECT * FROM post_local_state WHERE ownerUid = :ownerUid AND postId = :postId")
    suspend fun get(ownerUid: String, postId: Int): PostLocalStateEntity?

    @Upsert
    suspend fun upsert(state: PostLocalStateEntity)
}
