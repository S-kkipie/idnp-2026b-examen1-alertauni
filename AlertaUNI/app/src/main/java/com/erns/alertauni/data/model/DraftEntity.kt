package com.erns.alertauni.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Borrador de anuncio guardado sólo en este dispositivo (Room). Nunca se envía al backend
 * hasta que el usuario lo publica. [ownerUid] separa los borradores de cada cuenta.
 */
@Entity(tableName = "announcement_drafts", indices = [Index("ownerUid")])
data class DraftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerUid: String,
    /** COURSE (todo un curso) o STUDENT (privado). */
    val mode: String,
    val courseCatalogId: String?,
    val courseCode: String,
    val recipientStudentId: String? = null,
    val recipientName: String? = null,
    val priority: String,
    val title: String,
    val content: String,
    val allowComments: Boolean,
    /** Milisegundos desde epoch. */
    val updatedAt: Long,
)
