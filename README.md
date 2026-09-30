# AlertaUNI — IDNP 2026B (Examen 1 + Proyecto Integrador Parte 1)

Aplicación Android (Jetpack Compose + Hilt + Supabase + FCM) para la comunicación académica entre docentes y estudiantes de un curso universitario.
Este repositorio contiene el **proyecto base del curso** y el trabajo de dos entregas:

| Entrega | Informe | Tema |
|---|---|---|
| **Examen 1 – 2026B** | [`Docs/Examen1/INFORME_EXAMEN1.md`](Docs/Examen1/INFORME_EXAMEN1.md) | Comprender, mejorar y hacer evolucionar la app existente |
| **Proyecto Integrador – Parte 1** | [`Docs/Proyecto01/INFORME_PROYECTO01.md`](Docs/Proyecto01/INFORME_PROYECTO01.md) | Diseño del proceso de incorporación de estudiantes a un curso |

Estructura: `AlertaUNI/` (app Android) · `Backend/supabase/` (Edge Functions y migraciones) · `Docs/` (informes, prototipos y modelo de datos).

Leyenda: ✅ hecho · 🟡 parcial · ⬜ pendiente (lo hace el grupo)

---

## 1. Examen 1 — lo que pide el enunciado vs. lo realizado

### Actividad 1 · Comprensión del aplicativo actual (C3 · 8 pts)

| Pide el enunciado | Estado | Dónde / qué se hizo |
|---|---|---|
| Identificar **todas** las funcionalidades y comportamientos implementados | ✅ | Informe §1.3: 12 funcionalidades (F1–F12), incluidas las no mencionadas en el enunciado: completar perfil/rol, correo al estudiante, unirse por código, notificaciones push guardadas en Room sin UI, sesión cifrada |
| Por cada una: qué permite al usuario (docente/estudiante) | ✅ | Informe §1.3, apartado "Qué permite" de cada funcionalidad |
| Componentes de Android/Jetpack Compose que intervienen | ✅ | Informe §1.3, apartado "Componentes" |
| Cómo se gestionan estado, eventos y lógica | ✅ | Informe §1.1, §1.2 (diagrama de secuencia) y §1.3 |
| Distribución de responsabilidades UI / ViewModel / otros | ✅ | Informe §1.1 (diagrama y tabla de capas) y §1.3 |
| Demostrar comprensión, no sólo enumerar archivos | ✅ | Informe §1.4: 16 hallazgos (defectos, código muerto, riesgos de seguridad del backend) |

### Actividad 2 · Evaluación de funcionalidades existentes (C2 · 8 pts)

| Pide el enunciado | Estado | Dónde / qué se hizo |
|---|---|---|
| Al menos **dos** funcionalidades a mejorar, pensando en usuarios reales | ✅ | **Mejora 1**: incorporación a curso con estados explícitos. **Mejora 2**: tablero de anuncios (actualizar, estados de carga/vacío/error, confirmación al publicar, botón según rol) |
| Problema o limitación identificada | ✅ | Informe §2, apartado "Problema identificado" de cada mejora |
| Comportamiento que debería modificarse | ✅ | Informe §2, "Comportamiento esperado" (con diagrama de estados) |
| Recursos de Android/Compose necesarios | ✅ | Informe §2, tablas de recursos |
| Justificar por qué son apropiados | ✅ | Mismas tablas, columnas "Justificación" y "Restricciones" |
| Integración con la arquitectura existente | ✅ | Informe §2, "Integración con la arquitectura" + **implementado en código** |
| (extra) Correcciones encontradas en el análisis | ✅ | NavHost duplicado, permiso `POST_NOTIFICATIONS`, base Room única en el servicio FCM |

### Actividad 3 · Evolución del aplicativo (C1 · 4 pts)

| Pide el enunciado | Estado | Dónde / qué se hizo |
|---|---|---|
| Al menos **dos** funcionalidades nuevas | ✅ | Informe §3: P1 Bandeja de avisos, P2 Consultas privadas del estudiante, P3 Confirmación de lectura, P4 Unirse por QR |
| Desarrollar técnicamente al menos una | ✅ | **P1 Bandeja de avisos**, implementada: pestaña "Avisos", Room con `Flow`, repositorio, módulo Hilt |
| Necesidad, comportamiento esperado, recursos, estados/eventos, responsabilidades e integración | ✅ | Informe §3 (evaluación de alternativas, diagramas de estados y de capas, restricciones) |
| Evidencia técnica de viabilidad | ✅ | Código compilado (`assembleDebug`) y 2 `@Preview` de la bandeja |

### Sustentación

| Pide el enunciado | Estado | Dónde |
|---|---|---|
| Preparación para preguntas sobre el código y las decisiones | 🟡 | Informe §5 tiene una guía de preguntas probables; **cada integrante debe estudiarla** |

---

## 2. Proyecto Integrador Parte 1 — lo que pide el enunciado vs. lo realizado

### 4.1 Evaluación de alternativas

| Pide el enunciado | Estado | Dónde / qué se hizo |
|---|---|---|
| Analizar QR, código alfanumérico e invitación del docente (u otro mecanismo) | ✅ | Informe §2: tabla de ventajas y limitaciones de cada alternativa + propuesta **híbrida** |
| Considerar facilidad de uso, errores, control de acceso, seguridad, facilidad para docente y estudiante, viabilidad en Android | ✅ | Informe §2: **matriz ponderada** con esos 7 criterios y la explicación de la puntuación |
| Seleccionar y justificar el mecanismo | ✅ | Híbrido: el QR y el código llevan el mismo token + validación contra la lista de matriculados + "pendiente de aprobación" (5 justificaciones) |
| (rúbrica) Analizar aplicaciones similares | ✅ | Informe §1: Classroom, Teams, Moodle, Kahoot/Mentimeter, WhatsApp y lo que se aprendió de cada una |

### 4.2 Diseño del flujo de registro

| Pide el enunciado | Estado | Dónde |
|---|---|---|
| Cómo inicia el proceso | ✅ | Informe §3: diagrama de actividad con carriles + tabla |
| Qué información proporciona o recibe el estudiante | ✅ | Informe §3, tabla |
| Qué acciones realiza el docente | ✅ | Informe §3: mostrar/proyectar/compartir, regenerar, abrir/cerrar, aprobar/rechazar |
| Cómo se valida el registro | ✅ | Informe §3: validación en la app (`JoinCode`) y en el servidor (`service-course-enroll`) |
| Qué pasa con éxito y con error | ✅ | Informe §3: diagrama + máquina de estados |

### 4.3 Prototipo de interfaces (Figma)

| Pide el enunciado | Estado | Dónde / qué se hizo |
|---|---|---|
| Pantallas del proceso (no toda la app) | ✅ | 9 pantallas diseñadas en **Google Stitch** con el estilo de la app. Capturas en [`Docs/Proyecto01/stitch/`](Docs/Proyecto01/stitch) |
| Ingreso de información | ✅ | 01 Unirse (QR o código), 02 Confirmar inscripción |
| Espera o procesamiento | ✅ | 09 Escáner / verificando |
| Registro exitoso | ✅ | 06 |
| Código inválido | ✅ | 03 |
| Estudiante ya registrado | ✅ | 04 |
| Error durante el proceso | ✅ | 07 Error de red |
| (extra) Pendiente de aprobación y vista del docente | ✅ | 05 y 08 |
| **Archivo en Figma** | ⬜ | Falta exportar de Stitch a Figma (pasos en el informe §8) y **pegar aquí el enlace**. Las capturas de las pantallas 01 y 09 están en el proyecto Stitch, no en el repositorio |

### 4.4 Recursos de Android y Jetpack Compose

| Pide el enunciado | Estado | Dónde |
|---|---|---|
| Componentes, estado, eventos, ViewModel, navegación, lectura de QR, validación y otros | ✅ | Informe §5: 15 recursos |
| Por cada recurso: función, dónde se usa y por qué es apropiado | ✅ | Informe §5, columnas "Función", "Dónde" y "Por qué", más **restricciones y alternativas descartadas** (p. ej. Google code scanner frente a CameraX + ML Kit) |

### 4.5 Prototipo en Jetpack Compose

| Pide el enunciado | Estado | Dónde / qué se hizo |
|---|---|---|
| Parte representativa implementada (se puede usar `@Preview`) | ✅ | Ingreso y validación del código, **escaneo de QR**, todos los estados del proceso, confirmación y **QR del docente** (ZXing). 9 `@Preview` |
| Ingreso y validación de un código | ✅ | `domain/course/JoinCode.kt` + 5 pruebas unitarias (`JoinCodeTest`) |
| Distintos estados de la pantalla | ✅ | `EnrollUiState` con 9 estados → `AddCourseDialog` |
| Manejo de eventos de botones | ✅ | Buscar, escanear, registrarse, reintentar, regenerar, abrir/cerrar inscripción |
| Respetar la arquitectura existente y separar responsabilidades | ✅ | UI → ViewModel (Hilt) → Repository → DataSource → Edge Function; informe §6 |
| (extra) Backend del mecanismo híbrido | 🟡 | Migración SQL + `service-course-enroll`, `service-course-class-code` y `service-enrollment-requests`, **escritos pero no desplegados ni probados** contra Supabase |
| Aprobar o rechazar solicitudes (docente) | ✅ | Sección "Solicitudes pendientes" en el diálogo del docente (`ClassCodeDialog`) + `service-enrollment-requests` (LIST/APPROVE/REJECT). El endpoint no está desplegado |

### 5. Entregables

| # | Entregable | Estado | Ubicación |
|---|---|---|---|
| 1 | Comparación de alternativas | ✅ | Informe Proyecto §2 |
| 2 | Alternativa seleccionada y justificación | ✅ | Informe Proyecto §2 |
| 3 | Flujo completo | ✅ | Informe Proyecto §3 |
| 4 | Prototipo en Figma | 🟡 | Stitch listo; falta exportar a Figma y agregar el enlace |
| 5 | Recursos de Android/Compose | ✅ | Informe Proyecto §5 |
| 6 | Prototipo en Jetpack Compose | ✅ | `AlertaUNI/app/src/main/java/com/erns/alertauni/screen/course/`, `domain/course/` |
| 7 | Decisiones técnicas | ✅ | Informe Proyecto §7 (8 decisiones con sus alternativas) |
| – | Código en GitHub | ✅ | Este repositorio |

### Pendientes del grupo

- ⬜ Grupo de **4 a 5 integrantes**: agregar sus nombres a este README.
- ⬜ Exportar las pantallas de Stitch a **Figma** y pegar el enlace.
- ⬜ **Sustentación**: cada integrante debe poder explicar el flujo, las decisiones, los recursos, el estado y los eventos, y la relación entre Figma y el código (guía en el informe §9 y la tabla del §8).

---

## Verificación

```bash
cd AlertaUNI
./gradlew :app:testDebugUnitTest   # JoinCodeTest: 5/5 OK
./gradlew :app:assembleDebug       # compila (Hilt + Room)
```

Para compilar se necesita `app/google-services.json` (no se incluye; está en `.gitignore`).
Para ejecutar contra el backend hay que configurar la URL y la clave de Supabase en `data/di/SupabaseModule.kt` y el `serverClientId` de Google en `LoginViewModel`. El proyecto base del curso no trae estas credenciales, por eso **la app no se probó en ejecución**, sólo se compiló y se corrieron las pruebas unitarias.

Requisitos: Android Studio, JDK 17, dispositivo con Android 9 (API 28) o superior y Google Play services (para el escáner de QR).
