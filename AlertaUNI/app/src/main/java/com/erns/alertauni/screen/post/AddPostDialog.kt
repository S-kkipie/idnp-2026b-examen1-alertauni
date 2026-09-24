package com.erns.alertauni.screen.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.erns.alertauni.data.model.CatalogCourseEntity
import com.erns.alertauni.ui.theme.MyBorderColor
import com.erns.alertauni.ui.theme.MyMutedForegroundColor
import com.erns.alertauni.ui.theme.MyPrimaryColor
import com.erns.alertauni.ui.theme.MyPrimaryForegroundColor
import com.erns.alertauni.ui.theme.MySecondaryColor
import com.erns.alertauni.ui.theme.MySelectedColor
import com.erns.alertauni.ui.theme.MySurfaceColor


@Composable
fun AddPostDialog(
    onDismiss: () -> Unit,
    onClickNewPost: (String, String, String, String, String) -> Unit,
    catalogCourses: List<CatalogCourseEntity>
) {
    val courseCatalogSelected = remember { mutableStateOf<CatalogCourseEntity?>(null) }
    val titlePost = remember { mutableStateOf("") }
    val contentPost = remember { mutableStateOf("") }
    val opcionSeleccionada = remember { mutableStateOf("") }
    val maxWords = 280

    val onClickSelectCourse: (CatalogCourseEntity) -> Unit = { catalog ->
        courseCatalogSelected.value = catalog
        opcionSeleccionada.value = catalog.courseName
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
                Text(
                    text = "Nuevo Anuncio",
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
                // Título del anuncio
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Curso",//"Curso: " + if (courseCatalogSelected.value != null) courseCatalogSelected.value!!.courseName else "",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MyMutedForegroundColor,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    SelectorListaCurso(
                        catalogCourses,
                        opcionSeleccionada.value,
                        onClickSelectCourse
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    /*
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        items(catalogCourses.size) { index ->
                            val courseCatalog = catalogCourses[index]
                            Text(
                                text = courseCatalog.courseName,
                                fontSize = 16.sp,
                                color = MyPrimaryColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp)
                                    .background(if (courseCatalogSelected.value?.courseName == courseCatalog.courseName) MySelectedColor else MySecondaryColor)
                                    .padding(12.dp)
                                    .clickable {
                                        onClickSelectCourse(
                                            courseCatalog
                                        )
                                    }
                            )
                        }
                    }
                    */
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
                        // Aplicamos el shape directamente en el TextField, no en el background
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            // Parámetros correctos para Material 3
                            focusedContainerColor = MySecondaryColor,
                            unfocusedContainerColor = MySecondaryColor,
                            disabledContainerColor = MySecondaryColor,

                            // En Material 3, los bordes en un TextField se llaman "Indicator"
                            focusedIndicatorColor = MyPrimaryColor,
                            unfocusedIndicatorColor = MyBorderColor,

                            // Si usas OutlinedTextField serían otros nombres,
                            // pero para TextField (relleno) estos son los correctos.
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                    )
                }

                // Contenido del anuncio
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
//                        onValueChange = { contentPost.value = it },
                        onValueChange = { newValue ->
                            if (newValue.length <= maxWords) {
                                contentPost.value = newValue
                            } else {
                                // Si supera el límite, recortamos al máximo permitido
                                contentPost.value = newValue.take(maxWords)
                            }
                        },
                        placeholder = { Text("Escribe el contenido del anuncio...") },
                        modifier = Modifier.fillMaxWidth(),
                        // Aplicamos el shape directamente en el TextField, no en el background
                        shape = RoundedCornerShape(8.dp),
                        singleLine = false,
                        maxLines = 5,
                        minLines = 5,
                        colors = TextFieldDefaults.colors(
                            // Parámetros correctos para Material 3
                            focusedContainerColor = MySecondaryColor,
                            unfocusedContainerColor = MySecondaryColor,
                            disabledContainerColor = MySecondaryColor,

                            // En Material 3, los bordes en un TextField se llaman "Indicator"
                            focusedIndicatorColor = MyPrimaryColor,
                            unfocusedIndicatorColor = MyBorderColor,

                            // Si usas OutlinedTextField serían otros nombres,
                            // pero para TextField (relleno) estos son los correctos.
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
                        if (titlePost.value.isNotBlank() && contentPost.value.isNotBlank() && courseCatalogSelected.value != null) {
                            onClickNewPost(
                                courseCatalogSelected.value!!.id,
                                courseCatalogSelected.value!!.courseCode,
                                courseCatalogSelected.value!!.courseName,
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

@Composable
fun SelectorListaCurso(
//    opciones: List<String>,
    catalogCourses: List<CatalogCourseEntity>,
    opcionSeleccionada: String,
    onOpcionSeleccionada: (CatalogCourseEntity) -> Unit
) {
    var expandido = remember { mutableStateOf(false) }
    var tamañoTextField = remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    val densidad = LocalDensity.current
    val anchoExactoDp = with(densidad) { tamañoTextField.value.width.toDp() }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = opcionSeleccionada,
            onValueChange = {},
            readOnly = true, // Evita que el usuario escriba
            label = { Text("Selecciona una opción") },
            trailingIcon = {
                IconButton(onClick = { expandido.value = !expandido.value }) {
                    Icon(
                        imageVector = if (expandido.value) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Desplegar menú"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().onGloballyPositioned { coordenadas ->
                tamañoTextField.value = coordenadas.size.toSize()
            }
        )

        // Superficie transparente invisible sobre el TextField para detectar el clic
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expandido.value = true }
        )

        DropdownMenu(
            expanded = expandido.value,
            onDismissRequest = { expandido.value = false },
            modifier = Modifier.width(anchoExactoDp)
        ) {
            catalogCourses.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(text = opcion.courseName) },
                    onClick = {
                        onOpcionSeleccionada(opcion)
                        expandido.value = false
                    },
                    modifier = Modifier.width(anchoExactoDp)
                )
            }
        }
    }
}
