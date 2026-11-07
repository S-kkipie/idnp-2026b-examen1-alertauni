# Examen 2 – 2026B · Análisis y diseño del Muro de Anuncios

**Curso:** Introducción al Desarrollo de Nuevas Plataformas (E) · **Docente:** Mg. Ernesto Suárez López\
**Aplicativo:** AlertaUNI (`com.erns.alertauni`) – Android · Jetpack Compose · Hilt · Supabase\
**Grupo:** B7 — Adrian Issac Mamani Quispe · Daniel Wilston Chura Monroy · Johann Andre Cáceres Ruiz · Daniel Bedregal Pérez · Yourdyy Yossimar Huayhua Hillpa\
**Maquetas:** [`Docs/Examen2/maquetas/maquetas.html`](maquetas/maquetas.html) · **Prototipo en Compose (opcional):** `screen/post/` y `domain/post/`

---

## 1. Análisis de la interfaz actual (Tarea 1)

### 1.1 Método

Evaluamos el Muro de Anuncios del docente (`PostScreen.kt`, Figura 1) de tres formas complementarias:

1. **Recorrido de tareas** del docente con la app y el código: *T1* publicar un anuncio público, *T2* enviar un anuncio privado a un estudiante, *T3* encontrar un anuncio entre muchos, *T4* archivar un anuncio.
2. **Heurísticas de usabilidad de Nielsen** (visibilidad del estado, reconocer antes que recordar, prevención de errores, diseño minimalista, consistencia).
3. **Revisión del código** de la pantalla para confirmar cada problema con evidencia y no sólo con la captura.

Alcance acordado con el docente: sólo la pantalla principal del muro (no el chat de comentarios) y sólo orientación vertical.

![Figura 1. Interfaz actual con los problemas 1–9 marcados (ver Tabla 1). Los problemas 10 y 11 son de interacción y no se ven en una captura.](maquetas/00-actual-anotada.png)

### 1.2 Problemas identificados

**Tabla 1.** Problemas de diseño de la interfaz actual.

| # | Criterio | Problema | Evidencia | Por qué dificulta la experiencia |
|--|--------|----------------------|------------------|------------------------|
| 1 | Componentes | Público y Privado usan **el mismo ícono** de persona; sólo los diferencia una palabra gris de 16 sp. | `Icons.Outlined.Person` para ambos tipos en `MessageCard` | El tipo es el dato que más importa al docente (¿quién lo vio?) y es el menos visible. El ícono no comunica nada. |
| 2 | Interacción | **La lupa no busca.** Abre un campo de texto que no está conectado a la lista. | `searchQuery` se crea en `PostScreen` y nunca se usa; `SearchBoxComponent` tiene su propio `query` | Rompe la expectativa del usuario: escribe y no pasa nada. Con muchos anuncios sólo queda hacer scroll. |
| 3 | Organización | Cabecera azul de 100 dp con el **nombre del docente** y un título de 26 sp. | `SearchBoxComponent`, `height(100.dp)` | Ocupa ~13 % de la pantalla con un dato que el docente ya sabe. |
| 4 | Organización | **Autor y avatar** en cada tarjeta. | `UserInitialCircle(announce.authorName)` | Siempre es el propio docente (el enunciado lo excluye). Repite el mismo nombre en todas las tarjetas. |
| 5 | Organización | **Fecha con segundos** `2026-05-31 01:36:32`. | `Text(announce.date)` | Es larga y difícil de leer; al docente le importa "hace cuánto", no el segundo exacto. |
| 6 | Organización | Tarjetas de **altura fija** (220 dp) con título de 24 sp y contenido de 18 sp. | `Card(...height(220.dp))` | Un texto largo se corta y uno corto deja huecos. Sólo caben unos 2,5 anuncios por pantalla. |
| 7 | Componentes | El **FAB tapa** el botón "Comentarios" de la última tarjeta. | `LazyColumn` sin `contentPadding` inferior | El último anuncio no se puede abrir desde su botón. |
| 8 | Componentes | **No se muestra la prioridad** (alta, media o baja). | `PostEntity` y `PostRequest` no tienen prioridad | Un aviso urgente se ve igual que uno informativo, y el enunciado exige mostrarla. |
| 9 | Componentes | Un anuncio privado **no dice a quién** se envió. | `receiverPrivate` existe en `PostEntity` pero no se dibuja | El docente no puede saber, desde el muro, a qué estudiante le escribió. |
| 10 | Interacción | Toda la tarjeta y el botón "Comentarios" hacen lo mismo, y **no hay acciones de gestión** (archivar, activar o desactivar comentarios). | `Modifier.clickable` + botón con el mismo `onClickComment` | Dos objetivos para una sola acción y ninguna forma de ordenar el muro. |
| 11 | Interacción | El **envío privado está escondido** en otra pestaña (Contactos → curso → scroll → estudiante → diálogo). El "+" del muro sólo publica públicos. | `PostViewModel.sendPost` fija `is_private = false`; el privado vive en `TeacherContactScreen` | El docente no sabe que existe y debe recordar dónde está (heurística *reconocer antes que recordar*). Además no puede elegir prioridad ni si se permiten comentarios. |

### 1.3 Recorrido de las tareas en la interfaz actual

| Tarea | Pasos hoy | Observación |
|--------------|------------------------------|-----------------------|
| T1 · Publicar público | "+" → elegir curso → título → contenido → Enviar (5) | Funciona, pero no hay prioridad ni opción de comentarios. |
| T2 · Privado a un estudiante | Pestaña Contactos → elegir curso → scroll por la lista → tocar estudiante → título y contenido → Enviar (6 o más) | Hay que saber de antemano en qué curso está el estudiante y buscarlo a ojo. |
| T3 · Encontrar un anuncio | Scroll lineal | Con 4 cursos y 15 anuncios por curso (60 en total) son unas 24 pantallas de scroll. |
| T4 · Archivar | No existe | El muro sólo crece. |

**Qué conservamos:** la barra inferior (Posts · Contactos · Cursos), la paleta azul de la app, el "+" como punto de entrada para crear, el chip con el acrónimo del curso y el contador de comentarios. Funcionan y el usuario ya los conoce.
