package com.example.vexorgym.ui.exercise

import com.example.vexorgym.data.model.WorkoutSession
import com.example.vexorgym.data.model.WorkoutSet

data class SessionSetDraft(
    val weightInput: String = "",
    val repsInput: String = "",
    val error: String? = null,
)

data class ExerciseDetailUiState(
    val isLoading: Boolean = true,
    val exerciseName: String = "",
    val muscleGroup: String = "",
    val sessions: List<WorkoutSession> = emptyList(),
    val draftsBySessionId: Map<String, SessionSetDraft> = emptyMap(),
    val actionError: String? = null,
    val notFound: Boolean = false,
) {
    val lastSet: WorkoutSet?
        get() = sessions.lastOrNull()?.sets?.lastOrNull()

    fun draftFor(sessionId: String): SessionSetDraft =
        draftsBySessionId[sessionId] ?: SessionSetDraft()
}
