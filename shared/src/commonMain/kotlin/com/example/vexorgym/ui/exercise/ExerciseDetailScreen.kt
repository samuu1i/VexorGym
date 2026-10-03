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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vexorgym.data.model.WorkoutSession
import com.example.vexorgym.data.model.WorkoutSet
import com.example.vexorgym.di.AppContainer

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
        onAddSession = viewModel::addSession,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    uiState: ExerciseDetailUiState,
    onBack: () -> Unit,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onAddSet: () -> Unit,
    onAddSession: () -> Unit,
) {
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
                                    "Todavía no hay sesiones. Registrá la primera serie abajo.",
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
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Agregar serie", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Se suma a la sesión más reciente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = uiState.weightInput,
                                onValueChange = onWeightChange,
                                modifier = Modifier.weight(1f),
                                label = { Text("Peso") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            )
                            OutlinedTextField(
                                value = uiState.repsInput,
                                onValueChange = onRepsChange,
                                modifier = Modifier.weight(1f),
                                label = { Text("Repeticiones") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                        }
                        uiState.formError?.let { error ->
                            Spacer(Modifier.height(8.dp))
                            Text(error, color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = onAddSet,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Agregar serie")
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onAddSession,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Nueva sesión")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionCard(
    sessionNumber: Int,
    session: WorkoutSession,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Sesión $sessionNumber",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(8.dp))
            if (session.sets.isEmpty()) {
                Text(
                    "Sin series todavía.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                session.sets.forEachIndexed { index, set ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Serie ${index + 1}", style = MaterialTheme.typography.titleSmall)
                        Text(formatSet(set), style = MaterialTheme.typography.bodyLarge)
                    }
                }
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
