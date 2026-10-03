package com.example.vexorgym.ui.routine

import com.example.vexorgym.data.model.Exercise
import com.example.vexorgym.data.model.WeekDay

data class WeeklyRoutineUiState(
    val isLoading: Boolean = true,
    val routineName: String = "",
    val selectedDay: WeekDay = WeekDay.MONDAY,
    val exercisesByDay: Map<WeekDay, List<Exercise>> = emptyMap(),
) {
    val selectedExercises: List<Exercise>
        get() = exercisesByDay[selectedDay].orEmpty()
}
