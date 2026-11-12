package com.erns.alertauni.screen.post

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.erns.alertauni.data.demo.DemoAnnouncements
import com.erns.alertauni.data.model.DraftEntity
import com.erns.alertauni.domain.post.AnnouncementQuery
import com.erns.alertauni.domain.post.Audience
import com.erns.alertauni.domain.post.DraftMapper
import com.erns.alertauni.domain.post.DraftForm
import com.erns.alertauni.domain.post.LocalPostState
import com.erns.alertauni.domain.post.Priority
import com.erns.alertauni.domain.post.RecipientMode
import com.erns.alertauni.domain.post.TypeFilter
import com.erns.alertauni.ui.theme.AlertaUNITheme
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.WindowManager
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.InternalRoborazziApi
import com.github.takahirom.roborazzi.fetchRobolectricWindowRoots
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.captureScreenRoboImage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Capturas de los composables reales (PostContent, AnnouncementCard, DraftCard, NewAnnouncementSheet)
 * con los datos simulados, en un teléfono de 360x780 dp. Sin backend: el estado se arma con
 * PostUiStateBuilder, igual que en el ViewModel.
 * Grabar: ./gradlew :app:testDebugUnitTest -PgrabarCapturas
 */
@OptIn(ExperimentalRoborazziApi::class, ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h780dp-xhdpi")
class PostScreenshotsTest {

    @get:Rule
    val compose = createComposeRule()

    private val now: ZonedDateTime = ZonedDateTime.of(2026, 11, 12, 10, 0, 0, 0, ZoneId.of("America/Lima"))
    private val dir = System.getProperty("capturas.dir") ?: "build/capturas"

    private fun millisAgo(minutes: Long) = now.toInstant().minus(Duration.ofMinutes(minutes)).toEpochMilli()

    private val courses = DemoAnnouncements.courses
    private val students = DemoAnnouncements.students.map { s ->
        val course = courses.first { it.courseCode == s.courseCode }
        StudentOption(s.id, s.code, s.fullName, s.shortName, course.id, course.courseCode)
    }

    private val privateDraft = DraftForm(
        id = 1, mode = RecipientMode.STUDENT, courseCatalogId = "demo-inf-02", courseCode = "INF-02",
        recipientStudentId = "demo-stu-1", recipientName = "Lucía Quispe", priority = Priority.ALTA,
        title = "Recordatorio de sustentación",
        content = "Tu sustentación es el jueves a las 10:00 en el aula 205. Trae el informe impreso y la demo instalada.",
    )

    private val drafts: List<DraftEntity> = listOf(
        DraftMapper.toEntity(privateDraft, "uid-docente", millisAgo(12)),
        DraftMapper.toEntity(
            DraftForm(
                id = 2, courseCatalogId = "demo-inf-01", courseCode = "INF-01", title = "Práctica 4: enunciado",
                content = "Diseñen el diagrama de clases del módulo de reservas y justifiquen cada asociación."
            ),
            "uid-docente",
            now.minusDays(1).withHour(19).withMinute(40).toInstant().toEpochMilli()
        ),
        DraftMapper.toEntity(
            DraftForm(
                id = 3, courseCatalogId = "demo-inf-03", courseCode = "INF-03", priority = Priority.BAJA,
                content = "Ideas para el laboratorio de índices: comparar tiempos con y sin índice en la tabla de ventas."
            ),
            "uid-docente",
            now.minusDays(7).toInstant().toEpochMilli()
        ),
        // De otra cuenta: no debe aparecer
        DraftMapper.toEntity(DraftForm(id = 4, title = "Borrador de otro docente"), "uid-otro", millisAgo(1)),
    )

    private val sources = WallSources(
        catalogCourseCodes = courses.map { it.courseCode },
        isTeacher = true,
        owner = "uid-docente",
        drafts = drafts,
        localStates = listOf(-1004, -1011, -1018).associateWith { LocalPostState(archived = true) },
        demoEnabled = true,
        demoPosts = DemoAnnouncements.posts(now),
    )

    private fun state(view: WallViewOptions = WallViewOptions()) = PostUiStateBuilder.build(sources, view, now)

    private fun show(content: @Composable () -> Unit) =
        compose.setContent { AlertaUNITheme(dynamicColor = false, darkTheme = false) { content() } }

    private fun wall(state: PostUiState) = show { PostContent(state = state, actions = PostActions()) }

    private fun capture(name: String) = compose.onRoot().captureRoboImage("$dir/$name")

    private fun captureScreen(name: String) {
        compose.waitForIdle()
        captureScreenRoboImage("$dir/$name")
    }

    /**
     * Como captureScreenRoboImage, pero respeta el orden en que se abrieron las ventanas del mismo tipo:
     * Roborazzi dibuja el diálogo de descarte debajo de la hoja, y en el teléfono queda encima.
     */
    @OptIn(InternalRoborazziApi::class)
    private fun captureWindows(name: String) {
        compose.waitForIdle()
        val roots = fetchRobolectricWindowRoots()
        // Dentro de un mismo tipo, Espresso lista primero la ventana más nueva
        val ordered = roots.groupBy { it.windowLayoutParams.get()?.type ?: 0 }.toSortedMap().values
            .flatMap { it.reversed() }
        val screen = ordered.first().decorView
        val bitmap = Bitmap.createBitmap(screen.width, screen.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        ordered.forEach { root ->
            val view = root.decorView
            val params = root.windowLayoutParams.get()
            if (params != null && params.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND != 0) {
                canvas.drawColor(android.graphics.Color.argb(82, 0, 0, 0))
            }
            canvas.save()
            canvas.translate((screen.width - view.width) / 2f, (screen.height - view.height) / 2f)
            view.draw(canvas)
            canvas.restore()
        }
        bitmap.captureRoboImage("$dir/$name")
    }

    private fun sheet(initial: DraftForm? = null) = show {
        Box {
            PostContent(state = state(), actions = PostActions())
            NewAnnouncementSheet(
                courses = courses,
                students = students,
                canSendPrivate = true,
                audienceFor = { id, recipient ->
                    val code = courses.first { it.id == id }.courseCode
                    Audience(code, students.count { it.courseCatalogId == id }, recipient?.shortName)
                },
                onSend = {},
                onDismiss = {},
                initial = initial,
                onSaveDraft = {}
            )
        }
    }

    // El buscador del muro sigue detrás de la hoja: los campos se eligen por su etiqueta
    private fun field(label: String) = compose.onNode(hasSetTextAction() and hasText(label))

    private fun writePrivate() {
        compose.onNodeWithText("Un estudiante").performClick()
        field("Buscar estudiante por nombre o CUI").performTextInput("lucia")
        compose.onNodeWithText("Lucía Quispe Mamani").performClick()
        compose.onNode(hasText("Alta") and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
            .performClick()
        field("Título").performTextInput("Falta tu informe de laboratorio")
        field("Contenido")
            .performTextInput("No encuentro tu informe del laboratorio 4. Súbelo hasta mañana para que no tenga descuento.")
    }

    @Test
    fun muro_con_40_anuncios() {
        val state = state()
        assertEquals(DemoAnnouncements.count, state.demoCount)
        assertEquals(3, state.draftCount)
        wall(state)
        compose.onNodeWithText("Borradores (3)").assertExists()
        capture("01-muro-40-anuncios.png")
    }

    @Test
    fun busqueda() {
        wall(state(WallViewOptions(query = AnnouncementQuery(text = "examen"))))
        capture("02-busqueda.png")
    }

    @Test
    fun sin_resultados() {
        wall(state(WallViewOptions(query = AnnouncementQuery(text = "beca alimentaria"))))
        compose.onNodeWithText("Limpiar búsqueda y filtros").assertExists()
        capture("03-sin-resultados.png")
    }

    @Test
    fun filtro_privados() {
        wall(state(WallViewOptions(query = AnnouncementQuery(type = TypeFilter.PRIVATE))))
        capture("04-filtro-privados.png")
    }

    @Test
    fun nuevo_privado() {
        sheet()
        writePrivate()
        compose.onNodeWithText("Enviar sólo a Lucía Quispe").assertExists()
        captureScreen("05-nuevo-privado.png")
    }

    @Test
    fun guardar_borrador() {
        // A medio escribir: aún no se puede enviar, pero sí guardar como borrador
        sheet()
        field("Título").performTextInput("Salida de campo al parque industrial")
        compose.onNodeWithText("Elige un curso").assertIsNotEnabled()
        compose.onNodeWithText("Guardar borrador").performScrollTo().assertIsEnabled()
        captureScreen("06-guardar-borrador.png")
    }

    @Test
    fun cerrar_con_cambios_ofrece_guardar_borrador() {
        sheet()
        writePrivate()
        compose.onNodeWithContentDescription("Cerrar").performClick()
        compose.onNodeWithText("¿Guardar como borrador?").assertExists()
        captureWindows("06b-cerrar-con-cambios.png")
    }

    @Test
    fun lista_borradores() {
        wall(state(WallViewOptions(wallView = WallView.DRAFTS)))
        compose.onNodeWithText("Borrador de otro docente").assertDoesNotExist()
        capture("07-lista-borradores.png")
    }

    @Test
    fun editar_borrador() {
        sheet(initial = privateDraft)
        compose.onNodeWithText("Editar borrador").assertExists()
        compose.onNodeWithText("Lucía Quispe · INF-02").assertExists()
        captureScreen("08-editar-borrador.png")
    }

    @Test
    fun menu_datos_de_prueba() {
        wall(state())
        compose.onNodeWithContentDescription("Más opciones").performClick()
        compose.onNodeWithText("Datos de prueba (simulados)").assertExists()
        captureScreen("09-datos-de-prueba-menu.png")
    }

    @Test
    fun archivados() {
        val state = state(WallViewOptions(wallView = WallView.ARCHIVED))
        assertEquals(3, state.totalInView)
        wall(state)
        capture("10-archivados.png")
    }

    @Test
    fun origen_de_los_datos() {
        wall(state())
        compose.onNodeWithContentDescription("Más opciones").performClick()
        compose.onNodeWithText("Origen de los datos").performClick()
        compose.onAllNodesWithText("Este dispositivo (Room)").fetchSemanticsNodes().let { assertEquals(1, it.size) }
        captureScreen("11-origen-de-los-datos.png")
    }
}
