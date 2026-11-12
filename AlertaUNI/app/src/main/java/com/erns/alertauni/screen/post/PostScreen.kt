package com.erns.alertauni.screen.post

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.erns.alertauni.data.demo.DemoAnnouncements
import com.erns.alertauni.domain.post.DraftSummary
import com.erns.alertauni.domain.post.TypeFilter
import com.erns.alertauni.ui.theme.MyBackgroundColor
import com.erns.alertauni.ui.theme.MyBorderColor
import com.erns.alertauni.ui.theme.MyForegroundColor
import com.erns.alertauni.ui.theme.MyMutedForegroundColor
import com.erns.alertauni.ui.theme.MyPrimaryColor
import com.erns.alertauni.ui.theme.MyPrimaryForegroundColor
import com.erns.alertauni.ui.theme.MyPriorityHighColor
import com.erns.alertauni.ui.theme.MySecondaryColor
import com.erns.alertauni.ui.theme.MySimulatedColor
import com.erns.alertauni.ui.theme.MySimulatedForegroundColor
import com.erns.alertauni.ui.theme.MySurfaceColor
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

/** Acciones del muro. Por defecto no hacen nada (vistas previas y capturas de pantalla). */
class PostActions(
    val onSearchChange: (String) -> Unit = {},
    val onToggleType: (TypeFilter) -> Unit = {},
    val onToggleHigh: () -> Unit = {},
    val onToggleCourse: (String) -> Unit = {},
    val onClearFilters: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onShowView: (WallView) -> Unit = {},
    val onOpen: (AnnouncementUi) -> Unit = {},
    val onToggleArchive: (AnnouncementUi) -> Unit = {},
    val onToggleComments: (AnnouncementUi) -> Unit = {},
    val onOpenDraft: (DraftSummary) -> Unit = {},
    val onDeleteDraft: (DraftSummary) -> Unit = {},
    val onSetDemoData: (Boolean) -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    modifier: Modifier = Modifier,
    onClickComment: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    onFabActionReady: (() -> Unit) -> Unit,
    onFabExpandedChange: (Boolean) -> Unit = {}
) {
    val postViewModel: PostViewModel = hiltViewModel()
    val state by postViewModel.uiState.collectAsState()
    val catalogCourses by postViewModel.catalogCourses.collectAsState()
    val students by postViewModel.students.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var showComposer by rememberSaveable { mutableStateOf(false) }
    var editingDraftId by rememberSaveable { mutableStateOf<Long?>(null) }

    fun openComposer(draftId: Long?) {
        if (catalogCourses.isNotEmpty()) {
            editingDraftId = draftId
            showComposer = true
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Debes estar inscrito en al menos un curso para crear un anuncio."
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        onFabActionReady { openComposer(null) }
    }

    val fabExpanded = rememberFabExpanded(listState)
    LaunchedEffect(fabExpanded) { onFabExpandedChange(fabExpanded) }

    // Al cambiar la búsqueda, los filtros o la vista: barra visible y lista desde arriba
    LaunchedEffect(Unit) {
        snapshotFlow { state.query to state.wallView }
            .drop(1)
            .collect {
                scrollBehavior.state.heightOffset = 0f
                listState.scrollToItem(0)
            }
    }

    LaunchedEffect(Unit) {
        postViewModel.events.collect { event ->
            snackbarHostState.currentSnackbarData?.dismiss()
            scope.launch {
                when (event) {
                    is PostEvent.Sent -> snackbarHostState.showSnackbar(event.message)

                    is PostEvent.SendFailed -> {
                        val result = snackbarHostState.showSnackbar(
                            message = "No se pudo enviar el anuncio",
                            actionLabel = "Reintentar",
                            duration = SnackbarDuration.Long
                        )
                        if (result == SnackbarResult.ActionPerformed) postViewModel.send(event.draft)
                    }

                    is PostEvent.ArchivedChanged -> {
                        val result = snackbarHostState.showSnackbar(
                            message = if (event.archived) "Anuncio archivado" else "Anuncio desarchivado",
                            actionLabel = "Deshacer",
                            duration = SnackbarDuration.Long
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            postViewModel.setArchived(event.postId, !event.archived, notify = false)
                        }
                    }

                    // El backend aún no guarda este cambio: se aclara que es local
                    is PostEvent.CommentsChanged -> snackbarHostState.showSnackbar(
                        if (event.enabled) {
                            "Comentarios activados en este dispositivo"
                        } else {
                            "Comentarios desactivados en este dispositivo"
                        }
                    )

                    is PostEvent.DraftSaved -> {
                        val result = snackbarHostState.showSnackbar(
                            message = if (event.updated) "Borrador actualizado" else "Borrador guardado en este dispositivo",
                            actionLabel = "Ver"
                        )
                        if (result == SnackbarResult.ActionPerformed) postViewModel.setView(WallView.DRAFTS)
                    }

                    PostEvent.DraftNotSaved -> snackbarHostState.showSnackbar("No se pudo guardar el borrador")

                    is PostEvent.DraftDeleted -> {
                        val result = snackbarHostState.showSnackbar(
                            message = "Borrador eliminado",
                            actionLabel = "Deshacer",
                            duration = SnackbarDuration.Long
                        )
                        if (result == SnackbarResult.ActionPerformed) postViewModel.undoDeleteDraft()
                    }

                    is PostEvent.DemoDataChanged -> snackbarHostState.showSnackbar(
                        if (event.enabled) {
                            "${event.count} anuncios simulados en el muro (no van al servidor)"
                        } else {
                            "Datos de prueba ocultos"
                        }
                    )
                }
            }
        }
    }

    BackHandler(enabled = state.wallView != WallView.ACTIVE) { postViewModel.setView(WallView.ACTIVE) }

    if (showComposer && catalogCourses.isNotEmpty()) {
        val editing = editingDraftId?.let { id -> state.drafts.firstOrNull { it.id == id }?.form }
        NewAnnouncementSheet(
            courses = catalogCourses,
            students = students,
            canSendPrivate = state.isTeacher,
            audienceFor = postViewModel::audienceFor,
            onSend = postViewModel::send,
            onDismiss = {
                showComposer = false
                editingDraftId = null
            },
            initial = editing,
            onSaveDraft = if (state.canUseDrafts) postViewModel::saveDraft else null
        )
    }

    PostContent(
        state = state,
        modifier = modifier,
        listState = listState,
        scrollBehavior = scrollBehavior,
        actions = PostActions(
            onSearchChange = postViewModel::onSearchChange,
            onToggleType = postViewModel::toggleType,
            onToggleHigh = postViewModel::toggleOnlyHigh,
            onToggleCourse = postViewModel::toggleCourse,
            onClearFilters = postViewModel::clearFilters,
            onRetry = postViewModel::refresh,
            onShowView = postViewModel::setView,
            onOpen = { item ->
                if (item.isSimulated) {
                    // Simulado: no existe en el servidor, no hay comentarios que abrir
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar("Anuncio simulado: no tiene comentarios en el servidor")
                    }
                } else {
                    onClickComment(item.id.toString())
                }
            },
            onToggleArchive = { postViewModel.setArchived(it.id, !it.isArchived) },
            onToggleComments = { postViewModel.setCommentsEnabled(it.id, !it.allowComments) },
            onOpenDraft = { openComposer(it.id) },
            onDeleteDraft = { postViewModel.deleteDraft(it.id) },
            onSetDemoData = postViewModel::setDemoDataEnabled,
        )
    )
}

/** Muro sin ViewModel: sólo dibuja [state] y avisa las acciones. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostContent(
    state: PostUiState,
    actions: PostActions,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
) {
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MyBackgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            PostTopBar(
                state = state,
                scrollBehavior = scrollBehavior,
                onShowView = actions.onShowView,
                onSetDemoData = actions.onSetDemoData
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (state.showDrafts) {
                DraftList(drafts = state.drafts, listState = listState, actions = actions)
                return@Column
            }
            SearchField(
                text = state.query.text,
                onTextChange = actions.onSearchChange
            )
            FilterRow(
                state = state,
                onToggleType = actions.onToggleType,
                onToggleHigh = actions.onToggleHigh,
                onToggleCourse = actions.onToggleCourse,
                onOpenDrafts = { actions.onShowView(WallView.DRAFTS) }
            )
            if (state.demoEnabled) {
                DemoBanner(count = state.demoCount, onHide = { actions.onSetDemoData(false) })
            }
            if (state.query.isActive && state.totalInView > 0) {
                ResultsRow(count = state.resultCount, onClear = actions.onClearFilters)
            }
            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.isLoading && state.totalInView == 0 -> CircularProgressIndicator(
                        color = MyPrimaryColor,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    state.totalInView == 0 -> NoAnnouncements(state, onRetry = actions.onRetry)

                    state.resultCount == 0 -> EmptyState(
                        icon = Icons.Outlined.SearchOff,
                        title = if (state.query.text.isNotBlank()) {
                            "No hay anuncios que coincidan con «${state.query.text.trim()}»"
                        } else {
                            "No hay anuncios con estos filtros"
                        },
                        message = "Prueba con otras palabras o quita algún filtro.",
                        actionLabel = "Limpiar búsqueda y filtros",
                        onAction = actions.onClearFilters
                    )

                    else -> AnnouncementList(
                        state = state,
                        listState = listState,
                        onOpen = actions.onOpen,
                        onToggleArchive = actions.onToggleArchive,
                        onToggleComments = actions.onToggleComments
                    )
                }
            }
        }
    }
}

/** Extendido arriba de la lista o al volver a subir; sólo ícono al bajar. */
@Composable
private fun rememberFabExpanded(listState: LazyListState): Boolean {
    var expanded by remember { mutableStateOf(true) }
    LaunchedEffect(listState) {
        var lastIndex = listState.firstVisibleItemIndex
        var lastOffset = listState.firstVisibleItemScrollOffset
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                val atTop = index == 0 && offset == 0
                val scrollingUp = index < lastIndex || (index == lastIndex && offset < lastOffset)
                expanded = atTop || scrollingUp
                lastIndex = index
                lastOffset = offset
            }
    }
    return expanded
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostTopBar(
    state: PostUiState,
    scrollBehavior: TopAppBarScrollBehavior,
    onShowView: (WallView) -> Unit,
    onSetDemoData: (Boolean) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    var showSources by rememberSaveable { mutableStateOf(false) }
    TopAppBar(
        title = {
            Text(
                text = when (state.wallView) {
                    WallView.ACTIVE -> "Anuncios"
                    WallView.ARCHIVED -> "Archivados"
                    WallView.DRAFTS -> "Borradores"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            if (state.wallView != WallView.ACTIVE) {
                IconButton(onClick = { onShowView(WallView.ACTIVE) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver a anuncios")
                }
            }
        },
        actions = {
            if (state.wallView == WallView.ACTIVE) {
                IconButton(onClick = { onShowView(WallView.ARCHIVED) }) {
                    BadgedBox(
                        badge = {
                            if (state.archivedCount > 0) {
                                Badge(containerColor = MySecondaryColor, contentColor = MyPrimaryColor) {
                                    Text(state.archivedCount.toString())
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Outlined.Archive, contentDescription = "Ver archivados")
                    }
                }
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones")
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    containerColor = MySurfaceColor
                ) {
                    if (state.isTeacher) {
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("Datos de prueba (simulados)")
                                    Text(
                                        text = "${DemoAnnouncements.count} anuncios locales, no van al servidor",
                                        fontSize = 12.sp,
                                        color = MyMutedForegroundColor
                                    )
                                }
                            },
                            leadingIcon = { Icon(Icons.Outlined.Science, contentDescription = null) },
                            trailingIcon = {
                                Switch(
                                    checked = state.demoEnabled,
                                    onCheckedChange = null,
                                    colors = SwitchDefaults.colors(
                                        checkedTrackColor = MyPrimaryColor,
                                        checkedThumbColor = MyPrimaryForegroundColor
                                    )
                                )
                            },
                            onClick = {
                                menuOpen = false
                                onSetDemoData(!state.demoEnabled)
                            }
                        )
                        HorizontalDivider(color = MyBorderColor)
                    }
                    DropdownMenuItem(
                        text = { Text("Origen de los datos") },
                        leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            showSources = true
                        }
                    )
                }
            }
        },
        windowInsets = WindowInsets(0, 0, 0, 0),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MyPrimaryColor,
            scrolledContainerColor = MyPrimaryColor,
            titleContentColor = MyPrimaryForegroundColor,
            navigationIconContentColor = MyPrimaryForegroundColor,
            actionIconContentColor = MyPrimaryForegroundColor
        ),
        scrollBehavior = scrollBehavior
    )
    if (showSources) DataSourcesDialog(onDismiss = { showSources = false })
}

/** Qué viene del backend, qué se guarda en el dispositivo y qué es simulado. */
@Composable
private fun DataSourcesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MySurfaceColor,
        title = { Text("Origen de los datos") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SourceRow(
                    icon = Icons.Outlined.Cloud,
                    title = "Servidor (Supabase)",
                    text = "Anuncios publicados, envío público y privado, comentarios y estudiantes de tus cursos."
                )
                SourceRow(
                    icon = Icons.Outlined.PhoneAndroid,
                    title = "Este dispositivo (Room)",
                    text = "Borradores, prioridad, archivado y activar o desactivar comentarios. " +
                            "Se guardan por cuenta y siguen al cerrar la app."
                )
                SourceRow(
                    icon = Icons.Outlined.Science,
                    title = "Simulado",
                    text = "Anuncios de prueba con la etiqueta «Simulado». No se leen ni se envían al servidor."
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Entendido", color = MyPrimaryColor) }
        }
    )
}

@Composable
private fun SourceRow(icon: ImageVector, title: String, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = MyPrimaryColor, modifier = Modifier.size(22.dp))
        Column {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MyForegroundColor)
            Text(text, fontSize = 13.sp, color = MyMutedForegroundColor)
        }
    }
}

@Composable
private fun DemoBanner(count: Int, onHide: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 4.dp)
            .background(MySimulatedColor, RoundedCornerShape(8.dp))
            .padding(start = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Outlined.Science,
            contentDescription = null,
            tint = MySimulatedForegroundColor,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "Datos de prueba: $count anuncios simulados, no van al servidor.",
            fontSize = 12.sp,
            color = MySimulatedForegroundColor,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onHide) {
            Text("Ocultar", color = MySimulatedForegroundColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SearchField(text: String, onTextChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 12.dp),
        placeholder = { Text("Buscar en anuncios") },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(onClick = { onTextChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Borrar búsqueda")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MySurfaceColor,
            unfocusedContainerColor = MySurfaceColor,
            focusedBorderColor = MyPrimaryColor,
            unfocusedBorderColor = MyBorderColor,
            cursorColor = MyPrimaryColor
        )
    )
}

@Composable
private fun FilterRow(
    state: PostUiState,
    onToggleType: (TypeFilter) -> Unit,
    onToggleHigh: () -> Unit,
    onToggleCourse: (String) -> Unit,
    onOpenDrafts: () -> Unit,
) {
    val query = state.query
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Los borradores van aparte: nunca se mezclan con los anuncios publicados
        if (state.draftCount > 0 && state.wallView == WallView.ACTIVE) {
            item(key = "drafts") {
                WallFilterChip("Borradores (${state.draftCount})", false, onOpenDrafts) {
                    Icon(Icons.Outlined.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
        item {
            WallFilterChip("Públicos", query.type == TypeFilter.PUBLIC, { onToggleType(TypeFilter.PUBLIC) }) {
                Icon(Icons.Outlined.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
        item {
            WallFilterChip("Privados", query.type == TypeFilter.PRIVATE, { onToggleType(TypeFilter.PRIVATE) }) {
                Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
        item {
            WallFilterChip("Prioridad alta", query.onlyHigh, onToggleHigh) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MyPriorityHighColor, CircleShape)
                )
            }
        }
        items(state.courseCodes, key = { it }) { code ->
            WallFilterChip(code, query.courseCode == code, { onToggleCourse(code) })
        }
    }
}

@Composable
private fun WallFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MySurfaceColor,
            labelColor = MyForegroundColor,
            iconColor = MyMutedForegroundColor,
            selectedContainerColor = MySecondaryColor,
            selectedLabelColor = MyPrimaryColor,
            selectedLeadingIconColor = MyPrimaryColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MyBorderColor,
            selectedBorderColor = MyPrimaryColor
        )
    )
}

@Composable
private fun ResultsRow(count: Int, onClear: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (count == 1) "1 resultado" else "$count resultados",
            fontSize = 13.sp,
            color = MyMutedForegroundColor,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onClear) {
            Text("Limpiar", color = MyPrimaryColor)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AnnouncementList(
    state: PostUiState,
    listState: LazyListState,
    onOpen: (AnnouncementUi) -> Unit,
    onToggleArchive: (AnnouncementUi) -> Unit,
    onToggleComments: (AnnouncementUi) -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        // Espacio para que el FAB nunca tape la última tarjeta
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        state.sections.forEach { section ->
            stickyHeader(key = "header-${section.label}") {
                DateHeader(section.label)
            }
            items(section.items, key = { it.id }) { item ->
                SwipeToArchive(
                    item = item,
                    archivedView = state.showArchived,
                    onSwiped = { onToggleArchive(item) },
                    modifier = Modifier.animateItem()
                ) {
                    AnnouncementCard(
                        item = item,
                        query = state.query.text,
                        canManageComments = state.isTeacher,
                        onOpen = { onOpen(item) },
                        onToggleArchive = { onToggleArchive(item) },
                        onToggleComments = { onToggleComments(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateHeader(label: String) {
    Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MyMutedForegroundColor,
        modifier = Modifier
            .fillMaxWidth()
            .background(MyBackgroundColor)
            .padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
            .semantics { heading() }
    )
}

/** Deslizar a la izquierda archiva (o desarchiva en la vista de archivados). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToArchive(
    item: AnnouncementUi,
    archivedView: Boolean,
    onSwiped: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(positionalThreshold = { it * 0.35f })
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) onSwiped()
    }
    // Si se deshace antes de que termine la animación de salida, vuelve a su sitio
    LaunchedEffect(item.isArchived) {
        if (item.isArchived == archivedView && dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val active = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (active) MyPrimaryColor else Color.Transparent)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (archivedView) "Desarchivar" else "Archivar",
                    color = MyPrimaryForegroundColor,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (archivedView) Icons.Outlined.Unarchive else Icons.Outlined.Archive,
                    contentDescription = null,
                    tint = MyPrimaryForegroundColor
                )
            }
        },
        content = content
    )
}

@Composable
private fun DraftList(drafts: List<DraftSummary>, listState: LazyListState, actions: PostActions) {
    if (drafts.isEmpty()) {
        EmptyState(
            icon = Icons.Outlined.EditNote,
            title = "No tienes borradores",
            message = "Al escribir un anuncio, toca «Guardar borrador» para terminarlo después."
        )
        return
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "drafts-info") {
            Row(
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Outlined.PhoneAndroid,
                    contentDescription = null,
                    tint = MyMutedForegroundColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Guardados sólo en este dispositivo, para tu cuenta. No se publican hasta que los envíes.",
                    fontSize = 13.sp,
                    color = MyMutedForegroundColor
                )
            }
        }
        items(drafts, key = { it.id }) { draft ->
            SwipeToDelete(onSwiped = { actions.onDeleteDraft(draft) }) {
                DraftCard(
                    draft = draft,
                    onOpen = { actions.onOpenDraft(draft) },
                    onDelete = { actions.onDeleteDraft(draft) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDelete(onSwiped: () -> Unit, content: @Composable RowScope.() -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(positionalThreshold = { it * 0.35f })
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) onSwiped()
    }
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val active = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (active) MyPriorityHighColor else Color.Transparent)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Eliminar", color = MyPrimaryForegroundColor, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Outlined.Delete, contentDescription = null, tint = MyPrimaryForegroundColor)
            }
        },
        content = content
    )
}

@Composable
private fun NoAnnouncements(state: PostUiState, onRetry: () -> Unit) {
    when {
        state.showArchived -> EmptyState(
            icon = Icons.Outlined.Archive,
            title = "No hay anuncios archivados",
            message = "Desliza un anuncio hacia la izquierda para archivarlo."
        )

        state.loadFailed -> EmptyState(
            icon = Icons.Outlined.CloudOff,
            title = "No se pudieron cargar los anuncios",
            message = "Revisa tu conexión e inténtalo otra vez.",
            actionLabel = "Reintentar",
            onAction = onRetry
        )

        state.isTeacher -> EmptyState(
            icon = Icons.Outlined.Campaign,
            title = "Aún no publicaste anuncios",
            message = "Toca «Nuevo anuncio» para escribir el primero."
        )

        else -> EmptyState(
            icon = Icons.Outlined.Campaign,
            title = "Aún no hay anuncios en tus cursos",
            message = "Aquí verás los avisos de tus docentes."
        )
    }
}

@Composable
private fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 32.dp, end = 32.dp, bottom = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MySecondaryColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MyPrimaryColor, modifier = Modifier.size(36.dp))
        }
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MyForegroundColor,
            textAlign = TextAlign.Center
        )
        if (message != null) {
            Text(
                text = message,
                fontSize = 14.sp,
                color = MyMutedForegroundColor,
                textAlign = TextAlign.Center
            )
        }
        if (actionLabel != null && onAction != null) {
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = MyPrimaryColor)
            ) {
                Text(actionLabel, color = MyPrimaryForegroundColor)
            }
        }
    }
}
