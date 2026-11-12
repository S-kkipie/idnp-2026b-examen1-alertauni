package com.erns.alertauni.data.repository

import com.erns.alertauni.data.local.DraftDao
import com.erns.alertauni.data.model.DraftEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DraftRepositoryImpl @Inject constructor(
    private val draftDao: DraftDao
) : DraftRepository {

    override fun observeDrafts(ownerUid: String): Flow<List<DraftEntity>> =
        draftDao.observeByOwner(ownerUid)

    override suspend fun getDraft(ownerUid: String, id: Long): DraftEntity? =
        draftDao.getById(ownerUid, id)

    override suspend fun saveDraft(draft: DraftEntity): Long {
        require(draft.ownerUid.isNotBlank()) { "Un borrador necesita dueño" }
        // Al editar, no se puede pisar el borrador de otra cuenta con el mismo id
        if (draft.id != 0L) {
            val existing = draftDao.getById(draft.ownerUid, draft.id)
            if (existing == null && draftDao.getAnyById(draft.id) != null) {
                return draftDao.upsert(draft.copy(id = 0))
            }
        }
        val rowId = draftDao.upsert(draft)
        return if (rowId == -1L) draft.id else rowId
    }

    override suspend fun deleteDraft(ownerUid: String, id: Long) {
        draftDao.delete(ownerUid, id)
    }
}
