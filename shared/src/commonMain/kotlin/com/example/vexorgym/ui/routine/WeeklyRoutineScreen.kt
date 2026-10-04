package com.example.vexorgym.ui.routine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vexorgym.data.model.Exercise
import com.example.vexorgym.data.model.WeekDay
import com.example.vexorgym.di.AppContainer

@Composable
fun WeeklyRoutineRoute(
    onExerciseClick: (String) -> Unit,
    onLogoutSuccess: () -> Unit,
    viewModel: WeeklyRoutineViewModel = viewModel {
        WeeklyRoutineViewModel(AppContainer.gymRepository)
    },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WeeklyRoutineScreen(
        uiState = uiState,
        onDaySelected = viewModel::selectDay,
        onExerciseClick = onExerciseClick,
        onRemoveExercise = viewModel::removeExercise,
        onAddExistingExercise = viewModel::addExistingExercise,
        onAddFromCatalog = viewModel::addExerciseFromCatalog,
        onCreateExercise = viewModel::createExercise,
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
    onLogoutClick: () -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.routineName.ifBlank { "Rutina semanal" }) },
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(WeekDay.entries) { day ->
                    FilterChip(
                        selected = day == uiState.selectedDay,
                        onClick = { onDaySelected(day) },
                        label = { Text(day.displayName) },
                    )
                }
            }

            val exercises = uiState.selectedExercises
            if (exercises.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
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
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(exercises, key = { it.id }) { exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            onClick = { onExerciseClick(exercise.id) },
                            onRemove = { onRemoveExercise(exercise.id) },
                        )
                    }
                }
            }

            uiState.actionError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("Agregar ejercicio")
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
}

@Composable
private fun ExerciseCard(
    exercise: Exercise,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    exercise.muscleGroup,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar ${exercise.name}",
                    tint = MaterialTheme.colorScheme.error,
                )
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
                }
                Text(
                    when (step) {
                        0 -> "Seleccionar grupo muscular"
                        1 -> "Seleccionar ejercicio"
                        else -> "Nuevo ejercicio"
                    }
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (step == 0) {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(allGroups) { group ->
                            Text(
                                text = group,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedGroup = group
                                        step = 1
                                    }
                                    .padding(vertical = 12.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else if (step == 1) {
                    val basicExercises = MUSCLE_GROUPS_CATALOG[selectedGroup].orEmpty()
                    val userExistingForGroup = availableExercises.filter { it.muscleGroup == selectedGroup }

                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
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
                                // Evitar duplicados si tienen el mismo nombre que el catálogo
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
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = { step = 2 }) {
                                Text("+ Agregar ejercicio personalizado")
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
                    ) {
                        Text("Crear y agregar")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        },
    )
}
