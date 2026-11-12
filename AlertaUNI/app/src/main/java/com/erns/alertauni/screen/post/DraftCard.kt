package com.erns.alertauni.screen.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lock
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erns.alertauni.domain.post.DraftRules
import com.erns.alertauni.domain.post.DraftSummary
import com.erns.alertauni.ui.theme.MyDraftBorderColor
import com.erns.alertauni.ui.theme.MyForegroundColor
import com.erns.alertauni.ui.theme.MyMutedColor
import com.erns.alertauni.ui.theme.MyMutedForegroundColor
import com.erns.alertauni.ui.theme.MyPriorityHighColor
import com.erns.alertauni.ui.theme.MyPrimaryColor
import com.erns.alertauni.ui.theme.MySecondaryColor

/** Borrador local: borde punteado y etiqueta «Borrador» para no confundirlo con un anuncio publicado. */
@Composable
fun DraftCard(
    draft: DraftSummary,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MyMutedColor)
            .drawBehind {
                val stroke = 1.5.dp.toPx()
                drawRoundRect(
                    color = MyDraftBorderColor,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(size.width - stroke, size.height - stroke),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)))
                )
            }
            .clickable(onClickLabel = "Seguir editando", onClick = onOpen)
            .padding(start = 14.dp, top = 10.dp, end = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .background(MySecondaryColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Outlined.EditNote, contentDescription = null, tint = MyPrimaryColor, modifier = Modifier.size(14.dp))
                Text("Borrador", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MyPrimaryColor)
            }
            Icon(
                imageVector = if (draft.isPrivate) Icons.Outlined.Lock else Icons.Outlined.Groups,
                contentDescription = null,
                tint = MyMutedForegroundColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = draft.targetLabel,
                fontSize = 12.sp,
                color = MyMutedForegroundColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = draft.title,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.SemiBold,
            fontStyle = if (draft.title == DraftRules.NO_TITLE) FontStyle.Italic else FontStyle.Normal,
            color = if (draft.title == DraftRules.NO_TITLE) MyMutedForegroundColor else MyForegroundColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = 8.dp)
        )
        if (draft.preview.isNotEmpty()) {
            Text(
                text = draft.preview,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MyForegroundColor.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        DraftBottomRow(draft, onOpen, onDelete)
    }
}

@Composable
private fun DraftBottomRow(draft: DraftSummary, onOpen: () -> Unit, onDelete: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    val priority = draft.form.priority
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).background(priority.color(), CircleShape))
            Text(
                text = priority.label,
                fontSize = 12.sp,
                color = MyMutedForegroundColor,
                modifier = Modifier.semantics { contentDescription = "Prioridad ${priority.label}" }
            )
        }
        Text(
            text = draft.editedLabel,
            fontSize = 12.sp,
            color = MyMutedForegroundColor,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Acciones del borrador", tint = MyMutedForegroundColor)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Seguir editando") },
                    leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                    onClick = {
                        menuOpen = false
                        onOpen()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Eliminar borrador", color = MyPriorityHighColor) },
                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MyPriorityHighColor) },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    }
                )
            }
        }
    }
}
