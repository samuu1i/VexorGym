package com.example.vexorgym.ui.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vexorgym.data.model.WeekDay
import com.example.vexorgym.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeeklyRoutineViewModel(
    private val repository: GymRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklyRoutineUiState())
    val uiState: StateFlow<WeeklyRoutineUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.observeRoutine(),
                repository.observeCatalog(),
            ) { routine, catalog -> routine to catalog }
                .collect { (routine, catalog) ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            routineName = routine.name,
                            exercisesByDay = routine.exercisesByDay,
                            catalog = catalog,
                        )
                    }
                }
        }
    }

    fun selectDay(day: WeekDay) {
        _uiState.update { it.copy(selectedDay = day, actionError = null) }
    }

    fun addExistingExercise(exerciseId: String) {
        val day = _uiState.value.selectedDay
        viewModelScope.launch {
            repository.addExerciseToDay(day, exerciseId)
                .onFailure { error ->
                    _uiState.update {
                        it.copy(actionError = error.message ?: "No se pudo agregar el ejercicio.")
                    }
                }
                .onSuccess {
                    _uiState.update { it.copy(actionError = null) }
                }
        }
    }

    fun createExercise(name: String, muscleGroup: String) {
        val day = _uiState.value.selectedDay
        if (name.isBlank()) {
            _uiState.update { it.copy(actionError = "Ingresá el nombre del ejercicio.") }
            return
        }
        viewModelScope.launch {
            repository.createExerciseForDay(day, name, muscleGroup)
                .onFailure { error ->
                    _uiState.update {
                        it.copy(actionError = error.message ?: "No se pudo crear el ejercicio.")
                    }
                }
                .onSuccess {
                    _uiState.update { it.copy(actionError = null) }
                }
        }
    }

    fun removeExercise(exerciseId: String) {
        val day = _uiState.value.selectedDay
        viewModelScope.launch {
            repository.removeExerciseFromDay(day, exerciseId)
                .onFailure { error ->
                    _uiState.update {
                        it.copy(actionError = error.message ?: "No se pudo eliminar el ejercicio.")
                    }
                }
        }
    }
}
