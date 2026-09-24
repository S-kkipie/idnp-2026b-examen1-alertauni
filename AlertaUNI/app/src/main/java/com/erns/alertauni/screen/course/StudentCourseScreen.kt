package com.erns.alertauni.screen.course

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    //val scope = rememberCoroutineScope()
    val studentEnrollmentListState = viewModel.studentEnrollmentList.collectAsState()
    val showAddDialog = remember { mutableStateOf(false) }
    val studentEnrollment = remember { mutableStateOf<StudentEnrollment?>(null) }
    val username = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.username.collect {
            username.value = it
        }
    }

    LaunchedEffect(Unit) {
        viewModel.studentEnrollment.collect { value ->
            studentEnrollment.value = value
        }
    }

    LaunchedEffect(Unit) {
        viewModel.courseState.collect { value ->
            if (value == CourseViewModel.CourseState.Saved) {
                showAddDialog.value = false
            }
        }
    }

    val onClickFloatingActionButton: () -> Unit = {
        showAddDialog.value = true
    }

    onFabActionReady(onClickFloatingActionButton)

    val onClickFindCourse: (String) -> Unit = { searchCode ->
        viewModel.findCourse(searchCode)
    }

    val onClickCourseEnroll: (String) -> Unit = {
        viewModel.courseEnroll(it)
    }


    if (showAddDialog.value) {
        AddCourseDialog(
            studentEnrollment.value,
            onDismiss = {
                showAddDialog.value = false
                viewModel.clearEnrollment()
            },
            onClickFindCourse = onClickFindCourse,
            onClickCourseEnroll = onClickCourseEnroll
        )
    }

    StudentCourseScreenLayout(
        username.value,
        "Cursos",
        studentEnrollmentListState.value
    )
}

@Composable
fun StudentCourseScreenLayout(
    username: String,
    title: String,
    studentEnrollmentList: List<StudentEnrollment>
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SearchBoxComponent(username,title)
        LazyColumn {
            items(studentEnrollmentList) { studentEnrollment ->
                StudentEnrollmentCard(studentEnrollment)
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
