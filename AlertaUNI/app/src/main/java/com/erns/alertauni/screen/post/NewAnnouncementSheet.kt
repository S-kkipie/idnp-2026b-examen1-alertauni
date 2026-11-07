package com.erns.alertauni.screen.post

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erns.alertauni.data.model.CatalogCourseEntity
import com.erns.alertauni.domain.post.AnnouncementFilter
import com.erns.alertauni.domain.post.Audience
import com.erns.alertauni.domain.post.Priority
import com.erns.alertauni.ui.theme.MyBorderColor
import com.erns.alertauni.ui.theme.MyForegroundColor
import com.erns.alertauni.ui.theme.MyMutedForegroundColor
import com.erns.alertauni.ui.theme.MyPriorityHighColor
import com.erns.alertauni.ui.theme.MyPrimaryColor
import com.erns.alertauni.ui.theme.MyPrimaryForegroundColor
import com.erns.alertauni.ui.theme.MySecondaryColor
import com.erns.alertauni.ui.theme.MySurfaceColor
import kotlinx.coroutines.launch

private const val TITLE_MAX = 80
private const val CONTENT_MAX = 500
private const val MAX_RESULTS = 5

private enum class RecipientMode { COURSE, STUDENT }

/**
 * Hoja "Nuevo anuncio". Los campos siguen el orden de decisión: a quién, prioridad, texto.
 * Sólo el docente ve la opción "Un estudiante".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAnnouncementSheet(
    courses: List<CatalogCourseEntity>,
    students: List<StudentOption>,
    canSendPrivate: Boolean,
    audienceFor: (courseCatalogId: String, recipient: StudentOption?) -> Audience,
    onSend: (AnnouncementDraft) -> Unit,
    onDismiss: () -> Unit,
) {
    var mode by rememberSaveable { mutableStateOf(RecipientMode.COURSE) }
    var courseId by rememberSaveable { mutableStateOf(courses.singleOrNull()?.id) }
    var recipientKey by rememberSaveable { mutableStateOf<String?>(null) }
    var priority by rememberSaveable { mutableStateOf(Priority.MEDIA) }
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    var allowComments by rememberSaveable { mutableStateOf(true) }
    var showDiscard by remember { mutableStateOf(false) }
    var closing by remember { mutableStateOf(false) }

    val recipient = students.firstOrNull { it.key == recipientKey }
    val hasText = title.isNotBlank() || content.isNotBlank()
    val hasTextState = rememberUpdatedState(hasText)

    val scope = rememberCoroutineScope()
    // Deslizar hacia abajo o tocar fuera con texto escrito pide confirmación
    val confirmHide: (SheetValue) -> Boolean = remember {
        { value ->
            if (value == SheetValue.Hidden && hasTextState.value && !closing) {
                showDiscard = true
                false
            } else {
                true
            }
        }
    }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = confirmHide
    )

    fun close() {
        closing = true
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    fun requestClose() {
        if (hasText) showDiscard = true else close()
    }

    val target: Pair<String, StudentOption?>? = when (mode) {
        RecipientMode.COURSE -> courseId?.let { it to null }
        RecipientMode.STUDENT -> recipient?.let { it.courseCatalogId to it }
    }
    val canSend = target != null && title.isNotBlank() && content.isNotBlank()
    val sendLabel = when {
        target != null -> audienceFor(target.first, target.second).sendLabel
        mode == RecipientMode.STUDENT -> "Elige un estudiante"
        else -> "Elige un curso"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MySurfaceColor,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false)
    ) {
        BackHandler { requestClose() }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Nuevo anuncio",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MyForegroundColor,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { requestClose() }) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = MyMutedForegroundColor)
                }
            }

            SectionLabel("Para")
            if (canSendPrivate) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = mode == RecipientMode.COURSE,
                        onClick = { mode = RecipientMode.COURSE },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = segmentedColors(),
                        icon = { Icon(Icons.Outlined.Groups, contentDescription = null, Modifier.size(18.dp)) }
                    ) { Text("Todo un curso", maxLines = 1) }
                    SegmentedButton(
                        selected = mode == RecipientMode.STUDENT,
                        onClick = { mode = RecipientMode.STUDENT },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = segmentedColors(),
                        icon = { Icon(Icons.Outlined.Person, contentDescription = null, Modifier.size(18.dp)) }
                    ) { Text("Un estudiante", maxLines = 1) }
                }
            }
            if (mode == RecipientMode.COURSE) {
                CourseDropdown(courses = courses, selectedId = courseId, onSelect = { courseId = it })
            } else {
                StudentPicker(
                    students = students,
                    selected = recipient,
                    onSelect = { recipientKey = it?.key }
                )
            }

            SectionLabel("Prioridad")
            val priorities = listOf(Priority.BAJA, Priority.MEDIA, Priority.ALTA)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                priorities.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = priority == option,
                        onClick = { priority = option },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = priorities.size),
                        colors = segmentedColors(),
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(option.color(), CircleShape)
                            )
                        }
                    ) { Text(option.label) }
                }
            }
            Text(text = priority.helper, fontSize = 12.sp, color = MyMutedForegroundColor)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it.take(TITLE_MAX) },
                label = { Text("Título") },
                singleLine = true,
                supportingText = { Counter(title.length, TITLE_MAX) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it.take(CONTENT_MAX) },
                label = { Text("Contenido") },
                minLines = 4,
                maxLines = 8,
                supportingText = { Counter(content.length, CONTENT_MAX) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = allowComments,
                        role = Role.Switch,
                        onValueChange = { allowComments = it }
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Permitir comentarios", fontSize = 15.sp, color = MyForegroundColor)
                    Text(
                        text = if (allowComments) "Podrán responder en el anuncio" else "Nadie podrá responder",
                        fontSize = 12.sp,
                        color = MyMutedForegroundColor
                    )
                }
                Switch(
                    checked = allowComments,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MyPrimaryColor,
                        checkedThumbColor = MyPrimaryForegroundColor
                    )
                )
            }

            Button(
                onClick = {
                    val (catalogId, student) = target ?: return@Button
                    onSend(
                        AnnouncementDraft(
                            courseCatalogId = catalogId,
                            title = title.trim(),
                            content = content.trim(),
                            priority = priority,
                            allowComments = allowComments,
                            recipient = student
                        )
                    )
                    close()
                },
                enabled = canSend,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MyPrimaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = sendLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 8.dp, top = 6.dp, bottom = 6.dp)
                )
            }
        }

        if (showDiscard) {
            AlertDialog(
                onDismissRequest = { showDiscard = false },
                containerColor = MySurfaceColor,
                title = { Text("¿Descartar anuncio?") },
                text = { Text("Se perderá lo que escribiste.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDiscard = false
                        close()
                    }) { Text("Descartar", color = MyPriorityHighColor) }
                },
                dismissButton = {
                    TextButton(onClick = { showDiscard = false }) {
                        Text("Seguir editando", color = MyPrimaryColor)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseDropdown(
    courses: List<CatalogCourseEntity>,
    selectedId: String?,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = courses.firstOrNull { it.id == selectedId }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.let { "${it.courseCode} · ${it.courseName}" }.orEmpty(),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Curso") },
            placeholder = { Text("Elige un curso") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = fieldColors(),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MySurfaceColor
        ) {
            courses.forEach { course ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(course.courseCode, fontWeight = FontWeight.SemiBold)
                            Text(course.courseName, fontSize = 12.sp, color = MyMutedForegroundColor)
                        }
                    },
                    onClick = {
                        onSelect(course.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

/** Buscador único sobre los estudiantes de todos los cursos del docente. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentPicker(
    students: List<StudentOption>,
    selected: StudentOption?,
    onSelect: (StudentOption?) -> Unit,
) {
    if (selected != null) {
        InputChip(
            selected = true,
            onClick = { onSelect(null) },
            label = { Text("${selected.shortName} · ${selected.courseCode}") },
            avatar = { Initials(selected.fullName, size = 24) },
            trailingIcon = {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Quitar destinatario",
                    modifier = Modifier.size(InputChipDefaults.IconSize)
                )
            },
            colors = InputChipDefaults.inputChipColors(
                selectedContainerColor = MySecondaryColor,
                selectedLabelColor = MyPrimaryColor,
                selectedTrailingIconColor = MyPrimaryColor
            )
        )
        return
    }

    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(query, students) {
        val terms = AnnouncementFilter.terms(query)
        if (terms.isEmpty()) {
            emptyList()
        } else {
            students
                .filter { s ->
                    val haystack = AnnouncementFilter.normalize("${s.fullName} ${s.code} ${s.courseCode}")
                    terms.all { haystack.contains(it) }
                }
                .sortedBy { it.fullName }
                .take(MAX_RESULTS)
        }
    }

    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        placeholder = { Text("Buscar estudiante por nombre o CUI") },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        singleLine = true,
        colors = fieldColors(),
        modifier = Modifier.fillMaxWidth()
    )
    when {
        students.isEmpty() -> Hint("Aún no hay estudiantes inscritos en tus cursos.")
        query.isBlank() -> Hint("Busca entre los ${students.distinctBy { it.studentId }.size} estudiantes de tus cursos.")
        results.isEmpty() -> Hint("Ningún estudiante coincide con «${query.trim()}».")
        else -> Column {
            results.forEachIndexed { index, student ->
                if (index > 0) HorizontalDivider(color = MyBorderColor)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClickLabel = "Elegir destinatario") { onSelect(student) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Initials(student.fullName, size = 36)
                    Column {
                        Text(student.fullName, fontSize = 14.sp, color = MyForegroundColor)
                        Text(
                            text = "${student.code} · ${student.courseCode}",
                            fontSize = 12.sp,
                            color = MyMutedForegroundColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Initials(name: String, size: Int) {
    val initials = name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1) }.uppercase()
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(MySecondaryColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            fontSize = (size * 0.38f).sp,
            fontWeight = FontWeight.SemiBold,
            color = MyPrimaryColor
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MyMutedForegroundColor)
}

@Composable
private fun Hint(text: String) {
    Text(text = text, fontSize = 12.sp, color = MyMutedForegroundColor)
}

@Composable
private fun Counter(length: Int, max: Int) {
    Text(text = "$length/$max", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun segmentedColors() = SegmentedButtonDefaults.colors(
    activeContainerColor = MySecondaryColor,
    activeContentColor = MyPrimaryColor,
    activeBorderColor = MyPrimaryColor,
    inactiveContainerColor = MySurfaceColor,
    inactiveContentColor = MyForegroundColor,
    inactiveBorderColor = MyBorderColor
)

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MyPrimaryColor,
    unfocusedBorderColor = MyBorderColor,
    focusedLabelColor = MyPrimaryColor,
    cursorColor = MyPrimaryColor
)
