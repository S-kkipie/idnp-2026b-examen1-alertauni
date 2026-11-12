package com.erns.alertauni.data.demo

import com.erns.alertauni.data.model.CatalogCourseEntity
import com.erns.alertauni.data.model.PostEntity
import com.erns.alertauni.domain.post.Priority
import java.time.Duration
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * DATOS SIMULADOS para pruebas funcionales (T4A). No vienen del backend ni se envían a él:
 * sus ids son negativos y la app los marca con «Simulado». Siempre generan lo mismo para la
 * misma fecha [now], con fechas relativas a ella (hoy, ayer y unos dos meses atrás).
 */
object DemoAnnouncements {

    data class DemoStudent(
        val id: String,
        val code: String,
        val firstname: String,
        val surname: String,
        val courseCode: String,
    ) {
        val fullName: String get() = "$firstname $surname"
        val shortName: String get() = "${firstname.substringBefore(' ')} ${surname.substringBefore(' ')}"
    }

    private data class Seed(
        val title: String,
        val content: String,
        val course: Int,
        val priority: Priority,
        val recipient: Int? = null,
        val closed: Boolean = false,
    )

    private const val ID_BASE = -1000

    val courses: List<CatalogCourseEntity> = listOf(
        CatalogCourseEntity("demo-inf-01", "INF-01", "Ingeniería de Software"),
        CatalogCourseEntity("demo-inf-02", "INF-02", "Programación Web"),
        CatalogCourseEntity("demo-inf-03", "INF-03", "Bases de Datos"),
        CatalogCourseEntity("demo-inf-04", "INF-04", "Interacción Humano-Computador"),
    )

    val students: List<DemoStudent> = listOf(
        DemoStudent("demo-stu-1", "20230101", "Lucía", "Quispe Mamani", "INF-02"),
        DemoStudent("demo-stu-2", "20230102", "Diego", "Huamán Torres", "INF-01"),
        DemoStudent("demo-stu-3", "20230103", "Valeria", "Condori Apaza", "INF-03"),
        DemoStudent("demo-stu-4", "20230104", "Mateo", "Flores Ramos", "INF-04"),
        DemoStudent("demo-stu-5", "20230105", "Camila", "Ccama Vargas", "INF-01"),
        DemoStudent("demo-stu-6", "20230106", "Sebastián", "Choque Ríos", "INF-02"),
        DemoStudent("demo-stu-7", "20230107", "Ana Lucía", "Paredes Soto", "INF-03"),
        DemoStudent("demo-stu-8", "20230108", "José", "Mamani Cruz", "INF-04"),
    )

    /** Nombre corto por id de estudiante, para mostrar «Para: Lucía Quispe». */
    val studentNames: Map<String, String> = students.associate { it.id to it.shortName }

    private val A = Priority.ALTA
    private val M = Priority.MEDIA
    private val B = Priority.BAJA

    // Índice 0 = el más reciente. course: 0..3 = INF-01..INF-04; recipient: 1..8 = students
    private val seeds = listOf(
        Seed("Examen parcial reprogramado", "La evaluación de la unidad 2 pasa al lunes 8 en el aula 304. Traigan calculadora y su carné universitario.", 0, A),
        Seed("Sobre tu trabajo final", "Revisa las observaciones de la evaluación antes del viernes. Si tienes dudas, escríbeme por aquí.", 1, M, recipient = 1),
        Seed("Aula 304", "Hoy la clase es en el aula 304.", 2, B),
        Seed("Laboratorio cancelado por corte de luz", "No habrá laboratorio esta tarde. Lo recuperamos el jueves en el mismo horario.", 3, A, closed = true),
        Seed("Notas", "Ya están publicadas.", 0, M),
        Seed("Entrega del sprint 3", "El repositorio debe tener el tablero actualizado, las historias cerradas y el informe de retrospectiva en la carpeta Docs. La revisión será en clase con todo el equipo presente.", 0, M),
        Seed("Tu justificación fue aceptada", "Registré tu inasistencia del martes como justificada. No necesitas traer otro documento.", 2, B, recipient = 3),
        Seed("Importante: la entrega del proyecto integrador se amplía hasta el domingo", "Por el mantenimiento del aula virtual se amplía el plazo. No habrá otra ampliación: suban el PDF y el enlace al repositorio en la misma tarea.", 1, A),
        Seed("Material de lectura de la semana 9", "Subí las diapositivas y dos artículos sobre normalización. Léanlos antes de la práctica del miércoles.", 2, B),
        Seed("Prueba de usabilidad con usuarios", "Cada grupo debe traer 3 participantes externos. Usen el guion de tareas que revisamos y registren tiempo, errores y la escala SUS.", 3, M),
        Seed("Cambio de horario", "Desde la próxima semana la clase empieza a las 7:00.", 1, A),
        Seed("Falta tu informe de laboratorio", "No encuentro tu informe del laboratorio 4 en la plataforma. Tienes hasta mañana para subirlo sin descuento.", 0, A, recipient = 2),
        Seed("Consultas", "Atenderé consultas el jueves de 15:00 a 17:00 en la oficina 210.", 2, B, closed = true),
        Seed("Examen de recuperación", "Quienes tengan nota menor a 10 en el primer parcial pueden rendir la recuperación el sábado a las 9:00. Inscríbanse con el delegado hasta el miércoles.", 2, A),
        Seed("Proyecto: formato del informe", "Usen la plantilla del curso. Máximo 10 páginas sin contar anexos, letra de 11 puntos y referencias en formato APA. Incluyan el anexo de uso de IA con las preguntas principales que hicieron y qué verificaron ustedes.", 1, M),
        Seed("Felicitaciones", "Tu prototipo fue el mejor evaluado del curso. ¿Te gustaría presentarlo en la feria de proyectos?", 3, B, recipient = 4),
        Seed("Práctica calificada 3", "Será individual y sin apuntes. Temas: consultas con JOIN, subconsultas y vistas.", 2, M),
        Seed("Sin clase hoy", "Por el feriado regional no hay clase. Avancen con la lectura.", 0, B, closed = true),
        Seed("Exposiciones finales: orden de presentación", "El orden se sorteó en clase y está en el aula virtual. Cada grupo tiene 12 minutos de exposición y 5 de preguntas. Si un integrante no se presenta, su nota será cero salvo justificación.", 3, M),
        Seed("Reunión de avance", "Quiero revisar contigo el avance del módulo de pagos antes de la entrega. ¿Puedes el lunes a las 11?", 0, M, recipient = 5),
        Seed("Encuesta docente", "Completen la encuesta de la oficina de calidad antes del cierre del semestre. Es anónima y toma cinco minutos.", 1, B),
        Seed("Tarea 5: API REST", "Implementen los endpoints de la guía con validación de datos y pruebas en Postman. Entrega por el repositorio del grupo.", 1, M),
        Seed("Copia detectada en la tarea 4", "Tu entrega coincide en más de un 80% con la de otro grupo. Necesito que vengas a mi oficina para conversarlo.", 1, A, recipient = 6),
        Seed("Visita a la empresa", "El viernes visitamos el área de sistemas de una empresa local. Salida a las 8:00 desde la puerta principal; traigan su carné.", 3, M),
        Seed("Guía del laboratorio 6", "Ya está disponible la guía. Lean la sección de índices antes de venir.", 2, B),
        Seed("Nota de la práctica 2 corregida", "Revisé de nuevo tu práctica y subí la nota a 16. Ya aparece en el sistema.", 2, M, recipient = 7),
        Seed("Lista de grupos", "Revisen que su nombre aparezca en un grupo. Quien no tenga grupo debe avisarme hoy.", 3, A),
        Seed("Bibliografía", "Pressman, capítulos 4 al 6.", 0, B),
        Seed("Simulacro de examen", "El martes haremos un simulacro de 30 minutos con preguntas parecidas a las del parcial. No cuenta para la nota, pero sirve para practicar el tiempo.", 0, M),
        Seed("Tu exposición", "Buen trabajo en la exposición. Mejora el contraste de las diapositivas y practica el tiempo: te pasaste 4 minutos.", 3, B, recipient = 8),
        Seed("Plazo para elegir tema del proyecto", "Hasta el viernes deben registrar el tema y los integrantes en el formulario. Después de esa fecha asignaré los temas que queden libres.", 1, A),
        Seed("Clase grabada", "Subí la grabación de la clase del lunes.", 2, B, closed = true),
        Seed("Evaluación de prototipos en papel", "Traigan sus prototipos en papel y marcadores. Haremos pruebas cruzadas entre grupos y cada uno anotará los problemas encontrados en la hoja de observaciones.", 3, M),
        Seed("Acceso al servidor de pruebas", "Te creé un usuario en el servidor de pruebas. Las credenciales te llegan al correo institucional.", 1, B, recipient = 1),
        Seed("Bienvenida al curso", "Bienvenidos a Ingeniería de Software. En el aula virtual están el sílabo, el cronograma y las reglas de evaluación. Lean el sílabo completo antes de la segunda clase: allí están las fechas de los exámenes, el peso de cada entrega y la política de atrasos. Durante el semestre usaremos este muro para los avisos oficiales: activen las notificaciones de la app y revisen los anuncios de prioridad alta el mismo día. Las consultas personales escríbanlas como comentario privado.", 0, M),
        Seed("Instalación de herramientas", "Para la próxima clase instalen PostgreSQL 16 y DBeaver.", 2, M),
        Seed("Cronograma actualizado", "Moví la práctica 1 una semana por el congreso. El resto del cronograma no cambia.", 1, M),
        Seed("Inscripción a laboratorios", "Inscríbanse en un solo turno de laboratorio. Si el turno está lleno, escojan otro.", 3, A),
        Seed("Repositorio del curso", "Clonen el repositorio con los ejemplos de clase y verifiquen que compila antes del laboratorio.", 1, B),
        Seed("Retraso en tu matrícula", "Aún no apareces en la lista oficial. Regulariza tu matrícula en la oficina de registro esta semana.", 0, A, recipient = 2),
        Seed("Primer examen: temario", "Unidades 1 y 2: requisitos, historias de usuario y estimación. Revisen los ejercicios resueltos de las prácticas.", 0, M),
        Seed("Normas de laboratorio", "No se permite comida en el laboratorio. Apaguen los equipos al salir.", 2, B, closed = true),
        Seed("Presentación del sílabo", "En la primera clase revisaremos el sílabo y formaremos los grupos de trabajo.", 3, B),
        Seed("Horario de tutoría", "Tutoría los martes a las 16:00.", 1, B),
    )

    val count: Int get() = seeds.size

    fun isSimulated(postId: Int): Boolean = postId < 0

    private val BACKEND_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT)

    /** Anuncios simulados con la misma forma que los del backend (`service-posts`). */
    fun posts(now: ZonedDateTime, authorName: String = "Docente de prueba"): List<PostEntity> =
        seeds.mapIndexed { index, seed ->
            val course = courses[seed.course]
            val recipient = seed.recipient?.let { students[it - 1] }
            PostEntity(
                id = ID_BASE - index - 1,
                authorName = authorName,
                authorType = 1,
                courseCode = course.courseCode,
                courseName = course.courseName,
                title = seed.title,
                content = seed.content,
                allowComments = !seed.closed,
                countComments = if (seed.closed) 0 else (index * 7) % 6,
                isPrivate = recipient != null,
                date = publishedAt(index, now).withZoneSameInstant(ZoneOffset.UTC).format(BACKEND_FORMAT),
                receiverPrivate = recipient?.id,
                priority = seed.priority.code,
            )
        }

    // 4 de hoy, 4 de ayer y el resto repartido entre 2 y 60 días atrás
    private fun publishedAt(index: Int, now: ZonedDateTime): ZonedDateTime = when {
        index < 4 -> now.minus(Duration.ofMinutes(listOf(4L, 35L, 95L, 170L)[index]))
        index < 8 -> now.toLocalDate().minusDays(1)
            .atTime(listOf(18, 15, 11, 8)[index - 4], listOf(20, 5, 40, 15)[index - 4])
            .atZone(now.zone)
        else -> {
            val daysAgo = 2L + (index - 8) * 58L / (seeds.size - 9).coerceAtLeast(1)
            now.toLocalDate().minusDays(daysAgo)
                .atTime(7 + (index * 5) % 12, (index * 13) % 60)
                .atZone(now.zone)
        }
    }
}
