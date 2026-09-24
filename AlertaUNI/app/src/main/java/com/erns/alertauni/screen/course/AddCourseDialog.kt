package com.erns.alertauni.screen.course


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erns.alertauni.data.model.StudentEnrollment
import com.erns.alertauni.ui.theme.MyMutedForegroundColor
import com.erns.alertauni.ui.theme.MyPrimaryColor
import com.erns.alertauni.ui.theme.MyPrimaryForegroundColor
import com.erns.alertauni.ui.theme.MySurfaceColor


@Composable
fun AddCourseDialog(
    studentEnrollment: StudentEnrollment?,
    onDismiss: () -> Unit,
    onClickFindCourse: (String) -> Unit,
    onClickCourseEnroll: (String) -> Unit
) {
    val professor = remember { mutableStateOf("") }
    professor.value = if (studentEnrollment != null) (studentEnrollment.firstname
            + " " + studentEnrollment.surname) else "Docente"

    val inputText = remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                IconButton(onClick = onDismiss) {
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
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = inputText.value,
                    onValueChange = { inputText.value = it },
                    placeholder = { Text("Código ...") },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.5f
                        ),
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,   // Quita la línea al enfocar
                        unfocusedIndicatorColor = Color.Transparent, // Quita la línea al desenfocar
                        disabledIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (inputText.value.isNotBlank()) {
                                onClickFindCourse(inputText.value)
                                inputText.value = ""
                            }
                        }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "",
                                tint = Color.Gray,
                                modifier = Modifier.rotate(0f)
                            )
                        }
                    },
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    modifier = Modifier.padding(start = 2.dp),
                    text = studentEnrollment?.courseCode ?: "Código",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MyMutedForegroundColor
                )
                Text(
                    modifier = Modifier.padding(start = 2.dp),
                    text = studentEnrollment?.courseName ?: "Curso",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MyMutedForegroundColor
                )
                Text(
                    modifier = Modifier.padding(start = 2.dp),
                    text = "Tipo ${studentEnrollment?.courseType ?: ""}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MyMutedForegroundColor
                )
                Text(
                    modifier = Modifier.padding(start = 2.dp),
                    text = "Grupo ${studentEnrollment?.groupType ?: ""}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MyMutedForegroundColor
                )
                Text(
                    modifier = Modifier.padding(start = 2.dp),
                    text = professor.value,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MyMutedForegroundColor
                )

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
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MyPrimaryColor
                    )
                ) {
                    Text("Cancelar", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        if (studentEnrollment != null) {
                            onClickCourseEnroll(
                                studentEnrollment.course_catalog_id
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MyPrimaryColor),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "Registrarme",
                        fontWeight = FontWeight.SemiBold,
                        color = MyPrimaryForegroundColor
                    )
                }
            }
        },
        dismissButton = {}
    )
}
