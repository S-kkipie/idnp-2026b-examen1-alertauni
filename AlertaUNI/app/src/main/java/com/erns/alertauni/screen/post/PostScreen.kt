package com.erns.alertauni.screen.post

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.erns.alertauni.R
import com.erns.alertauni.data.model.PostEntity
import com.erns.alertauni.screen.common.SearchBoxComponent
import com.erns.alertauni.screen.common.UserInitialCircle
import com.erns.alertauni.ui.theme.MyBorderColor
import com.erns.alertauni.ui.theme.MyMutedColor
import com.erns.alertauni.ui.theme.MyMutedForegroundColor
import com.erns.alertauni.ui.theme.MyPrimaryColor
import com.erns.alertauni.ui.theme.MySecondaryColor
import com.erns.alertauni.ui.theme.MySurfaceColor
import kotlinx.coroutines.launch

@Composable
fun PostScreen(
    modifier: Modifier = Modifier,
    onClickComment: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    onFabActionReady: (() -> Unit) -> Unit
) {
    val context = LocalContext.current
    val postViewModel: PostViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val announcementState = postViewModel.announcements.collectAsState()
    val catalogCourseState = postViewModel.catalogCourses.collectAsState()
    val showAddDialog = remember { mutableStateOf(false) }
    val searchQuery = remember { mutableStateOf("") }
    val username = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        postViewModel.username.collect {
            username.value = it
        }
    }

    val onClickNewPost: (String, String, String, String, String) -> Unit =
        { courseCatalogId, courseCode, courseName, titlePost, contentPost ->
            showAddDialog.value = false
            postViewModel.sendPost(
                courseCatalogId,
                titlePost,
                contentPost
            )
        }

    val onClickFloatingActionButton: () -> Unit = {
        if (catalogCourseState.value.isNotEmpty()) {
            showAddDialog.value = true
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Debes estar inscrito en al menos un curso para crear una notificación."
                )
            }
        }
    }

    onFabActionReady(onClickFloatingActionButton)

    if (showAddDialog.value && catalogCourseState.value.isNotEmpty()) {
        AddPostDialog(
            onDismiss = { showAddDialog.value = false },
            onClickNewPost = onClickNewPost,
            catalogCourses = catalogCourseState.value
        )
    }

    BoardLayout(
        username.value,
        "Anuncios",
        searchQuery = searchQuery,
        notificacionesFiltradas = announcementState.value,
        onClickComment
    )

}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BoardLayout(
    username: String,
    title: String,
    searchQuery: MutableState<String>,
    notificacionesFiltradas: List<PostEntity>,
    onClickComment: (String) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MySecondaryColor)
    ) {
        SearchBoxComponent(username, title)
        AnnounceList(notificacionesFiltradas, onClickComment)
    }

}


@Composable
fun AnnounceList(
    announcements: List<PostEntity>,
    onClickComment: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(announcements) { announce ->
            MessageCard(announce, onClickComment)
        }

    }
}


@Composable
fun MessageCard(
    announce: PostEntity,
    onClickComment: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(8.dp)
            .clickable(onClick = { onClickComment(announce.id.toString()) }),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MySurfaceColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            )
            {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = announce.courseCode,
                        color = MyMutedForegroundColor,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .background(
                                color = MyMutedColor,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "",
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text(
                        text = if (announce.isPrivate) "Privado" else "Público",
                        color = MyMutedForegroundColor,
                        fontSize = 16.sp,
                    )
                }
                Text(
                    text = announce.date,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                )
            }

            Text(
                text = announce.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = announce.content,
                fontSize = 18.sp
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MyBorderColor)
            )
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth(),
//            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            )
            {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    UserInitialCircle(announce.authorName ?: "X")
                    Text(
                        text = announce.authorName ?: "Desconocido",
                        color = MyMutedForegroundColor,
                        fontSize = 16.sp,
                        maxLines = Int.MAX_VALUE,
                        overflow = TextOverflow.Clip,
                        softWrap = true
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_chat_bubble_outline_24),
                        contentDescription = "",
                    )
                    Text(
                        text = announce.countComments.toString(),
                        color = MyPrimaryColor,
                        fontSize = 16.sp,
                    )
                    Text(
                        text = "Comentarios",
                        color = MyPrimaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

            }

        }

    }
}
