package com.example.vexorgym.ui.exercise

import com.example.vexorgym.data.model.WorkoutSet

data class ExerciseDetailUiState(
    val isLoading: Boolean = true,
    val exerciseName: String = "",
    val muscleGroup: String = "",
    val sets: List<WorkoutSet> = emptyList(),
    val weightInput: String = "",
    val repsInput: String = "",
    val formError: String? = null,
    val notFound: Boolean = false,
)
