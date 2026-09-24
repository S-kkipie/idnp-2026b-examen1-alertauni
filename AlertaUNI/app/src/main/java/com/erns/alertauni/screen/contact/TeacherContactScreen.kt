package com.erns.alertauni.screen.contact

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.erns.alertauni.data.model.CourseCatalogRequest
import com.erns.alertauni.data.model.PostPrivateRequest
import com.erns.alertauni.data.model.StudentEnrollment
import com.erns.alertauni.data.model.StudentEntity
import com.erns.alertauni.screen.common.SearchBoxComponent
import com.erns.alertauni.ui.theme.MySurfaceColor


@Composable
fun TeacherContactScreen(
    viewModel: ContactViewModel = hiltViewModel(),
) {
    val courses = remember { mutableStateOf<List<StudentEnrollment>>(emptyList()) }
    val students = remember { mutableStateOf<List<StudentEntity>>(emptyList()) }
    val studentSelected = remember { mutableStateOf<StudentEntity?>(null) }
    val courseSelected = remember { mutableStateOf<String?>(null) }
    val showAddDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.studentEnrollmentList.collect { it ->
            courses.value = it
        }
    }

    LaunchedEffect(Unit) {
        viewModel.students.collect { it ->
            students.value = it
        }
    }

    LaunchedEffect(courseSelected.value) {
        if (courseSelected.value != null) {
            viewModel.getStudentByCourse(CourseCatalogRequest(courseSelected.value))
        }
    }

    val onClickNewPost: (String, String, String) -> Unit =
        { studentId, titlePost, contentPost ->
            showAddDialog.value = false
            viewModel.addPostPrivate(
                PostPrivateRequest(
                    courseSelected.value!!,
                    titlePost,
                    contentPost,
                    studentId,
                    true
                )
            )
        }

    val onClickAddComment: (StudentEntity) -> Unit = {
        studentSelected.value = it
        showAddDialog.value = true
    }

    if (showAddDialog.value) {
        AddCommentPrivateDialog(
            onDismiss = { showAddDialog.value = false },
            studentSelected.value!!,
            onClickNewPost = onClickNewPost,
        )
    }

    TeacherContactScreenLayout(
        "Ernesto Suarez",
        "Contactos",
        courses.value,
        students.value,
        courseSelected,
        onClickAddComment
    )

}

@Composable
fun TeacherContactScreenLayout(
    username: String,
    title: String,
    courses: List<StudentEnrollment>,
    students: List<StudentEntity>,
    courseSelected: MutableState<String?>,
    onClickAddComment: (StudentEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SearchBoxComponent(username, title)
        TeacherCourseList(courses, courseSelected)
        LazyColumn() {
            items(students) { student ->
                TeacherContactStudentCard(student, onClickAddComment)
            }
        }
    }

}

@Composable
fun TeacherCourseList(courses: List<StudentEnrollment>, courseSelected: MutableState<String?>) {
    val courseTitle = remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            courses.forEach { course ->
                val isSelected =
                    course.course_catalog_id == courseSelected.value //selectedOptions.value.contains(course.course_catalog_id)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        courseTitle.value = course.courseName
                        courseSelected.value = course.course_catalog_id
                    },
                    label = { Text(course.courseCode) }
                )
            }
        }
        Text(text = courseTitle.value)
    }

}

@Composable
fun TeacherContactStudentCard(
    studentEntity: StudentEntity,
    onClickAddComment: (StudentEntity) -> Unit
) {
    val context = LocalContext.current
    // Configuramos el launcher para manejar intents
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Aquí puedes manejar el resultado si lo necesitas
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MySurfaceColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = studentEntity.codigo,
                        fontSize = 14.sp,
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = "${studentEntity.surname}, ${studentEntity.firstname}",
                        fontSize = 14.sp,
                    )
                }

                Text(
                    text = studentEntity.email,
                    fontSize = 18.sp,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "message/rfc822" // asegura que solo apps de correo lo manejen
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(studentEntity.email))
                            putExtra(Intent.EXTRA_SUBJECT, "Anuncio")
                            putExtra(Intent.EXTRA_TEXT, "")
                        }
                        try {
                            launcher.launch(Intent.createChooser(intent, "Enviar correo con"))
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "No se encontró aplicación de correo",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    //color = Color.Blue, // opcional: para que parezca enlace
                    textDecoration = TextDecoration.Underline // opcional
                )
            }
            IconButton(onClick = {
                onClickAddComment(studentEntity)
            }) {
                Icon(
                    Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "Enviar",
                    tint = Color.Gray,
                    modifier = Modifier.rotate(0f)
                )
            }
        }

    }
}
