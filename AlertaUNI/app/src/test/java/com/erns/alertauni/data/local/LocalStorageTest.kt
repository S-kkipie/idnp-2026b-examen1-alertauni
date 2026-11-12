package com.erns.alertauni.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.erns.alertauni.data.model.DraftEntity
import com.erns.alertauni.data.repository.DraftRepositoryImpl
import com.erns.alertauni.data.repository.PostLocalStateRepositoryImpl
import com.erns.alertauni.domain.post.Priority
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Room real (SQLite de Robolectric): borradores por cuenta, persistencia y migración 2 -> 3. */
@RunWith(RobolectricTestRunner::class)
class LocalStorageTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbName = "local-storage-test.db"

    @After
    fun cleanUp() {
        context.deleteDatabase(dbName)
    }

    private fun draft(owner: String, title: String, updatedAt: Long, id: Long = 0) = DraftEntity(
        id = id, ownerUid = owner, mode = "COURSE", courseCatalogId = "c1", courseCode = "INF-01",
        priority = "MEDIA", title = title, content = "", allowComments = true, updatedAt = updatedAt
    )

    @Test
    fun `cada cuenta ve solo sus borradores, del mas reciente al mas antiguo`() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        val repo = DraftRepositoryImpl(db.draftDao())
        repo.saveDraft(draft("uid-docente-a", "Viejo", updatedAt = 1_000))
        repo.saveDraft(draft("uid-docente-a", "Nuevo", updatedAt = 2_000))
        val ajeno = repo.saveDraft(draft("uid-docente-b", "De otra cuenta", updatedAt = 3_000))

        assertEquals(listOf("Nuevo", "Viejo"), repo.observeDrafts("uid-docente-a").first().map { it.title })
        assertEquals(listOf("De otra cuenta"), repo.observeDrafts("uid-docente-b").first().map { it.title })
        // Ni por id se puede leer o borrar el borrador de otra cuenta
        assertNull(repo.getDraft("uid-docente-a", ajeno))
        repo.deleteDraft("uid-docente-a", ajeno)
        assertEquals(1, repo.observeDrafts("uid-docente-b").first().size)
        db.close()
    }

    @Test
    fun `guardar de nuevo actualiza el mismo borrador y restaurar conserva el id`() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        val repo = DraftRepositoryImpl(db.draftDao())
        val id = repo.saveDraft(draft("uid-a", "Primera versión", updatedAt = 1_000))
        assertEquals(id, repo.saveDraft(draft("uid-a", "Segunda versión", updatedAt = 2_000, id = id)))
        assertEquals(listOf("Segunda versión"), repo.observeDrafts("uid-a").first().map { it.title })

        val saved = repo.getDraft("uid-a", id)!!
        repo.deleteDraft("uid-a", id)
        assertTrue(repo.observeDrafts("uid-a").first().isEmpty())
        assertEquals(id, repo.saveDraft(saved)) // "Deshacer"
        assertEquals(saved, repo.getDraft("uid-a", id))
        db.close()
    }

    @Test
    fun `un id de otra cuenta no pisa su borrador`() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        val repo = DraftRepositoryImpl(db.draftDao())
        val idB = repo.saveDraft(draft("uid-b", "De B", updatedAt = 1_000))
        val idA = repo.saveDraft(draft("uid-a", "De A", updatedAt = 2_000, id = idB))
        assertTrue(idA != idB)
        assertEquals("De B", repo.getDraft("uid-b", idB)!!.title)
        assertEquals("De A", repo.getDraft("uid-a", idA)!!.title)
        db.close()
    }

    @Test
    fun `los borradores y el estado local siguen despues de cerrar y abrir la app`() = runBlocking {
        val first = AppDatabase.build(context, dbName)
        DraftRepositoryImpl(first.draftDao()).saveDraft(draft("uid-a", "Persistente", updatedAt = 5_000))
        PostLocalStateRepositoryImpl(first.postLocalStateDao()).run {
            setArchived("uid-a", postId = 7, archived = true)
            setPriority("uid-a", postId = 7, priority = Priority.ALTA)
            setCommentsEnabled("uid-a", postId = 8, enabled = false)
        }
        first.close()

        val reopened = AppDatabase.build(context, dbName)
        assertEquals(listOf("Persistente"), reopened.draftDao().observeByOwner("uid-a").first().map { it.title })
        val states = reopened.postLocalStateDao().observeByOwner("uid-a").first().associateBy { it.postId }
        assertEquals(true, states.getValue(7).archived)
        assertEquals("ALTA", states.getValue(7).priority)
        assertNull(states.getValue(7).commentsEnabled)
        assertEquals(false, states.getValue(8).commentsEnabled)
        assertTrue(reopened.postLocalStateDao().observeByOwner("uid-b").first().isEmpty())
        reopened.close()
    }

    @Test
    fun `migrar de la version 2 conserva las notificaciones`() = runBlocking {
        // Base tal como la dejaba la versión 2 de la app
        val file = context.getDatabasePath(dbName).apply { parentFile?.mkdirs() }
        SQLiteDatabase.openOrCreateDatabase(file, null).use { old ->
            old.execSQL(
                "CREATE TABLE IF NOT EXISTS `notificaciones` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`cursoId` TEXT NOT NULL, `mensaje` TEXT NOT NULL, `fecha` TEXT NOT NULL)"
            )
            old.execSQL("INSERT INTO notificaciones (cursoId, mensaje, fecha) VALUES ('INF-01', 'Examen el lunes', '2026-11-10')")
            old.version = 2
        }

        val migrated = AppDatabase.build(context, dbName)
        assertEquals(listOf("Examen el lunes"), migrated.notificacionDao().observeNotifications().first().map { it.mensaje })
        val id = DraftRepositoryImpl(migrated.draftDao()).saveDraft(draft("uid-a", "Tras migrar", updatedAt = 1))
        assertEquals("Tras migrar", migrated.draftDao().getById("uid-a", id)!!.title)
        migrated.close()
    }
}
