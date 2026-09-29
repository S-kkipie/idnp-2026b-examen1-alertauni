package com.erns.alertauni.screen.course

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.erns.alertauni.data.model.StudentEnrollment
import com.erns.alertauni.screen.common.SearchBoxComponent
import com.erns.alertauni.ui.theme.MyMutedForegroundColor
import com.erns.alertauni.ui.theme.MySurfaceColor


@Composable
fun StudentCourseScreen(
    viewModel: CourseViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    onFabActionReady: (() -> Unit) -> Unit
) {
    val studentEnrollmentListState = viewModel.studentEnrollmentList.collectAsState()
    val isLoadingCourses = viewModel.isLoadingCourses.collectAsState()
    val enrollUiState = viewModel.enrollUiState.collectAsState()
    val username = viewModel.username.collectAsState()
    val showAddDialog = rememberSaveable { mutableStateOf(false) }

    val onClickFloatingActionButton: () -> Unit = {
        viewModel.clearEnrollment()
        showAddDialog.value = true
    }

    LaunchedEffect(Unit) {
        onFabActionReady(onClickFloatingActionButton)
    }

    // Evento de un solo disparo: confirmación visible aun después de cerrar el diálogo
    LaunchedEffect(enrollUiState.value) {
        val state = enrollUiState.value
        if (state is CourseViewModel.EnrollUiState.Enrolled) {
            snackbarHostState.showSnackbar("Te registraste en ${state.course.courseName}")
        }
    }

    if (showAddDialog.value) {
        AddCourseDialog(
            enrollUiState = enrollUiState.value,
            onDismiss = {
                showAddDialog.value = false
                viewModel.clearEnrollment()
            },
            onClickFindCourse = { searchCode -> viewModel.findCourse(searchCode) },
            onClickCourseEnroll = { viewModel.courseEnroll() }
        )
    }

    StudentCourseScreenLayout(
        username.value,
        "Cursos",
        studentEnrollmentListState.value,
        isLoadingCourses.value
    )
}

@Composable
fun StudentCourseScreenLayout(
    username: String,
    title: String,
    studentEnrollmentList: List<StudentEnrollment>,
    isLoading: Boolean = false
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SearchBoxComponent(username,title)
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            studentEnrollmentList.isEmpty() -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no estás registrado en ningún curso.\nUsa el botón + e ingresa el código que te dio tu docente.",
                    color = MyMutedForegroundColor,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
            }

            else -> LazyColumn {
                items(studentEnrollmentList, key = { it.course_catalog_id }) { studentEnrollment ->
                    StudentEnrollmentCard(studentEnrollment)
                }
            }
        }
    }
}

@Composable
fun StudentEnrollmentCard(
    studentEnrollment: StudentEnrollment
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(8.dp)
            .clickable(onClick = { }),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MySurfaceColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            )
            {
//                Text(
//                    text = studentEnrollment.courseCode,
//                    color = MyMutedForegroundColor,
//                    fontSize = 16.sp,
//                    modifier = Modifier
//                        .background(
//                            color = MyMutedColor,
//                            shape = RoundedCornerShape(4.dp)
//                        )
//                        .padding(horizontal = 8.dp, vertical = 4.dp)
//                )
                Text(
                    text = studentEnrollment.courseCode,
                    fontSize = 18.sp,
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = studentEnrollment.courseName.uppercase(),
                    fontSize = 18.sp,
                )
            }

            Text(
                text = "Tipo ${studentEnrollment.courseType}",
                color = MyMutedForegroundColor,
                fontSize = 18.sp
            )
            Text(
                text = "Grupo ${studentEnrollment.groupType}",
                color = MyMutedForegroundColor,
                fontSize = 18.sp
            )
            Text(
                text = studentEnrollment.firstname + " " + studentEnrollment.surname,
                color = MyMutedForegroundColor,
                fontSize = 16.sp,
            )
            Text(
                text = studentEnrollment.email,
                color = MyMutedForegroundColor,
                fontSize = 18.sp,
            )


        }

    }
}
