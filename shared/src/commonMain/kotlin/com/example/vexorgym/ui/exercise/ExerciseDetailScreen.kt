package com.example.vexorgym.ui.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vexorgym.data.model.WorkoutSession
import com.example.vexorgym.data.model.WorkoutSet
import com.example.vexorgym.di.AppContainer
import kotlin.time.Instant

@Composable
fun ExerciseDetailRoute(
    exerciseId: String,
    onBack: () -> Unit,
    viewModel: ExerciseDetailViewModel = viewModel(key = exerciseId) {
        ExerciseDetailViewModel(exerciseId, AppContainer.gymRepository)
    },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExerciseDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onWeightChange = viewModel::onWeightChange,
        onRepsChange = viewModel::onRepsChange,
        onAddSet = viewModel::addSet,
        onDeleteSet = viewModel::deleteSet,
        onAddSession = viewModel::addSession,
        onDeleteSession = viewModel::deleteSession,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    uiState: ExerciseDetailUiState,
    onBack: () -> Unit,
    onWeightChange: (sessionId: String, value: String) -> Unit,
    onRepsChange: (sessionId: String, value: String) -> Unit,
    onAddSet: (sessionId: String) -> Unit,
    onDeleteSet: (sessionId: String, setId: String) -> Unit,
    onAddSession: () -> Unit,
    onDeleteSession: (sessionId: String) -> Unit,
) {
    var sessionPendingDelete by remember { mutableStateOf<WorkoutSession?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.exerciseName.ifBlank { "Ejercicio" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.notFound -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Ejercicio no encontrado")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onBack) { Text("Volver") }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    Text(
                        text = uiState.muscleGroup,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    uiState.lastSet?.let { lastSet ->
                        Text(
                            text = "Última serie: ${formatSet(lastSet)}",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (uiState.sessions.isEmpty()) {
                            item {
                                Text(
                                    "Todavía no hay sesiones. Creá una para empezar a anotar.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        } else {
                            itemsIndexed(
                                uiState.sessions,
                                key = { _, session -> session.id },
                            ) { index, session ->
                                SessionCard(
                                    sessionNumber = index + 1,
                                    session = session,
                                    draft = uiState.draftFor(session.id),
                                    onWeightChange = { onWeightChange(session.id, it) },
                                    onRepsChange = { onRepsChange(session.id, it) },
                                    onAddSet = { onAddSet(session.id) },
                                    onDeleteSet = { onDeleteSet(session.id, it) },
                                    onDeleteSession = { sessionPendingDelete = session },
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                    Column(modifier = Modifier.padding(16.dp)) {
                        uiState.actionError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        Button(
                            onClick = onAddSession,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text("Crear nueva sesión")
                        }
                    }
                }
            }
        }
    }

    sessionPendingDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionPendingDelete = null },
            title = { Text("Eliminar sesión") },
            text = {
                Text("Se va a borrar esta sesión y todas sus series. Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSession(session.id)
                        sessionPendingDelete = null
                    },
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionPendingDelete = null }) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun SessionCard(
    sessionNumber: Int,
    session: WorkoutSession,
    draft: SessionSetDraft,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onAddSet: () -> Unit,
    onDeleteSet: (setId: String) -> Unit,
    onDeleteSession: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Sesión $sessionNumber",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = formatSessionDate(session.createdAtMillis),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDeleteSession) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Eliminar sesión $sessionNumber",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            if (session.sets.isEmpty()) {
                Text(
                    "Sin series todavía.",
                    modifier = Modifier.padding(end = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                session.sets.forEachIndexed { index, set ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Serie ${index + 1}", style = MaterialTheme.typography.titleSmall)
                            Text(formatSet(set), style = MaterialTheme.typography.bodyLarge)
                        }
                        IconButton(onClick = { onDeleteSet(set.id) }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Eliminar serie ${index + 1}",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(modifier = Modifier.padding(end = 12.dp))
            Spacer(Modifier.height(12.dp))
            Text(
                "Agregar serie",
                modifier = Modifier.padding(end = 12.dp),
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.padding(end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = draft.weightInput,
                    onValueChange = onWeightChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Peso") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = draft.repsInput,
                    onValueChange = onRepsChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Repeticiones") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            draft.error?.let { error ->
                Spacer(Modifier.height(8.dp))
                Text(
                    error,
                    modifier = Modifier.padding(end = 12.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onAddSet,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 12.dp),
            ) {
                Text("Agregar serie")
            }
        }
    }
}

private fun formatSet(set: WorkoutSet): String {
    val weight = if (set.weightKg % 1.0 == 0.0) {
        set.weightKg.toInt().toString()
    } else {
        set.weightKg.toString()
    }
    return "$weight kg × ${set.repetitions} reps"
}

private fun formatSessionDate(createdAtMillis: Long): String {
    val iso = Instant.fromEpochMilliseconds(createdAtMillis).toString()
    val datePart = iso.substringBefore('T')
    val timePart = iso.substringAfter('T').take(5)
    return "$datePart · $timePart"
}
