package com.erns.alertauni.screen.post

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
    val postViewModel: PostViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val boardUiState = postViewModel.boardUiState.collectAsState()
    val catalogCourseState = postViewModel.catalogCourses.collectAsState()
    val username = postViewModel.username.collectAsState()
    val showAddDialog = rememberSaveable { mutableStateOf(false) }
    val searchQuery = remember { mutableStateOf("") }

    // Mensajes de un solo disparo emitidos por el ViewModel
    LaunchedEffect(Unit) {
        postViewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
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
                    message = "Debes tener al menos un curso a cargo para publicar un anuncio."
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        onFabActionReady(onClickFloatingActionButton)
    }

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
        boardUiState = boardUiState.value,
        onRefresh = { postViewModel.refresh() },
        onClickComment = onClickComment
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardLayout(
    username: String,
    title: String,
    searchQuery: MutableState<String>,
    boardUiState: PostViewModel.BoardUiState,
    onRefresh: () -> Unit,
    onClickComment: (String) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MySecondaryColor)
    ) {
        SearchBoxComponent(username, title)
        PullToRefreshBox(
            isRefreshing = boardUiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                boardUiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                boardUiState.posts.isEmpty() -> BoardMessage(
                    text = boardUiState.errorMessage
                        ?: "No hay anuncios todavía.\nDesliza hacia abajo para actualizar.",
                    isError = boardUiState.errorMessage != null
                )

                else -> AnnounceList(boardUiState.posts, boardUiState.errorMessage, onClickComment)
            }
        }
    }

}

@Composable
private fun BoardMessage(text: String, isError: Boolean) {
    // LazyColumn para que el gesto "pull to refresh" funcione también con la lista vacía
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Text(
                text = text,
                color = if (isError) MaterialTheme.colorScheme.error else MyMutedForegroundColor,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}


@Composable
fun AnnounceList(
    announcements: List<PostEntity>,
    errorMessage: String?,
    onClickComment: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (errorMessage != null) {
            item {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        items(announcements, key = { it.id }) { announce ->
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
            .padding(8.dp)
            .clickable(onClick = { onClickComment(announce.id.toString()) }),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MySurfaceColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                fontSize = 18.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
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
