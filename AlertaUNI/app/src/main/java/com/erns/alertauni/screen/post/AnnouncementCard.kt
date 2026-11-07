package com.erns.alertauni.screen.post

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CommentsDisabled
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erns.alertauni.domain.post.AnnouncementFilter
import com.erns.alertauni.domain.post.Priority
import com.erns.alertauni.ui.theme.AlertaUNITheme
import com.erns.alertauni.ui.theme.MyBorderColor
import com.erns.alertauni.ui.theme.MyForegroundColor
import com.erns.alertauni.ui.theme.MyMutedForegroundColor
import com.erns.alertauni.ui.theme.MyPriorityHighColor
import com.erns.alertauni.ui.theme.MyPriorityLowColor
import com.erns.alertauni.ui.theme.MyPriorityMediumColor
import com.erns.alertauni.ui.theme.MyPrimaryColor
import com.erns.alertauni.ui.theme.MySecondaryColor
import com.erns.alertauni.ui.theme.MySelectedColor
import com.erns.alertauni.ui.theme.MySurfaceColor

fun Priority.color(): Color = when (this) {
    Priority.ALTA -> MyPriorityHighColor
    Priority.MEDIA -> MyPriorityMediumColor
    Priority.BAJA -> MyPriorityLowColor
}

@Composable
fun AnnouncementCard(
    item: AnnouncementUi,
    query: String,
    canManageComments: Boolean,
    onOpen: () -> Unit,
    onToggleArchive: () -> Unit,
    onToggleComments: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MySurfaceColor),
        border = BorderStroke(1.dp, MyBorderColor),
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .clickable(onClickLabel = "Ver comentarios", onClick = onOpen)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(item.priority.color())
            )
            Column(
                modifier = Modifier.padding(start = 12.dp, top = 10.dp, end = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MetaRow(item, query)
                Text(
                    text = highlight(item.title, query),
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MyForegroundColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = highlight(item.content, query),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MyForegroundColor.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 8.dp)
                )
                BottomRow(item, canManageComments, onToggleArchive, onToggleComments)
            }
        }
    }
}

@Composable
private fun MetaRow(item: AnnouncementUi, query: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = highlight(item.courseCode, query),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MyPrimaryColor,
            modifier = Modifier
                .background(MySecondaryColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (item.isPrivate) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Privado",
                    tint = MyMutedForegroundColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = highlight(item.privateLabel, query),
                    fontSize = 12.sp,
                    color = MyMutedForegroundColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Groups,
                    contentDescription = "Público, todo el curso",
                    tint = MyMutedForegroundColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(
            text = item.dateLabel,
            fontSize = 12.sp,
            color = MyMutedForegroundColor,
            maxLines = 1
        )
    }
}

@Composable
private fun BottomRow(
    item: AnnouncementUi,
    canManageComments: Boolean,
    onToggleArchive: () -> Unit,
    onToggleComments: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(item.priority.color(), CircleShape)
            )
            Text(
                text = item.priority.label,
                fontSize = 12.sp,
                fontWeight = if (item.priority == Priority.ALTA) FontWeight.SemiBold else FontWeight.Normal,
                color = if (item.priority == Priority.ALTA) MyPriorityHighColor else MyMutedForegroundColor,
                modifier = Modifier.semantics { contentDescription = "Prioridad ${item.priority.label}" }
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (item.allowComments) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint = MyMutedForegroundColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = item.commentCount.toString(),
                    fontSize = 12.sp,
                    color = MyMutedForegroundColor,
                    modifier = Modifier.semantics {
                        contentDescription = "${item.commentCount} comentarios"
                    }
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.CommentsDisabled,
                    contentDescription = null,
                    tint = MyMutedForegroundColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(text = "Comentarios cerrados", fontSize = 12.sp, color = MyMutedForegroundColor)
            }
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Más acciones",
                    tint = MyMutedForegroundColor
                )
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                containerColor = MySurfaceColor
            ) {
                DropdownMenuItem(
                    text = { Text(if (item.isArchived) "Desarchivar" else "Archivar") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (item.isArchived) Icons.Outlined.Unarchive else Icons.Outlined.Archive,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuOpen = false
                        onToggleArchive()
                    }
                )
                if (canManageComments) {
                    DropdownMenuItem(
                        text = { Text(if (item.allowComments) "Desactivar comentarios" else "Activar comentarios") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (item.allowComments) {
                                    Icons.Outlined.CommentsDisabled
                                } else {
                                    Icons.Outlined.ChatBubbleOutline
                                },
                                contentDescription = null
                            )
                        },
                        onClick = {
                            menuOpen = false
                            onToggleComments()
                        }
                    )
                }
            }
        }
    }
}

/** Resalta las palabras buscadas (sin importar tildes ni mayúsculas). */
@Composable
private fun highlight(text: String, query: String): AnnotatedString = remember(text, query) {
    buildAnnotatedString {
        append(text)
        AnnouncementFilter.highlightRanges(text, query).forEach { range ->
            addStyle(
                SpanStyle(background = MySelectedColor, color = MyForegroundColor),
                range.first,
                range.last + 1
            )
        }
    }
}

private val previewItems = listOf(
    AnnouncementUi(
        id = 1, title = "Examen parcial reprogramado",
        content = "La evaluación de la unidad 2 pasa al lunes 8 en el aula 304. Traigan calculadora y su carné universitario.",
        courseCode = "INF-01", courseName = "Ingeniería de Software", isPrivate = false,
        recipientName = null, privateLabel = "Privado", priority = Priority.ALTA, allowComments = true,
        commentCount = 3, dateLabel = "hace 5 min", publishedAt = null, isArchived = false
    ),
    AnnouncementUi(
        id = 2, title = "Sobre tu trabajo final",
        content = "Revisa las observaciones de la evaluación antes del viernes.",
        courseCode = "INF-02", courseName = "Programación Web", isPrivate = true,
        recipientName = "Juan Pérez", privateLabel = "Para: Juan Pérez", priority = Priority.MEDIA,
        allowComments = true, commentCount = 0, dateLabel = "ayer 14:30", publishedAt = null, isArchived = false
    ),
    AnnouncementUi(
        id = 3, title = "Material de lectura",
        content = "Subí las diapositivas de la semana 4.",
        courseCode = "INF-01", courseName = "Ingeniería de Software", isPrivate = false,
        recipientName = null, privateLabel = "Privado", priority = Priority.BAJA, allowComments = false,
        commentCount = 0, dateLabel = "31 may", publishedAt = null, isArchived = false
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC, widthDp = 360)
@Composable
private fun AnnouncementCardPreview() {
    AlertaUNITheme(dynamicColor = false) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            previewItems.forEach { item ->
                AnnouncementCard(
                    item = item,
                    query = "evaluacion",
                    canManageComments = true,
                    onOpen = {},
                    onToggleArchive = {},
                    onToggleComments = {}
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
