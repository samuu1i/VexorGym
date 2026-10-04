package com.example.vexorgym.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vexorgym.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExerciseDetailViewModel(
    private val exerciseId: String,
    private val repository: GymRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseDetailUiState())
    val uiState: StateFlow<ExerciseDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeExercise(exerciseId).collect { exercise ->
                _uiState.update { current ->
                    if (exercise == null) {
                        current.copy(isLoading = false, notFound = true)
                    } else {
                        current.copy(
                            isLoading = false,
                            notFound = false,
                            exerciseName = exercise.name,
                            muscleGroup = exercise.muscleGroup,
                            sessions = exercise.sessions,
                        )
                    }
                }
            }
        }
    }

    fun onWeightChange(sessionId: String, value: String) {
        updateDraft(sessionId) { it.copy(weightInput = value, error = null) }
    }

    fun onRepsChange(sessionId: String, value: String) {
        updateDraft(sessionId) { it.copy(repsInput = value, error = null) }
    }

    fun addSession() {
        viewModelScope.launch {
            println("[PERFORMANCE] addSession TRIGGERED - Tiempo: ${kotlin.time.Clock.System.now().toEpochMilliseconds()}")
            repository.addSession(exerciseId)
                .onFailure { error ->
                    _uiState.update {
                        it.copy(actionError = error.message ?: "No se pudo crear la sesión.")
                    }
                }
                .onSuccess {
                    _uiState.update { it.copy(actionError = null) }
                }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            println("[PERFORMANCE] deleteSession TRIGGERED - Tiempo: ${kotlin.time.Clock.System.now().toEpochMilliseconds()}")
            repository.deleteSession(exerciseId, sessionId)
                .onSuccess {
                    _uiState.update { current ->
                        current.copy(
                            draftsBySessionId = current.draftsBySessionId - sessionId,
                            actionError = null,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(actionError = error.message ?: "No se pudo eliminar la sesión.")
                    }
                }
        }
    }

    fun addSet(sessionId: String) {
        val draft = _uiState.value.draftFor(sessionId)
        val weight = draft.weightInput.replace(",", ".").toDoubleOrNull()
        val reps = draft.repsInput.toIntOrNull()
        if (weight == null || weight < 0) {
            updateDraft(sessionId) { it.copy(error = "Ingresá un peso válido.") }
            return
        }
        if (reps == null || reps <= 0) {
            updateDraft(sessionId) { it.copy(error = "Ingresá repeticiones válidas.") }
            return
        }
        viewModelScope.launch {
            println("[PERFORMANCE] addSet TRIGGERED - Tiempo: ${kotlin.time.Clock.System.now().toEpochMilliseconds()}")
            repository.addSet(exerciseId, sessionId, weight, reps)
                .onSuccess {
                    updateDraft(sessionId) { SessionSetDraft() }
                    _uiState.update { it.copy(actionError = null) }
                }
                .onFailure { error ->
                    updateDraft(sessionId) {
                        it.copy(error = error.message ?: "No se pudo agregar la serie.")
                    }
                }
        }
    }

    fun deleteSet(sessionId: String, setId: String) {
        viewModelScope.launch {
            println("[PERFORMANCE] deleteSet TRIGGERED - Tiempo: ${kotlin.time.Clock.System.now().toEpochMilliseconds()}")
            repository.deleteSet(exerciseId, sessionId, setId)
                .onSuccess {
                    _uiState.update { it.copy(actionError = null) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(actionError = error.message ?: "No se pudo eliminar la serie.")
                    }
                }
        }
    }

    private fun updateDraft(sessionId: String, transform: (SessionSetDraft) -> SessionSetDraft) {
        _uiState.update { current ->
            val draft = transform(current.draftFor(sessionId))
            current.copy(
                draftsBySessionId = current.draftsBySessionId + (sessionId to draft),
                actionError = null,
            )
        }
    }
}
