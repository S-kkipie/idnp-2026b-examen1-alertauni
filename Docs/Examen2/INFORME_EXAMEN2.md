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

---

## 2. Propuesta de soluciones (Tarea 2)

Criterios de comparación, tomados del enunciado: **simplicidad** (pocos controles y pasos), **claridad de la interacción** (se entiende con sólo mirar) y **prevención de errores**. Agregamos el **costo de implementación** en Compose. Puntaje de 1 a 5.

### 2.1 Problema 1 — Envío de anuncios públicos y privados

![Figura 2. Alternativas para el Problema 1 (A: dos botones, B: campo «Para», C: privado desde Contactos).](img/p1-alternativas.png)

- **A. Dos botones o FAB desplegable.** El "+" se abre en "Anuncio público" y "Anuncio privado".
  - *Ventajas:* el tipo se elige de forma explícita.
  - *Limitaciones:* son dos puntos de entrada y el docente debe decidir el tipo antes de pensar en el destinatario. Compose no trae un *speed dial* nativo, hay que construirlo con `AnimatedVisibility`. Además tapa más contenido.
- **B. Un solo botón "Nuevo anuncio" con campo «Para».** Una hoja inferior empieza por el destinatario: `Todo un curso | Un estudiante`.
  - En *Un estudiante*, un único buscador encuentra al alumno entre **todos** los cursos del docente, por nombre o CUI, y fija el curso automáticamente.
  - El botón de envío dice a quién llega: *"Enviar a 32 estudiantes de INF-01"* o *"Enviar sólo a Luis Quispe"*.
  - *Ventajas:* un solo flujo y destinatario siempre visible.
  - *Limitaciones:* hay un control más en el formulario y hay que cargar los estudiantes.
- **C. Mantener el privado en Contactos** y enlazarlo desde el muro.
  - *Ventajas:* casi no cambia el código.
  - *Limitaciones:* sigue siendo poco descubrible, toma 6 pasos o más y obliga a recorrer curso por curso.

**Tabla 2.** Comparación ponderada, Problema 1.

| Criterio (peso) | A. Dos botones | **B. Campo «Para»** | C. Contactos |
|---|:-:|:-:|:-:|
| Simplicidad (30 %) | 3 | **5** | 2 |
| Claridad de quién recibe (30 %) | 4 | **5** | 3 |
| Prevención de errores (25 %) | 3 | **5** | 3 |
| Costo de implementación (15 %) | 2 | **4** | 5 |
| **Total** | 3,15 | **4,85** | 3,00 |

**Elegimos B.** Resuelve el reto que planteó el docente: enviar un privado a un estudiante sin entrar al curso ni recorrer la lista. Lo hace con **un solo punto de entrada**, así que no hay botones duplicados. Previene errores en tres momentos:

1. **Antes:** el destinatario es la primera decisión y el curso se deduce del estudiante elegido.
2. **Durante:** el chip del estudiante elegido y el texto de ayuda de la prioridad informan del efecto de cada opción.
3. **Al confirmar:** el botón repite el destinatario, el envío se deshabilita mientras falten datos y cerrar con texto escrito pide confirmación.

### 2.2 Problema 2 — Visualización y búsqueda de anuncios

![Figura 3. Alternativas para el Problema 2 (A: lupa expandible, B: buscador + filtros + grupos por fecha, C: pestañas por curso).](img/p2-alternativas.png)

- **A. Lupa en la cabecera que se expande**, con la lista ordenada por fecha.
  - *Ventajas:* casi no ocupa espacio.
  - *Limitaciones:* la búsqueda queda escondida (el problema actual), no se pueden combinar filtros y no se sabe en qué campos busca.
- **B. Buscador siempre visible, chips de filtro y lista agrupada por fecha**, con tarjetas compactas.
  - El buscador mira título, contenido, curso y nombre del destinatario, sin distinguir mayúsculas ni tildes, y resalta las coincidencias.
  - Los chips filtran por tipo, prioridad alta y curso.
  - Los encabezados *Hoy · Ayer · Esta semana · Mes* dan orientación temporal.
  - *Ventajas:* todo se ve de un vistazo y se combina.
  - *Limitaciones:* ocupa unos 100 dp arriba, espacio que se recupera al quitar la cabecera de 100 dp y compactar las tarjetas.
- **C. Pestañas por curso** (`ScrollableTabRow`) con búsqueda.
  - *Ventajas:* separa los cursos.
  - *Limitaciones:* con 5 o más cursos las pestañas se desbordan, no se puede buscar entre cursos y un privado queda escondido dentro de su curso.

**Tabla 3.** Comparación ponderada, Problema 2.

| Criterio (peso) | A. Lupa | **B. Buscador + chips** | C. Pestañas |
|---|:-:|:-:|:-:|
| Rapidez para encontrar (30 %) | 3 | **5** | 3 |
| Visibilidad / descubrimiento (25 %) | 2 | **5** | 4 |
| Escala con muchos cursos y anuncios (20 %) | 3 | **5** | 2 |
| Espacio en pantalla (15 %) | 5 | **3** | 3 |
| Costo de implementación (10 %) | 5 | **4** | 4 |
| **Total** | 3,25 | **4,60** | 3,15 |

**Elegimos B** y lo complementamos con decisiones sobre la información de cada tarjeta (Tabla 4) para que entren más anuncios por pantalla sin perder ningún dato obligatorio.

**Tabla 4.** Información de cada anuncio: antes y después.

| Dato (enunciado) | Antes | Después | Motivo |
|-----------|-------------|---------------------|---------------------|
| Acrónimo del curso | Chip gris | Se mantiene | Ya era claro y compacto. |
| Tipo | Ícono igual + palabra | Ícono distinto: grupo (público) o candado (privado) + **"Para: nombre"** en privados | El ícono basta para el tipo; en el privado lo importante es a quién. |
| Título y contenido | 24 sp / 18 sp, altura fija | 16 sp / 14 sp, contenido en 2 líneas con "…" | La tarjeta se adapta al texto. El detalle completo está al tocarla. |
| Prioridad | No existía | Franja lateral de color + etiqueta *Alta / Media / Baja* | El color se ve de lejos y el texto no depende del color (daltonismo). |
| N.º de comentarios | Botón de texto | Ícono + número, o "Comentarios cerrados" | Muestra también si los comentarios están activos. |
| Fecha y hora | `2026-05-31 01:36:32` | *hace 2 h · ayer 14:30 · 31 may* | Más corta y fácil de leer. La fecha exacta se ve en el detalle. |
| Autor | Avatar + nombre | Se elimina | Siempre es el propio docente. |

**Archivar.** Se hace deslizando la tarjeta hacia la izquierda o desde el menú ⋮. Aparece un Snackbar con **Deshacer**, y los archivados se ven en su propia vista (ícono de archivo en la barra superior). Así el muro queda despejado sin perder nada. El menú ⋮ también permite **activar o desactivar los comentarios** de cada anuncio.
