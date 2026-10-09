package com.example.vexorgym.ui.routine

import com.example.vexorgym.data.model.Exercise
import com.example.vexorgym.data.model.WeekDay

data class WeeklyRoutineUiState(
    val isLoading: Boolean = true,
    val routineName: String = "",
    val selectedDay: WeekDay = WeekDay.MONDAY,
    val exercisesByDay: Map<WeekDay, List<Exercise>> = emptyMap(),
    val catalog: List<Exercise> = emptyList(),
    val actionError: String? = null,
) {
    val selectedExercises: List<Exercise>
        get() = exercisesByDay[selectedDay].orEmpty()

    val exercisesNotOnSelectedDay: List<Exercise>
        get() {
            val assignedSignatures = selectedExercises.map { 
                "${it.name.lowercase().trim()}|${it.muscleGroup.lowercase().trim()}" 
            }.toSet()
            
            return catalog.filter { 
                val sig = "${it.name.lowercase().trim()}|${it.muscleGroup.lowercase().trim()}"
                sig !in assignedSignatures 
            }.distinctBy { 
                "${it.name.lowercase().trim()}|${it.muscleGroup.lowercase().trim()}" 
            }
        }
}
