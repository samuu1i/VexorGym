package com.example.vexorgym.data.repository

import com.example.vexorgym.data.model.Exercise
import com.example.vexorgym.data.model.Routine
import com.example.vexorgym.data.model.WeekDay
import com.example.vexorgym.data.model.WorkoutSession
import com.example.vexorgym.data.model.WorkoutSet
import kotlin.time.Clock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MockGymRepository : GymRepository {

    private val mutex = Mutex()
    private var exerciseSequence = 200
    private var sessionSequence = 20
    private var setSequence = 100

    private val exercisesFlow = MutableStateFlow(
        listOf(
            exercise("ex_bench", "Press banca", "Pecho", session(60.0 to 10, 70.0 to 8), session(80.0 to 6)),
            exercise("ex_incline", "Press inclinado", "Pecho", session(40.0 to 12), session(45.0 to 10)),
            exercise("ex_dips", "Fondos", "Pecho", session(0.0 to 12, 5.0 to 10)),
            exercise("ex_squat", "Sentadilla", "Piernas", session(80.0 to 8, 90.0 to 6), session(100.0 to 5)),
            exercise("ex_leg_press", "Prensa", "Piernas", session(120.0 to 12, 140.0 to 10)),
            exercise("ex_leg_curl", "Curl femoral", "Piernas", session(35.0 to 12, 40.0 to 10)),
            exercise("ex_pullup", "Dominadas", "Espalda", session(0.0 to 8), session(0.0 to 6)),
            exercise("ex_row", "Remo con barra", "Espalda", session(50.0 to 10, 60.0 to 8)),
            exercise("ex_face_pull", "Face pull", "Espalda", session(15.0 to 15, 17.5 to 12)),
            exercise("ex_deadlift", "Peso muerto", "Posterior", session(100.0 to 5, 110.0 to 5)),
            exercise("ex_hip_thrust", "Hip thrust", "Glúteos", session(70.0 to 10, 80.0 to 8)),
            exercise("ex_ohp", "Press militar", "Hombros", session(40.0 to 8, 45.0 to 6)),
            exercise("ex_lateral", "Elevaciones laterales", "Hombros", session(8.0 to 15, 10.0 to 12)),
            exercise("ex_plank", "Plancha", "Core", session(0.0 to 45, 0.0 to 60)),
            exercise("ex_crunch", "Crunch", "Core", session(0.0 to 20, 0.0 to 25)),
        ).associateBy { it.id },
    )

    private val routineMeta = MutableStateFlow(
        RoutineMeta(
            id = "routine_1",
            name = "Rutina 1",
            exerciseIdsByDay = mapOf(
                WeekDay.MONDAY to listOf("ex_bench", "ex_incline", "ex_dips"),
                WeekDay.TUESDAY to listOf("ex_squat", "ex_leg_press", "ex_leg_curl"),
                WeekDay.WEDNESDAY to listOf("ex_pullup", "ex_row", "ex_face_pull"),
                WeekDay.THURSDAY to listOf("ex_deadlift", "ex_hip_thrust"),
                WeekDay.FRIDAY to listOf("ex_ohp", "ex_lateral"),
                WeekDay.SATURDAY to listOf("ex_plank", "ex_crunch"),
                WeekDay.SUNDAY to emptyList(),
            ),
        ),
    )

    override suspend fun login(email: String, password: String): Result<Unit> {
        delay(400)
        return Result.success(Unit)
    }

    override fun observeRoutine(): Flow<Routine> =
        combine(exercisesFlow, routineMeta) { exercises, meta ->
            Routine(
                id = meta.id,
                name = meta.name,
                exercisesByDay = WeekDay.entries.associateWith { day ->
                    meta.exerciseIdsByDay[day].orEmpty().mapNotNull { exercises[it] }
                },
            )
        }

    override fun observeCatalog(): Flow<List<Exercise>> =
        exercisesFlow.map { current -> current.values.sortedBy { it.name } }

    override fun observeExercise(exerciseId: String): Flow<Exercise?> =
        exercisesFlow.map { current -> current[exerciseId] }

    override suspend fun addExerciseToDay(day: WeekDay, exerciseId: String): Result<Unit> {
        val exists = exercisesFlow.value[exerciseId] != null
        if (!exists) {
            return Result.failure(IllegalArgumentException("El ejercicio no existe."))
        }
        mutex.withLock {
            val ids = routineMeta.value.exerciseIdsByDay[day].orEmpty()
            if (exerciseId in ids) {
                return Result.failure(IllegalArgumentException("Ese ejercicio ya está en ${day.displayName}."))
            }
            routineMeta.update { current ->
                current.copy(
                    exerciseIdsByDay = current.exerciseIdsByDay + (day to ids + exerciseId),
                )
            }
        }
        return Result.success(Unit)
    }

    override suspend fun createExerciseForDay(
        day: WeekDay,
        name: String,
        muscleGroup: String,
    ): Result<Unit> {
        val trimmedName = name.trim()
        val trimmedGroup = muscleGroup.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("El nombre del ejercicio no puede estar vacío."))
        }
        mutex.withLock {
            val newId = "ex_custom_${++exerciseSequence}"
            val created = Exercise(
                id = newId,
                name = trimmedName,
                muscleGroup = trimmedGroup.ifEmpty { "General" },
            )
            exercisesFlow.update { it + (newId to created) }
            routineMeta.update { current ->
                val ids = current.exerciseIdsByDay[day].orEmpty()
                current.copy(exerciseIdsByDay = current.exerciseIdsByDay + (day to ids + newId))
            }
        }
        return Result.success(Unit)
    }

    override suspend fun removeExerciseFromDay(day: WeekDay, exerciseId: String): Result<Unit> {
        mutex.withLock {
            routineMeta.update { current ->
                val ids = current.exerciseIdsByDay[day].orEmpty().filterNot { it == exerciseId }
                current.copy(exerciseIdsByDay = current.exerciseIdsByDay + (day to ids))
            }
        }
        return Result.success(Unit)
    }

    override suspend fun addSession(exerciseId: String): Result<Unit> {
        if (exercisesFlow.value[exerciseId] == null) {
            return Result.failure(IllegalArgumentException("El ejercicio no existe."))
        }
        mutex.withLock {
            exercisesFlow.update { current ->
                val exercise = current[exerciseId] ?: return@update current
                current + (exerciseId to exercise.copy(sessions = exercise.sessions + newSession()))
            }
        }
        return Result.success(Unit)
    }

    override suspend fun deleteSession(exerciseId: String, sessionId: String): Result<Unit> {
        return mutex.withLock {
            val exercise = exercisesFlow.value[exerciseId]
                ?: return@withLock Result.failure(IllegalArgumentException("El ejercicio no existe."))
            if (exercise.sessions.none { it.id == sessionId }) {
                return@withLock Result.failure(IllegalArgumentException("La sesión no existe."))
            }
            updateExercise(exerciseId) { current ->
                current.copy(sessions = current.sessions.filterNot { it.id == sessionId })
            }
            Result.success(Unit)
        }
    }

    override suspend fun addSet(
        exerciseId: String,
        sessionId: String,
        weightKg: Double,
        repetitions: Int,
    ): Result<Unit> {
        if (weightKg < 0 || repetitions <= 0) {
            return Result.failure(IllegalArgumentException("Peso y repeticiones deben ser válidos."))
        }
        return mutex.withLock {
            val exercise = exercisesFlow.value[exerciseId]
                ?: return@withLock Result.failure(IllegalArgumentException("El ejercicio no existe."))
            if (exercise.sessions.none { it.id == sessionId }) {
                return@withLock Result.failure(IllegalArgumentException("La sesión no existe."))
            }
            val newSet = WorkoutSet(
                id = "set_${++setSequence}",
                repetitions = repetitions,
                weightKg = weightKg,
            )
            updateExercise(exerciseId) { current ->
                current.copy(
                    sessions = current.sessions.map { session ->
                        if (session.id == sessionId) {
                            session.copy(sets = session.sets + newSet)
                        } else {
                            session
                        }
                    },
                )
            }
            Result.success(Unit)
        }
    }

    override suspend fun deleteSet(
        exerciseId: String,
        sessionId: String,
        setId: String,
    ): Result<Unit> {
        return mutex.withLock {
            val exercise = exercisesFlow.value[exerciseId]
                ?: return@withLock Result.failure(IllegalArgumentException("El ejercicio no existe."))
            val session = exercise.sessions.firstOrNull { it.id == sessionId }
                ?: return@withLock Result.failure(IllegalArgumentException("La sesión no existe."))
            if (session.sets.none { it.id == setId }) {
                return@withLock Result.failure(IllegalArgumentException("La serie no existe."))
            }
            updateExercise(exerciseId) { current ->
                current.copy(
                    sessions = current.sessions.map { item ->
                        if (item.id == sessionId) {
                            item.copy(sets = item.sets.filterNot { it.id == setId })
                        } else {
                            item
                        }
                    },
                )
            }
            Result.success(Unit)
        }
    }

    private fun updateExercise(exerciseId: String, transform: (Exercise) -> Exercise) {
        exercisesFlow.update { current ->
            val exercise = current[exerciseId] ?: return@update current
            current + (exerciseId to transform(exercise))
        }
    }

    private fun exercise(
        id: String,
        name: String,
        muscleGroup: String,
        vararg sessions: WorkoutSession,
    ): Exercise = Exercise(
        id = id,
        name = name,
        muscleGroup = muscleGroup,
        sessions = sessions.toList(),
    )

    private fun session(vararg values: Pair<Double, Int>): WorkoutSession {
        val sessionId = "session_${++sessionSequence}"
        return WorkoutSession(
            id = sessionId,
            createdAtMillis = BASE_MILLIS + sessionSequence * DAY_MILLIS,
            sets = values.map { (weight, reps) ->
                WorkoutSet(
                    id = "set_${++setSequence}",
                    repetitions = reps,
                    weightKg = weight,
                )
            },
        )
    }

    private fun newSession(vararg sets: WorkoutSet): WorkoutSession =
        WorkoutSession(
            id = "session_${++sessionSequence}",
            createdAtMillis = Clock.System.now().toEpochMilliseconds(),
            sets = sets.toList(),
        )

    private data class RoutineMeta(
        val id: String,
        val name: String,
        val exerciseIdsByDay: Map<WeekDay, List<String>>,
    )

    private companion object {
        const val BASE_MILLIS = 1_720_000_000_000L
        const val DAY_MILLIS = 86_400_000L
    }
}
