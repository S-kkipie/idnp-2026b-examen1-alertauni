package com.erns.alertauni.data.repository

import com.erns.alertauni.data.model.DraftEntity
import kotlinx.coroutines.flow.Flow

/** Borradores locales (Room). Cada operación recibe el dueño para no mezclar cuentas. */
interface DraftRepository {
    fun observeDrafts(ownerUid: String): Flow<List<DraftEntity>>
    suspend fun getDraft(ownerUid: String, id: Long): DraftEntity?

    /** Inserta o actualiza; devuelve el id del borrador. */
    suspend fun saveDraft(draft: DraftEntity): Long
    suspend fun deleteDraft(ownerUid: String, id: Long)
}
