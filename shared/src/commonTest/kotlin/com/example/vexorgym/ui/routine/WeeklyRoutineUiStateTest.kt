package com.example.vexorgym.ui.routine

import com.example.vexorgym.data.model.Exercise
import com.example.vexorgym.data.model.WeekDay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WeeklyRoutineUiStateTest {

    @Test
    fun `exercisesNotOnSelectedDay should exclude exercises with same name and muscle group regardless of case`() {
        // Arrange
        val catalog = listOf(
            Exercise(id = "1", name = "Press Banca", muscleGroup = "Pecho"),
            Exercise(id = "2", name = "Sentadilla", muscleGroup = "Piernas"),
            Exercise(id = "3", name = "Dominadas", muscleGroup = "Espalda"),
            Exercise(id = "4", name = "press banca", muscleGroup = "PECHO") // Duplicado en catálogo con otro ID
        )

        val selectedExercises = listOf(
            Exercise(id = "5", name = "PRESS BANCA", muscleGroup = "pecho")
        )

        val uiState = WeeklyRoutineUiState(
            selectedDay = WeekDay.MONDAY,
            exercisesByDay = mapOf(WeekDay.MONDAY to selectedExercises),
            catalog = catalog
        )

        // Act
        val available = uiState.exercisesNotOnSelectedDay

        // Assert
        // Press Banca (ID 1 y 4) no deberían estar disponibles porque ya hay un "PRESS BANCA" asignado
        assertFalse(available.any { it.name.lowercase() == "press banca" }, "Press Banca should be excluded")
        
        // Sentadilla y Dominadas sí deberían estar disponibles
        assertTrue(available.any { it.name == "Sentadilla" }, "Sentadilla should be available")
        assertTrue(available.any { it.name == "Dominadas" }, "Dominadas should be available")
        
        // El catálogo tiene 4 elementos, pero quitamos 2 variaciones de Press Banca. Deberían quedar 2.
        assertEquals(2, available.size)
    }

    @Test
    fun `exercisesNotOnSelectedDay should remove duplicates within the catalog itself`() {
        // Arrange
        val catalog = listOf(
            Exercise(id = "1", name = "Curl", muscleGroup = "Bíceps"),
            Exercise(id = "2", name = "curl", muscleGroup = "bíceps")
        )

        val uiState = WeeklyRoutineUiState(
            selectedDay = WeekDay.MONDAY,
            exercisesByDay = emptyMap(), // Nada seleccionado
            catalog = catalog
        )

        // Act
        val available = uiState.exercisesNotOnSelectedDay

        // Assert
        // Debería quedar solo 1 a pesar de que no hay nada seleccionado, por el distinctBy
        assertEquals(1, available.size)
        assertEquals("Curl", available.first().name)
    }
}
