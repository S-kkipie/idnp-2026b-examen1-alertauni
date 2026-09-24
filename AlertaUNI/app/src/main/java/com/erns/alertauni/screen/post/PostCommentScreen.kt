package com.erns.alertauni.screen.post

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.erns.alertauni.data.model.CommentEntity
import com.erns.alertauni.screen.common.SearchBoxComponent

@Composable
fun PostCommentScreen(
    viewModel: PostCommentViewModel = hiltViewModel(),
) {
    val comments = viewModel.comments.collectAsState()
    val username = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.username.collect {
            username.value = it
        }
    }

    val onClickAddComment: (String) -> Unit = { comment ->
        viewModel.sendComment(comment)
    }

    CommentScreenLayout(
        username.value,
        "Comentarios",
        comments.value,
        onClickAddComment
    )

}

@Composable
fun CommentScreenLayout(
    username: String,
    title: String,
    comments: List<CommentEntity>,
    onClickAddComment: (String) -> Unit
) {
    val inputText = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        SearchBoxComponent(username, title)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
                .weight(1f),
            reverseLayout = true // To show latest at bottom
        ) {
            items(comments.reversed()) { message ->
                MessageBubble(message)
            }

        }

        // Input area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            TextField(
                value = inputText.value,
                onValueChange = { inputText.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                // 1. Esquinas redondeadas estilo WhatsApp
                shape = RoundedCornerShape(28.dp),
                // 2. Eliminamos los indicadores visuales de "caja" de Material
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,   // Quita la línea al enfocar
                    unfocusedIndicatorColor = Color.Transparent, // Quita la línea al desenfocar
                    disabledIndicatorColor = Color.Transparent
                ),
                placeholder = {
                    Text("Mensaje", color = Color.Gray)
                },
//                leadingIcon = {
//                    Icon(Icons.Default.Face, contentDescription = "Emoji", tint = Color.Gray)
//                },
                trailingIcon = {
                    IconButton(onClick = {
                        if (inputText.value.isNotBlank()) {
                            onClickAddComment(inputText.value)
                            inputText.value = ""
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Outlined.Send,
                            contentDescription = "Enviar",
                            tint = Color.Gray,
                            modifier = Modifier.rotate(0f)
                        )
                    }
                },
                maxLines = 5 // Permite que crezca un poco antes de hacer scroll interno
            )

        }

    }


}

@Composable
fun MessageBubble(message: CommentEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (message.isMine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (message.isMine) Color(0xFFDCF8C6) else Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                if (!message.isMine) {
                    Text(
                        text = message.authorName,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = message.content,
                    textAlign = if (message.isMine) TextAlign.End else TextAlign.Start
                )
                Text(
                    text = message.publishedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.align(if (message.isMine) Alignment.End else Alignment.Start)
                )
            }
        }
    }
}
