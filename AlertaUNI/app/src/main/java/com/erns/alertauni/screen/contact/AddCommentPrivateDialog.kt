package com.erns.alertauni.screen.contact

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erns.alertauni.data.model.CatalogCourseEntity
import com.erns.alertauni.data.model.StudentEntity
import com.erns.alertauni.ui.theme.MyBorderColor
import com.erns.alertauni.ui.theme.MyMutedForegroundColor
import com.erns.alertauni.ui.theme.MyPrimaryColor
import com.erns.alertauni.ui.theme.MyPrimaryForegroundColor
import com.erns.alertauni.ui.theme.MySecondaryColor
import com.erns.alertauni.ui.theme.MySurfaceColor


@Composable
fun AddCommentPrivateDialog(
    onDismiss: () -> Unit,
    studentEntity: StudentEntity,
    onClickNewPost: (String, String, String) -> Unit
) {
    val titlePost = remember { mutableStateOf("") }
    val contentPost = remember { mutableStateOf("") }
    val opcionSeleccionada = remember { mutableStateOf("") }
    val maxWords = 280

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
                    text = "Anuncio privado",
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${studentEntity.firstname} ${studentEntity.surname}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = MyPrimaryColor,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Título",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MyMutedForegroundColor,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    TextField(
                        value = titlePost.value,
                        onValueChange = {
                            titlePost.value = it
                        },
                        placeholder = { Text("Ej: Nueva tarea disponible") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MySecondaryColor,
                            unfocusedContainerColor = MySecondaryColor,
                            disabledContainerColor = MySecondaryColor,
                            focusedIndicatorColor = MyPrimaryColor,
                            unfocusedIndicatorColor = MyBorderColor,
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Contenido",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MyMutedForegroundColor,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    TextField(
                        value = contentPost.value,
                        onValueChange = { newValue ->
                            if (newValue.length <= maxWords) {
                                contentPost.value = newValue
                            } else {
                                contentPost.value = newValue.take(maxWords)
                            }
                        },
                        placeholder = { Text("Escribe el contenido del anuncio...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = false,
                        maxLines = 5,
                        minLines = 5,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MySecondaryColor,
                            unfocusedContainerColor = MySecondaryColor,
                            disabledContainerColor = MySecondaryColor,
                            focusedIndicatorColor = MyPrimaryColor,
                            unfocusedIndicatorColor = MyBorderColor,
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                    )
                    Text(
                        text = "Caracteres  ${contentPost.value.length}/${maxWords}",
                        fontSize = 12.sp,
                        color = MyMutedForegroundColor
                    )
                }
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
                        if (titlePost.value.isNotBlank() && contentPost.value.isNotBlank()) {
                            onClickNewPost(
                                studentEntity.student_id,
                                titlePost.value,
                                contentPost.value
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
                        "Publicar",
                        fontWeight = FontWeight.SemiBold,
                        color = MyPrimaryForegroundColor
                    )
                }
            }
        },
        dismissButton = {}
    )
}
