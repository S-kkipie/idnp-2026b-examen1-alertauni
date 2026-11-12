package com.erns.alertauni.domain.post

import com.erns.alertauni.data.model.PostLocalStateEntity

/**
 * Lo que este dispositivo cambió sobre un anuncio publicado (Room, `post_local_state`).
 * Lo que es null se toma del backend.
 */
data class LocalPostState(
    val priority: Priority? = null,
    val archived: Boolean = false,
    val commentsEnabled: Boolean? = null,
) {
    fun priorityOr(backendCode: String?): Priority = priority ?: Priority.fromCode(backendCode)

    fun commentsOr(backend: Boolean): Boolean = commentsEnabled ?: backend

    companion object {
        val NONE = LocalPostState()

        fun from(entity: PostLocalStateEntity) = LocalPostState(
            priority = entity.priority?.let { Priority.fromCode(it) },
            archived = entity.archived,
            commentsEnabled = entity.commentsEnabled,
        )

        /** Por postId, sólo del dueño indicado. */
        fun byPost(entities: List<PostLocalStateEntity>, owner: String?): Map<Int, LocalPostState> =
            entities.filter { it.ownerUid == owner }.associate { it.postId to from(it) }
    }
}
