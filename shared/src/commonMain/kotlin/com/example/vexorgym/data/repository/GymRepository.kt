package com.example.vexorgym.data.repository

import com.example.vexorgym.data.model.Exercise
import com.example.vexorgym.data.model.Routine
import com.example.vexorgym.data.model.WorkoutSet
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de datos del dominio. Sustituí [MockGymRepository] por una
 * implementación Firebase/Supabase sin tocar ViewModels ni UI.
 */
interface GymRepository {
    suspend fun login(email: String, password: String): Result<Unit>

    fun observeRoutine(): Flow<Routine>

    fun observeExercise(exerciseId: String): Flow<Exercise?>

    /** Series ordenadas de más antigua a más reciente. */
    fun observeSets(exerciseId: String): Flow<List<WorkoutSet>>

    suspend fun addSet(exerciseId: String, weightKg: Double, repetitions: Int): Result<Unit>
}
