package com.erns.alertauni.data.repository

import com.erns.alertauni.data.model.PostLocalStateEntity
import com.erns.alertauni.domain.post.Priority
import kotlinx.coroutines.flow.Flow

/** Prioridad, archivado y comentarios de anuncios publicados, guardados sólo en este dispositivo. */
interface PostLocalStateRepository {
    fun observe(ownerUid: String): Flow<List<PostLocalStateEntity>>
    suspend fun setArchived(ownerUid: String, postId: Int, archived: Boolean)
    suspend fun setPriority(ownerUid: String, postId: Int, priority: Priority)
    suspend fun setCommentsEnabled(ownerUid: String, postId: Int, enabled: Boolean)
}
