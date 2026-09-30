package com.erns.alertauni.screen.course

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erns.alertauni.R
import com.erns.alertauni.data.model.StudentEnrollment
import com.erns.alertauni.screen.course.CourseViewModel.EnrollUiState
import com.erns.alertauni.ui.theme.MyMutedForegroundColor
import com.erns.alertauni.ui.theme.MyPrimaryColor
import com.erns.alertauni.ui.theme.MyPrimaryForegroundColor
import com.erns.alertauni.ui.theme.MySurfaceColor

private val SuccessColor = Color(0xFF15803D)
private val WarningColor = Color(0xFFB45309)
private val ErrorColor = Color(0xFFB91C1C)

@Composable
fun AddCourseDialog(
    enrollUiState: EnrollUiState,
    onDismiss: () -> Unit,
    onClickFindCourse: (String) -> Unit,
    onClickCourseEnroll: () -> Unit,
    onClickScanQr: () -> Unit = {}
) {
    // rememberSaveable: el código escrito sobrevive a la rotación de pantalla
    val inputText = rememberSaveable { mutableStateOf("") }
    val isBusy = enrollUiState is EnrollUiState.Searching || enrollUiState is EnrollUiState.Enrolling
    val course = enrollUiState.courseOrNull()
    // Estados finales: el proceso terminó (con éxito o a la espera del docente)
    val isFinished = enrollUiState is EnrollUiState.Enrolled ||
            enrollUiState is EnrollUiState.PendingApproval

    val search: () -> Unit = {
        if (inputText.value.isNotBlank() && !isBusy) onClickFindCourse(inputText.value)
    }

    AlertDialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        containerColor = MySurfaceColor,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Agregar Curso",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MyPrimaryColor
                )
                IconButton(onClick = onDismiss, enabled = !isBusy) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = MyMutedForegroundColor
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isFinished) {
                    OutlinedButton(
                        onClick = onClickScanQr,
                        enabled = !isBusy,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MyPrimaryColor)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_qr_code_scanner_24),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Escanear código QR")
                    }
                    Text(
                        text = "o ingrese el código",
                        fontSize = 13.sp,
                        color = MyMutedForegroundColor,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = inputText.value,
                    onValueChange = { inputText.value = it },
                    placeholder = { Text("Código de clase") },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    enabled = !isBusy && !isFinished,
                    isError = enrollUiState is EnrollUiState.NotFound,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(onSearch = { search() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.5f
                        ),
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        if (enrollUiState is EnrollUiState.Searching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            IconButton(onClick = search, enabled = inputText.value.isNotBlank()) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Buscar curso",
                                    tint = Color.Gray
                                )
                            }
                        }
                    },
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (course != null) {
                    CourseSummary(course)
                } else if (enrollUiState is EnrollUiState.Idle) {
                    Text(
                        text = "Ingrese el código que le proporcionó su docente.",
                        fontSize = 14.sp,
                        color = MyMutedForegroundColor
                    )
                }

                EnrollStatusMessage(enrollUiState)
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isBusy,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MyPrimaryColor
                    )
                ) {
                    Text(
                        if (isFinished) "Cerrar" else "Cancelar",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (!isFinished) {
                    val canEnroll = enrollUiState is EnrollUiState.CourseFound ||
                            (enrollUiState is EnrollUiState.Error && enrollUiState.course != null)
                    Button(
                        onClick = onClickCourseEnroll,
                        enabled = canEnroll,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MyPrimaryColor),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        if (enrollUiState is EnrollUiState.Enrolling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MyPrimaryForegroundColor
                            )
                        } else {
                            Text(
                                if (enrollUiState is EnrollUiState.Error) "Reintentar" else "Registrarme",
                                fontWeight = FontWeight.SemiBold,
                                color = MyPrimaryForegroundColor
                            )
                        }
                    }
                }
            }
        },
        dismissButton = {}
    )
}

@Composable
private fun CourseSummary(course: StudentEnrollment) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "${course.courseCode} · ${course.courseName}",
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = MyPrimaryColor
        )
        Text(
            text = "Tipo ${course.courseType} · Grupo ${course.groupType} · ${course.semester}",
            fontSize = 14.sp,
            color = MyMutedForegroundColor
        )
        Text(
            text = "Docente: ${course.firstname} ${course.surname}",
            fontSize = 14.sp,
            color = MyMutedForegroundColor
        )
    }
}

@Composable
private fun EnrollStatusMessage(state: EnrollUiState) {
    val (icon, color, text) = when (state) {
        is EnrollUiState.NotFound -> Triple(Icons.Default.Warning, ErrorColor, state.message)
        is EnrollUiState.AlreadyEnrolled -> Triple(
            Icons.Default.Info, WarningColor, "Ya se encuentra registrado en este curso."
        )
        is EnrollUiState.Enrolled -> Triple(
            Icons.Default.CheckCircle, SuccessColor, "Registro exitoso. El curso ya aparece en su lista."
        )
        is EnrollUiState.PendingApproval -> Triple(
            Icons.Default.Info, WarningColor,
            "No figura en la lista de matriculados. Se envió su solicitud al docente; le avisaremos cuando la apruebe."
        )
        is EnrollUiState.Error -> Triple(Icons.Default.Warning, ErrorColor, state.message)
        else -> return
    }
    StatusRow(icon, color, text)
}

@Composable
private fun StatusRow(icon: ImageVector, color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(text = text, color = color, fontSize = 14.sp)
    }
}

private fun EnrollUiState.courseOrNull(): StudentEnrollment? = when (this) {
    is EnrollUiState.CourseFound -> course
    is EnrollUiState.AlreadyEnrolled -> course
    is EnrollUiState.Enrolling -> course
    is EnrollUiState.Enrolled -> course
    is EnrollUiState.PendingApproval -> course
    is EnrollUiState.Error -> course
    else -> null
}

private val previewCourse = StudentEnrollment(
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

@Preview(name = "Inicial")
@Composable
private fun AddCourseDialogIdlePreview() {
    AddCourseDialog(EnrollUiState.Idle, {}, {}, {})
}

@Preview(name = "Curso encontrado")
@Composable
private fun AddCourseDialogFoundPreview() {
    AddCourseDialog(EnrollUiState.CourseFound(previewCourse), {}, {}, {})
}

@Preview(name = "Código inválido")
@Composable
private fun AddCourseDialogNotFoundPreview() {
    AddCourseDialog(
        EnrollUiState.NotFound("No se encontró un curso con el código \"XYZ\" o su inscripción está cerrada"),
        {}, {}, {}
    )
}

@Preview(name = "Ya registrado")
@Composable
private fun AddCourseDialogAlreadyEnrolledPreview() {
    AddCourseDialog(EnrollUiState.AlreadyEnrolled(previewCourse), {}, {}, {})
}

@Preview(name = "Registro exitoso")
@Composable
private fun AddCourseDialogEnrolledPreview() {
    AddCourseDialog(EnrollUiState.Enrolled(previewCourse), {}, {}, {})
}

@Preview(name = "Pendiente de aprobación")
@Composable
private fun AddCourseDialogPendingPreview() {
    AddCourseDialog(EnrollUiState.PendingApproval(previewCourse), {}, {}, {})
}

@Preview(name = "Error de red")
@Composable
private fun AddCourseDialogErrorPreview() {
    AddCourseDialog(
        EnrollUiState.Error("No se pudo completar la inscripción. Verifique su conexión.", previewCourse),
        {}, {}, {}
    )
}
