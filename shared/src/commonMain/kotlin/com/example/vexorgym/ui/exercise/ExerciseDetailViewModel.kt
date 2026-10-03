package com.example.vexorgym.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vexorgym.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
            combine(
                repository.observeExercise(exerciseId),
                repository.observeSets(exerciseId),
            ) { exercise, sets -> exercise to sets }
                .collect { (exercise, sets) ->
                    _uiState.update { current ->
                        if (exercise == null) {
                            current.copy(isLoading = false, notFound = true)
                        } else {
                            current.copy(
                                isLoading = false,
                                notFound = false,
                                exerciseName = exercise.name,
                                muscleGroup = exercise.muscleGroup,
                                sets = sets,
                            )
                        }
                    }
                }
        }
    }

    fun onWeightChange(value: String) {
        _uiState.update { it.copy(weightInput = value, formError = null) }
    }

    fun onRepsChange(value: String) {
        _uiState.update { it.copy(repsInput = value, formError = null) }
    }

    fun addSet() {
        val current = _uiState.value
        val weight = current.weightInput.replace(",", ".").toDoubleOrNull()
        val reps = current.repsInput.toIntOrNull()
        if (weight == null || weight < 0) {
            _uiState.update { it.copy(formError = "Ingresá un peso válido.") }
            return
        }
        if (reps == null || reps <= 0) {
            _uiState.update { it.copy(formError = "Ingresá repeticiones válidas.") }
            return
        }
        viewModelScope.launch {
            repository.addSet(exerciseId, weight, reps)
                .onSuccess {
                    _uiState.update { it.copy(weightInput = "", repsInput = "", formError = null) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(formError = error.message ?: "No se pudo agregar la serie.")
                    }
                }
        }
    }
}
