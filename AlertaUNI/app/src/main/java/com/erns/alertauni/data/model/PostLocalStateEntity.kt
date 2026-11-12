package com.erns.alertauni.data.model

import androidx.room.Entity

/**
 * Cambios que el backend aún no guarda (prioridad, archivado, comentarios activados) sobre un
 * anuncio publicado. Se guardan en Room por cuenta; null significa "usar el valor del backend".
 */
@Entity(tableName = "post_local_state", primaryKeys = ["ownerUid", "postId"])
data class PostLocalStateEntity(
    val ownerUid: String,
    val postId: Int,
    val priority: String? = null,
    val archived: Boolean = false,
    val commentsEnabled: Boolean? = null,
)
