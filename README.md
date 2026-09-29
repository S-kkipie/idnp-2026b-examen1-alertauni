# AlertaUNI – Examen 1 (IDNP 2026B)

Aplicación Android (Jetpack Compose + Hilt + Supabase + FCM) para la comunicación académica entre docentes y estudiantes.
Este repositorio contiene el proyecto base del curso y el trabajo del **Examen 1**.

- **Informe:** [`Docs/Examen1/INFORME_EXAMEN1.md`](Docs/Examen1/INFORME_EXAMEN1.md)
  1. Comprensión del aplicativo (arquitectura, funcionalidades y hallazgos)
  2. Mejoras implementadas: incorporación a curso con estados explícitos y tablero de anuncios con actualización
  3. Evolución: bandeja de avisos (notificaciones persistidas con Room)
- **App Android:** `AlertaUNI/`
- **Backend (Supabase Edge Functions):** `Backend/supabase/functions`
- **Modelo de datos:** `Docs/Modelo base datos.png`

## Ejecutar

1. Abrir `AlertaUNI/` en Android Studio.
2. Agregar `app/google-services.json` del proyecto Firebase y configurar URL/clave de Supabase en `data/di/SupabaseModule.kt` y el `serverClientId` de Google en `LoginViewModel`.
3. Ejecutar en un dispositivo con Android 9 (API 28) o superior.
