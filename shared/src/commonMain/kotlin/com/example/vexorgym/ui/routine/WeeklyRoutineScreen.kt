package com.example.vexorgym.ui.routine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vexorgym.data.model.Exercise
import com.example.vexorgym.data.model.WeekDay
import com.example.vexorgym.di.AppContainer
import com.example.vexorgym.ui.MuscleGroupImage
import com.example.vexorgym.ui.getMuscleGroupColors

@Composable
fun WeeklyRoutineRoute(
    onExerciseClick: (String) -> Unit,
    onLogoutSuccess: () -> Unit,
    viewModel: WeeklyRoutineViewModel = viewModel {
        WeeklyRoutineViewModel(AppContainer.gymRepository)
    },
) {
    val uiState by viewModel.uiState.collectAsState()
    WeeklyRoutineScreen(
        uiState = uiState,
        onDaySelected = viewModel::selectDay,
        onExerciseClick = onExerciseClick,
        onRemoveExercise = viewModel::removeExercise,
        onAddExistingExercise = viewModel::addExistingExercise,
        onAddFromCatalog = viewModel::addExerciseFromCatalog,
        onCreateExercise = viewModel::createExercise,
        onUpdateOrder = viewModel::updateExerciseOrder,
        onLogoutClick = { viewModel.logout(onLogoutSuccess) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyRoutineScreen(
    uiState: WeeklyRoutineUiState,
    onDaySelected: (WeekDay) -> Unit,
    onExerciseClick: (String) -> Unit,
    onRemoveExercise: (String) -> Unit,
    onAddExistingExercise: (String) -> Unit,
    onAddFromCatalog: (String, String) -> Unit,
    onCreateExercise: (String, String) -> Unit,
    onUpdateOrder: (List<String>) -> Unit,
    onLogoutClick: () -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var exercisePendingDelete by remember { mutableStateOf<String?>(null) }
    var localExercises by remember(uiState.selectedExercises) { mutableStateOf(uiState.selectedExercises) }

    val listState = rememberLazyListState()
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }

    Scaffold(
        containerColor = Color(0xFFF7F7FA), // Color pastel como referencia
        topBar = {
            // HEADER CUSTOM
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.FitnessCenter, contentDescription = null, tint = Color(0xFF6B58C1), modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Rutina", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1C1B1F))
                    Text("Tu plan de entrenamiento", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF79747E))
                }
                IconButton(onClick = onLogoutClick) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión", tint = Color(0xFF1C1B1F))
                }
            }
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // DAYS TABS
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(WeekDay.entries) { day ->
                    val isSelected = day == uiState.selectedDay
                    Box(
                        modifier = Modifier
                            .background(if (isSelected) Color(0xFF6B58C1) else Color.White, RoundedCornerShape(50))
                            .border(1.dp, if (isSelected) Color.Transparent else Color(0xFFE0E0E0), RoundedCornerShape(50))
                            .clickable { onDaySelected(day) }
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.DateRange,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Color(0xFF79747E),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = day.displayName,
                                color = if (isSelected) Color.White else Color(0xFF49454F),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            val exercises = localExercises
            if (exercises.isEmpty()) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Día libre", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No hay ejercicios asignados para ${uiState.selectedDay.displayName}.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    itemsIndexed(exercises, key = { _, ex -> ex.id }) { index, exercise ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    exercisePendingDelete = exercise.id
                                    return@rememberSwipeToDismissBoxState false
                                }
                                false
                            }
                        )

                        val isDragging = index == draggingIndex
                        val modifier = if (isDragging) {
                            Modifier.zIndex(1f).graphicsLayer { translationY = dragOffset }
                        } else {
                            Modifier.zIndex(0f)
                        }

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            modifier = modifier,
                            backgroundContent = {
                                val color = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                    MaterialTheme.colorScheme.errorContainer
                                } else {
                                    Color.Transparent
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color, RoundedCornerShape(16.dp))
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.onErrorContainer)
                                    }
                                }
                            }
                        ) {
                            ExerciseCard(
                                exercise = exercise,
                                onClick = { onExerciseClick(exercise.id) },
                                onDragStart = { draggingIndex = index },
                                onDragEnd = {
                                    draggingIndex = null
                                    dragOffset = 0f
                                    onUpdateOrder(localExercises.map { it.id })
                                },
                                onDrag = { dragAmount ->
                                    dragOffset += dragAmount
                                    val currentIdx = draggingIndex ?: return@ExerciseCard
                                    val visibleItems = listState.layoutInfo.visibleItemsInfo
                                    val draggingItemInfo = visibleItems.find { it.index == currentIdx } ?: return@ExerciseCard
                                    val draggingItemCenter = draggingItemInfo.offset + (draggingItemInfo.size / 2) + dragOffset
                                    val targetItem = visibleItems.find {
                                        it.index != currentIdx && draggingItemCenter > it.offset && draggingItemCenter < it.offset + it.size
                                    }
                                    if (targetItem != null) {
                                        val targetIndex = targetItem.index
                                        val newList = localExercises.toMutableList()
                                        val tmp = newList[currentIdx]
                                        newList[currentIdx] = newList[targetIndex]
                                        newList[targetIndex] = tmp
                                        localExercises = newList
                                        draggingIndex = targetIndex
                                        dragOffset += draggingItemInfo.offset - targetItem.offset
                                    }
                                }
                            )
                        }
                    }
                }
            }

            uiState.actionError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B58C1))
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Agregar ejercicio", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }

    if (showAddDialog) {
        AddExerciseDialog(
            availableExercises = uiState.exercisesNotOnSelectedDay,
            onDismiss = { showAddDialog = false },
            onAddFromCatalog = { name, group ->
                onAddFromCatalog(name, group)
                showAddDialog = false
            },
            onSelectExisting = { exerciseId ->
                onAddExistingExercise(exerciseId)
                showAddDialog = false
            },
            onCreate = { name, muscleGroup ->
                onCreateExercise(name, muscleGroup)
                showAddDialog = false
            },
        )
    }

    exercisePendingDelete?.let { exId ->
        AlertDialog(
            onDismissRequest = { exercisePendingDelete = null },
            title = { Text("Eliminar ejercicio") },
            text = { Text("¿Estás seguro de que querés eliminar este ejercicio?") },
            confirmButton = {
                TextButton(onClick = {
                    onRemoveExercise(exId)
                    exercisePendingDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { exercisePendingDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ExerciseCard(
    exercise: Exercise,
    onClick: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

    val lastSession = exercise.sessions.lastOrNull()
    val lastSessionInfo = lastSession?.sets?.lastOrNull()?.let { lastSet ->
        val weightStr = if (lastSet.weightKg % 1.0 == 0.0) lastSet.weightKg.toInt().toString() else lastSet.weightKg.toString()
        "Última: ${weightStr}kg × ${lastSet.repetitions}"
    }

    val (bgColor, contentColor) = getMuscleGroupColors(exercise.muscleGroup)

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(56.dp).background(Color.White.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                MuscleGroupImage(muscleGroup = exercise.muscleGroup, modifier = Modifier.size(40.dp), iconSize = 24)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1C1B1F))
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationSearching, contentDescription = null, tint = Color(0xFF49454F), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        exercise.muscleGroup,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF49454F),
                    )
                }
                Spacer(Modifier.height(8.dp))
                if (lastSessionInfo != null) {
                    Box(modifier = Modifier.background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.FitnessCenter, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = lastSessionInfo,
                                style = MaterialTheme.typography.labelSmall,
                                color = contentColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            
            // Reordenar vs Flecha: En la imagen hay una flecha a la derecha en lugar del icono de arrastrar visible.
            // Para no romper la funcionalidad de Drag&Drop existente, mantendré el drag handling en toda la tarjeta o en el ícono.
            // La referencia muestra un círculo semitransparente con una flecha.
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.5f), CircleShape)
                    .pointerInput(Unit) {
                        // El drag & drop funciona tocando el área de la flecha/derecha
                        detectVerticalDragGestures(
                            onDragStart = { currentOnDragStart() },
                            onDragEnd = { currentOnDragEnd() },
                            onDragCancel = { currentOnDragEnd() },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                currentOnDrag(dragAmount)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Ir o Reordenar", tint = contentColor)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExerciseDialog(
    availableExercises: List<Exercise>,
    onDismiss: () -> Unit,
    onAddFromCatalog: (String, String) -> Unit,
    onSelectExisting: (String) -> Unit,
    onCreate: (String, String) -> Unit,
) {
    var step by remember { mutableStateOf(0) }
    var selectedGroup by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }

    val allGroups = MUSCLE_GROUPS_CATALOG.keys.toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (step > 0) {
                    IconButton(onClick = { step-- }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                } else {
                    Icon(Icons.Filled.FitnessCenter, contentDescription = null, tint = Color(0xFF6B58C1), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    when (step) {
                        0 -> "Librería de músculos"
                        1 -> "Seleccionar ejercicio"
                        else -> "Nuevo ejercicio"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1C1B1F)
                )
            }
        },
        containerColor = Color(0xFFF7F7FA),
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (step == 0) {
                    // GRID DE MUSCULOS
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.heightIn(max = 400.dp).padding(top = 8.dp)
                    ) {
                        items(allGroups) { group ->
                            Card(
                                modifier = Modifier.clickable {
                                    selectedGroup = group
                                    step = 1
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    MuscleGroupImage(group, modifier = Modifier.size(64.dp), iconSize = 32)
                                    Spacer(Modifier.height(8.dp))
                                    Text(group.uppercase(), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                                }
                            }
                        }
                    }
                } else if (step == 1) {
                    val basicExercises = MUSCLE_GROUPS_CATALOG[selectedGroup].orEmpty()
                    val userExistingForGroup = availableExercises.filter { it.muscleGroup == selectedGroup }

                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(basicExercises) { exName ->
                            Text(
                                text = exName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAddFromCatalog(exName, selectedGroup) }
                                    .padding(vertical = 12.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        if (userExistingForGroup.isNotEmpty()) {
                            item {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Text("Mis ejercicios de $selectedGroup", style = MaterialTheme.typography.labelMedium)
                            }
                            items(userExistingForGroup, key = { it.id }) { ex ->
                                if (basicExercises.none { it.equals(ex.name, ignoreCase = true) }) {
                                    Text(
                                        text = ex.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onSelectExisting(ex.id) }
                                            .padding(vertical = 12.dp),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                        item {
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { step = 2 },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFE8FF)),
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = Color(0xFF6B58C1))
                                Spacer(Modifier.width(8.dp))
                                Text("Agregar", color = Color(0xFF6B58C1), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nombre del ejercicio ($selectedGroup)") },
                        singleLine = true,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { onCreate(customName, selectedGroup) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = customName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B58C1))
                    ) {
                        Text("Crear y agregar")
                    }
                }
            }
        },
        confirmButton = {
            if (step != 0) {
                TextButton(onClick = onDismiss) { Text("Cerrar") }
            }
        },
    )
}
