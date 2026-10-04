package com.example.vexorgym.data.repository

import com.example.vexorgym.data.model.Exercise
import com.example.vexorgym.data.model.Routine
import com.example.vexorgym.data.model.WeekDay
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de datos del dominio. Sustituí [MockGymRepository] por una
 * implementación Firebase/Supabase sin tocar ViewModels ni UI.
 */
interface GymRepository {
    suspend fun login(email: String, password: String): Result<Unit>

    fun observeRoutine(): Flow<Routine>

    fun observeCatalog(): Flow<List<Exercise>>

    fun observeExercise(exerciseId: String): Flow<Exercise?>

    suspend fun addExerciseToDay(day: WeekDay, exerciseId: String): Result<Unit>

    suspend fun createExerciseForDay(
        day: WeekDay,
        name: String,
        muscleGroup: String,
    ): Result<Unit>

    suspend fun removeExerciseFromDay(day: WeekDay, exerciseId: String): Result<Unit>

    /** Crea una sesión vacía con la fecha/hora actual. */
    suspend fun addSession(exerciseId: String): Result<Unit>

    suspend fun deleteSession(exerciseId: String, sessionId: String): Result<Unit>

    /** Agrega la serie a la sesión indicada. */
    suspend fun addSet(
        exerciseId: String,
        sessionId: String,
        weightKg: Double,
        repetitions: Int,
    ): Result<Unit>

    suspend fun deleteSet(
        exerciseId: String,
        sessionId: String,
        setId: String,
    ): Result<Unit>
}
