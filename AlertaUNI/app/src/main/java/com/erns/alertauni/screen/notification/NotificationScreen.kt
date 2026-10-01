package com.erns.alertauni.screen.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.erns.alertauni.data.model.NotificacionEntity
import com.erns.alertauni.screen.common.SearchBoxComponent
import com.erns.alertauni.screen.notification.NotificationViewModel.NotificationUiState
import com.erns.alertauni.ui.theme.MyMutedForegroundColor
import com.erns.alertauni.ui.theme.MyPrimaryColor
import com.erns.alertauni.ui.theme.MySurfaceColor

@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState()
    val username = viewModel.username.collectAsState()
    val showConfirmClear = rememberSaveable { mutableStateOf(false) }

    if (showConfirmClear.value) {
        AlertDialog(
            onDismissRequest = { showConfirmClear.value = false },
            title = { Text("Limpiar avisos") },
            text = { Text("Se eliminarán todos los avisos guardados en este dispositivo.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAll()
                    showConfirmClear.value = false
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClear.value = false }) { Text("Cancelar") }
            }
        )
    }

    NotificationScreenLayout(
        username = username.value,
        title = "Avisos",
        uiState = uiState.value,
        onClickDelete = { viewModel.deleteNotification(it) },
        onClickClearAll = { showConfirmClear.value = true }
    )
}

@Composable
fun NotificationScreenLayout(
    username: String,
    title: String,
    uiState: NotificationUiState,
    onClickDelete: (Int) -> Unit,
    onClickClearAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SearchBoxComponent(username, title)
        when (uiState) {
            NotificationUiState.Loading -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            NotificationUiState.Empty -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tienes avisos.\nAquí aparecerán las notificaciones de tus cursos.",
                    textAlign = TextAlign.Center,
                    color = MyMutedForegroundColor,
                    fontSize = 16.sp
                )
            }

            is NotificationUiState.Content -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.notifications.size} avisos",
                        color = MyMutedForegroundColor
                    )
                    TextButton(onClick = onClickClearAll) { Text("Limpiar todo") }
                }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.notifications, key = { it.id }) { notification ->
                        NotificationCard(notification, onClickDelete)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: NotificacionEntity,
    onClickDelete: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MySurfaceColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MyPrimaryColor,
                modifier = Modifier
                    .padding(top = 2.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = notification.cursoId.ifBlank { "Aviso" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = notification.mensaje, fontSize = 15.sp)
                Text(
                    text = notification.fecha,
                    fontSize = 12.sp,
                    color = MyMutedForegroundColor
                )
            }
            IconButton(onClick = { onClickDelete(notification.id) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar aviso",
                    tint = MyMutedForegroundColor
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE0E7FF)
@Composable
private fun NotificationScreenContentPreview() {
    NotificationScreenLayout(
        username = "Ana Quispe",
        title = "Avisos",
        uiState = NotificationUiState.Content(
            listOf(
                NotificacionEntity(1, "IDNP - Nuevo anuncio", "Se publicó el enunciado del Examen 1", "2026-09-29 10:36:00"),
                NotificacionEntity(2, "IDNP - Comentario", "El docente respondió tu consulta", "2026-09-28 18:02:10")
            )
        ),
        onClickDelete = {},
        onClickClearAll = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFE0E7FF)
@Composable
private fun NotificationScreenEmptyPreview() {
    NotificationScreenLayout("Ana Quispe", "Avisos", NotificationUiState.Empty, {}, {})
}
