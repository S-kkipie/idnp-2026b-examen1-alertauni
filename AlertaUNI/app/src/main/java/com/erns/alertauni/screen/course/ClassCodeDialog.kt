package com.erns.alertauni.screen.course

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erns.alertauni.data.model.ClassCodeInfo
import com.erns.alertauni.domain.course.JoinCode
import com.erns.alertauni.screen.course.CourseViewModel.ClassCodeUiState
import com.erns.alertauni.ui.theme.MyMutedForegroundColor
import com.erns.alertauni.ui.theme.MyPrimaryColor
import com.erns.alertauni.ui.theme.MySurfaceColor
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Diálogo del docente para compartir el código de inscripción de su curso:
 * QR para proyectar en el aula y el mismo código en texto para copiar o compartir.
 */
@Composable
fun ClassCodeDialog(
    state: ClassCodeUiState,
    onDismiss: () -> Unit,
    onRegenerate: () -> Unit,
    onEnrollmentOpenChange: (Boolean) -> Unit,
    onRetry: () -> Unit
) {
    if (state is ClassCodeUiState.Hidden) return
    val course = when (state) {
        is ClassCodeUiState.Loading -> state.course
        is ClassCodeUiState.Ready -> state.course
        is ClassCodeUiState.Error -> state.course
        ClassCodeUiState.Hidden -> return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MySurfaceColor,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Código de inscripción",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MyPrimaryColor
                    )
                    Text(
                        text = "${course.courseCode} · Grupo ${course.groupType}",
                        fontSize = 14.sp,
                        color = MyMutedForegroundColor
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MyMutedForegroundColor)
                }
            }
        },
        text = {
            when (state) {
                is ClassCodeUiState.Loading -> Box(
                    Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                is ClassCodeUiState.Error -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(state.message, textAlign = TextAlign.Center)
                    TextButton(onClick = onRetry) { Text("Reintentar") }
                }

                is ClassCodeUiState.Ready -> ClassCodeContent(
                    info = state.info,
                    isUpdating = state.isUpdating,
                    onRegenerate = onRegenerate,
                    onEnrollmentOpenChange = onEnrollmentOpenChange
                )

                ClassCodeUiState.Hidden -> Unit
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
private fun ClassCodeContent(
    info: ClassCodeInfo,
    isUpdating: Boolean,
    onRegenerate: () -> Unit,
    onEnrollmentOpenChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val qrContent = JoinCode.toQrContent(info.classCode)
    // El QR sólo se vuelve a generar cuando cambia el código (p. ej. al regenerarlo)
    val qrBitmap = remember(qrContent) { generateQrBitmap(qrContent, 512) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                bitmap = qrBitmap,
                contentDescription = "Código QR del curso",
                filterQuality = FilterQuality.None, // bordes nítidos al escalar
                modifier = Modifier.size(200.dp)
            )
            if (!info.enabled || isUpdating) {
                Box(
                    Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUpdating) CircularProgressIndicator()
                    else Text(
                        "Inscripción cerrada",
                        fontWeight = FontWeight.Bold,
                        color = MyPrimaryColor
                    )
                }
            }
        }

        Text(
            text = info.classCode,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            letterSpacing = 3.sp
        )
        Text(
            text = buildString {
                append("${info.enrolledCount} de ${info.rosterCount} matriculados unidos")
                if (info.pendingCount > 0) append(" · ${info.pendingCount} pendientes")
                info.expiresAt?.let { append("\nVence: $it") }
            },
            fontSize = 13.sp,
            color = MyMutedForegroundColor,
            textAlign = TextAlign.Center
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(onClick = { clipboard.setText(AnnotatedString(info.classCode)) }) {
                Text("Copiar")
            }
            FilledTonalButton(onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Únete al curso en AlertaUNI con el código ${info.classCode}"
                    )
                }
                context.startActivity(Intent.createChooser(intent, "Compartir código"))
            }) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            FilledTonalButton(onClick = onRegenerate, enabled = !isUpdating) {
                Icon(Icons.Default.Refresh, contentDescription = "Regenerar código", modifier = Modifier.size(18.dp))
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Inscripción abierta", fontWeight = FontWeight.SemiBold)
                Text(
                    "Al cerrarla, el código deja de funcionar",
                    fontSize = 12.sp,
                    color = MyMutedForegroundColor
                )
            }
            Switch(
                checked = info.enabled,
                onCheckedChange = onEnrollmentOpenChange,
                enabled = !isUpdating
            )
        }
    }
}

/** Genera la imagen del QR con ZXing (dependencia ya incluida en el proyecto). */
fun generateQrBitmap(content: String, sizePx: Int): ImageBitmap {
    val matrix = QRCodeWriter().encode(
        content,
        BarcodeFormat.QR_CODE,
        sizePx,
        sizePx,
        mapOf(EncodeHintType.MARGIN to 1)
    )
    val pixels = IntArray(sizePx * sizePx) { index ->
        if (matrix[index % sizePx, index / sizePx]) android.graphics.Color.BLACK
        else android.graphics.Color.WHITE
    }
    return Bitmap.createBitmap(pixels, sizePx, sizePx, Bitmap.Config.ARGB_8888).asImageBitmap()
}

private val previewInfo = ClassCodeInfo(
    courseCatalogId = "IDNP-2026B-A",
    classCode = "IDNP-7K3Q",
    enabled = true,
    expiresAt = "2026-10-06",
    enrolledCount = 28,
    rosterCount = 32,
    pendingCount = 2
)

private val previewProfessorCourse = com.erns.alertauni.data.model.StudentEnrollment(
    course_catalog_id = "IDNP-2026B-A",
    courseId = "1703240",
    courseCode = "IDNP",
    courseName = "Introducción al Desarrollo de Nuevas Plataformas",
    semester = "2026B",
    courseType = "Teoría",
    groupType = "A",
    firstname = "Ernesto",
    surname = "Suárez",
    email = "docente@unsa.edu.pe"
)

@Preview(name = "Docente - código listo")
@Composable
private fun ClassCodeDialogReadyPreview() {
    ClassCodeDialog(ClassCodeUiState.Ready(previewProfessorCourse, previewInfo), {}, {}, {}, {})
}

@Preview(name = "Docente - inscripción cerrada")
@Composable
private fun ClassCodeDialogClosedPreview() {
    ClassCodeDialog(
        ClassCodeUiState.Ready(previewProfessorCourse, previewInfo.copy(enabled = false)),
        {}, {}, {}, {}
    )
}
