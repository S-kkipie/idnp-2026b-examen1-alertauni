package com.erns.alertauni.data.repository

import com.erns.alertauni.data.local.PostLocalStateDao
import com.erns.alertauni.data.model.PostLocalStateEntity
import com.erns.alertauni.domain.post.Priority
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PostLocalStateRepositoryImpl @Inject constructor(
    private val dao: PostLocalStateDao
) : PostLocalStateRepository {

    override fun observe(ownerUid: String): Flow<List<PostLocalStateEntity>> = dao.observeByOwner(ownerUid)

    override suspend fun setArchived(ownerUid: String, postId: Int, archived: Boolean) =
        update(ownerUid, postId) { it.copy(archived = archived) }

    override suspend fun setPriority(ownerUid: String, postId: Int, priority: Priority) =
        update(ownerUid, postId) { it.copy(priority = priority.code) }

    override suspend fun setCommentsEnabled(ownerUid: String, postId: Int, enabled: Boolean) =
        update(ownerUid, postId) { it.copy(commentsEnabled = enabled) }

    private suspend fun update(ownerUid: String, postId: Int, change: (PostLocalStateEntity) -> PostLocalStateEntity) {
        val current = dao.get(ownerUid, postId) ?: PostLocalStateEntity(ownerUid = ownerUid, postId = postId)
        dao.upsert(change(current))
    }
}
