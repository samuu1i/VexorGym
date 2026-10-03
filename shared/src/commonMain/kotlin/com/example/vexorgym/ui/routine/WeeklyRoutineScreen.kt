package com.example.vexorgym.ui.routine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    viewModel: WeeklyRoutineViewModel = viewModel {
        WeeklyRoutineViewModel(AppContainer.gymRepository)
    },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WeeklyRoutineScreen(
        uiState = uiState,
        onDaySelected = viewModel::selectDay,
        onExerciseClick = onExerciseClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyRoutineScreen(
    uiState: WeeklyRoutineUiState,
    onDaySelected: (WeekDay) -> Unit,
    onExerciseClick: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(uiState.routineName.ifBlank { "Rutina semanal" }) })
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
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Día de descanso", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No hay ejercicios asignados para ${uiState.selectedDay.displayName}.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(exercises, key = { it.id }) { exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            onClick = { onExerciseClick(exercise.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: Exercise,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(exercise.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                exercise.muscleGroup,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
