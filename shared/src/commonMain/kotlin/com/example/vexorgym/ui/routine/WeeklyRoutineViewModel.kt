package com.example.vexorgym.ui.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vexorgym.data.model.WeekDay
import com.example.vexorgym.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeeklyRoutineViewModel(
    private val repository: GymRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklyRoutineUiState())
    val uiState: StateFlow<WeeklyRoutineUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeRoutine().collect { routine ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        routineName = routine.name,
                        exercisesByDay = routine.exercisesByDay,
                    )
                }
            }
        }
    }

    fun selectDay(day: WeekDay) {
        _uiState.update { it.copy(selectedDay = day) }
    }
}
