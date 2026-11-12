package com.erns.alertauni.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.erns.alertauni.data.model.DraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DraftDao {
    // Todas las consultas filtran por dueño: una cuenta nunca ve borradores de otra
    @Query("SELECT * FROM announcement_drafts WHERE ownerUid = :ownerUid ORDER BY updatedAt DESC, id DESC")
    fun observeByOwner(ownerUid: String): Flow<List<DraftEntity>>

    @Query("SELECT * FROM announcement_drafts WHERE ownerUid = :ownerUid AND id = :id")
    suspend fun getById(ownerUid: String, id: Long): DraftEntity?

    @Query("SELECT * FROM announcement_drafts WHERE id = :id")
    suspend fun getAnyById(id: Long): DraftEntity?

    /** Devuelve el id insertado, o -1 si actualizó una fila existente. */
    @Upsert
    suspend fun upsert(draft: DraftEntity): Long

    @Query("DELETE FROM announcement_drafts WHERE ownerUid = :ownerUid AND id = :id")
    suspend fun delete(ownerUid: String, id: Long): Int
}
