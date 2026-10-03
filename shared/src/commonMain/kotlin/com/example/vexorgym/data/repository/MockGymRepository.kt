package com.example.vexorgym.data.repository

import com.example.vexorgym.data.model.Exercise
import com.example.vexorgym.data.model.Routine
import com.example.vexorgym.data.model.WeekDay
import com.example.vexorgym.data.model.WorkoutSet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MockGymRepository : GymRepository {

    private val mutex = Mutex()
    private var setSequence = 100

    private val exercises: Map<String, Exercise> = listOf(
        Exercise("ex_bench", "Press banca", "Pecho"),
        Exercise("ex_incline", "Press inclinado", "Pecho"),
        Exercise("ex_dips", "Fondos", "Pecho"),
        Exercise("ex_squat", "Sentadilla", "Piernas"),
        Exercise("ex_leg_press", "Prensa", "Piernas"),
        Exercise("ex_leg_curl", "Curl femoral", "Piernas"),
        Exercise("ex_pullup", "Dominadas", "Espalda"),
        Exercise("ex_row", "Remo con barra", "Espalda"),
        Exercise("ex_face_pull", "Face pull", "Espalda"),
        Exercise("ex_deadlift", "Peso muerto", "Posterior"),
        Exercise("ex_hip_thrust", "Hip thrust", "Glúteos"),
        Exercise("ex_ohp", "Press militar", "Hombros"),
        Exercise("ex_lateral", "Elevaciones laterales", "Hombros"),
        Exercise("ex_plank", "Plancha", "Core"),
        Exercise("ex_crunch", "Crunch", "Core"),
    ).associateBy { it.id }

    private val routine = Routine(
        id = "routine_1",
        name = "Rutina 1",
        exercisesByDay = mapOf(
            WeekDay.MONDAY to listOf(exercises.getValue("ex_bench"), exercises.getValue("ex_incline"), exercises.getValue("ex_dips")),
            WeekDay.TUESDAY to listOf(exercises.getValue("ex_squat"), exercises.getValue("ex_leg_press"), exercises.getValue("ex_leg_curl")),
            WeekDay.WEDNESDAY to listOf(exercises.getValue("ex_pullup"), exercises.getValue("ex_row"), exercises.getValue("ex_face_pull")),
            WeekDay.THURSDAY to listOf(exercises.getValue("ex_deadlift"), exercises.getValue("ex_hip_thrust")),
            WeekDay.FRIDAY to listOf(exercises.getValue("ex_ohp"), exercises.getValue("ex_lateral")),
            WeekDay.SATURDAY to listOf(exercises.getValue("ex_plank"), exercises.getValue("ex_crunch")),
            WeekDay.SUNDAY to emptyList(),
        ),
    )

    private val routineFlow = MutableStateFlow(routine)

    private val setsByExercise = MutableStateFlow(
        mapOf(
            "ex_bench" to sets("ex_bench", 60.0 to 10, 70.0 to 8, 80.0 to 6),
            "ex_incline" to sets("ex_incline", 40.0 to 12, 45.0 to 10),
            "ex_dips" to sets("ex_dips", 0.0 to 12, 5.0 to 10),
            "ex_squat" to sets("ex_squat", 80.0 to 8, 90.0 to 6, 100.0 to 5),
            "ex_leg_press" to sets("ex_leg_press", 120.0 to 12, 140.0 to 10),
            "ex_leg_curl" to sets("ex_leg_curl", 35.0 to 12, 40.0 to 10),
            "ex_pullup" to sets("ex_pullup", 0.0 to 8, 0.0 to 6),
            "ex_row" to sets("ex_row", 50.0 to 10, 60.0 to 8),
            "ex_face_pull" to sets("ex_face_pull", 15.0 to 15, 17.5 to 12),
            "ex_deadlift" to sets("ex_deadlift", 100.0 to 5, 110.0 to 5),
            "ex_hip_thrust" to sets("ex_hip_thrust", 70.0 to 10, 80.0 to 8),
            "ex_ohp" to sets("ex_ohp", 40.0 to 8, 45.0 to 6),
            "ex_lateral" to sets("ex_lateral", 8.0 to 15, 10.0 to 12),
            "ex_plank" to sets("ex_plank", 0.0 to 45, 0.0 to 60),
            "ex_crunch" to sets("ex_crunch", 0.0 to 20, 0.0 to 25),
        ),
    )

    override suspend fun login(email: String, password: String): Result<Unit> {
        delay(400)
        val normalizedEmail = email.trim()
        return when {
            normalizedEmail.isEmpty() || password.isEmpty() ->
                Result.failure(IllegalArgumentException("Completá email y contraseña."))
            !normalizedEmail.contains("@") ->
                Result.failure(IllegalArgumentException("Ingresá un email válido."))
            password.length < 4 ->
                Result.failure(IllegalArgumentException("La contraseña debe tener al menos 4 caracteres."))
            else -> Result.success(Unit)
        }
    }

    override fun observeRoutine(): Flow<Routine> = routineFlow

    override fun observeExercise(exerciseId: String): Flow<Exercise?> =
        routineFlow.map { exercises[exerciseId] }

    override fun observeSets(exerciseId: String): Flow<List<WorkoutSet>> =
        setsByExercise.map { current -> current[exerciseId].orEmpty() }

    override suspend fun addSet(
        exerciseId: String,
        weightKg: Double,
        repetitions: Int,
    ): Result<Unit> {
        if (exercises[exerciseId] == null) {
            return Result.failure(IllegalArgumentException("El ejercicio no existe."))
        }
        if (weightKg < 0 || repetitions <= 0) {
            return Result.failure(IllegalArgumentException("Peso y repeticiones deben ser válidos."))
        }
        mutex.withLock {
            val newSet = WorkoutSet(
                id = "set_${++setSequence}",
                exerciseId = exerciseId,
                repetitions = repetitions,
                weightKg = weightKg,
            )
            setsByExercise.update { current ->
                val existing = current[exerciseId].orEmpty()
                current + (exerciseId to existing + newSet)
            }
        }
        return Result.success(Unit)
    }

    private fun sets(exerciseId: String, vararg values: Pair<Double, Int>): List<WorkoutSet> =
        values.mapIndexed { index, (weight, reps) ->
            WorkoutSet(
                id = "set_${exerciseId}_$index",
                exerciseId = exerciseId,
                repetitions = reps,
                weightKg = weight,
            )
        }
}
