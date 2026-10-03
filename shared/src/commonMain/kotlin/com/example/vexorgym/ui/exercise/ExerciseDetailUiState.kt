package com.example.vexorgym.ui.exercise

import com.example.vexorgym.data.model.WorkoutSession
import com.example.vexorgym.data.model.WorkoutSet

data class ExerciseDetailUiState(
    val isLoading: Boolean = true,
    val exerciseName: String = "",
    val muscleGroup: String = "",
    val sessions: List<WorkoutSession> = emptyList(),
    val weightInput: String = "",
    val repsInput: String = "",
    val formError: String? = null,
    val notFound: Boolean = false,
) {
    val lastSet: WorkoutSet?
        get() = sessions.lastOrNull()?.sets?.lastOrNull()
}
