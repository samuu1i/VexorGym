package com.example.vexorgym.data.repository

import com.example.vexorgym.data.model.Exercise
import com.example.vexorgym.data.model.Routine
import com.example.vexorgym.data.model.WeekDay
import com.example.vexorgym.data.model.WorkoutSession
import com.example.vexorgym.data.supabaseClient
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class RemoteGymRepository(
    private val client: SupabaseClient = supabaseClient,
) : GymRepository {

    private val mutex = Mutex()
    private var cacheLoaded = false
    private var routineSeeded = false

    private val exercisesCache = MutableStateFlow<Map<String, Exercise>>(emptyMap())
    private val routineMeta = MutableStateFlow(
        RoutineMeta(
            id = "routine_remote",
            name = "Rutina",
            exerciseIdsByDay = WeekDay.entries.associateWith { emptyList() },
        ),
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            refreshAll(force = true)
            Result.success(Unit)
        } catch (e: Exception) {
            val msg = e.message ?: ""
            val userError = if ("Email not confirmed" in msg || "not confirmed" in msg) {
                "Debes confirmar tu correo antes de iniciar sesión."
            } else if ("Invalid login credentials" in msg || "invalid" in msg.lowercase()) {
                "Credenciales incorrectas."
            } else {
                "Error al iniciar sesión."
            }
            Result.failure(Exception(userError))
        }
    }

    override suspend fun register(email: String, password: String): Result<Boolean> {
        return try {
            val user = client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            if (user != null && user.identities.isNullOrEmpty()) {
                return Result.failure(Exception("El correo ya está registrado. Iniciá sesión."))
            }
            val hasSession = client.auth.currentSessionOrNull() != null
            if (hasSession) {
                refreshAll(force = true)
            }
            Result.success(hasSession)
        } catch (e: Exception) {
            Result.failure(Exception("Error al registrarse: ${e.message}"))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            client.auth.signOut()
            mutex.withLock {
                exercisesCache.value = emptyMap()
                routineMeta.value = routineMeta.value.copy(
                    exerciseIdsByDay = WeekDay.entries.associateWith { emptyList() }
                )
                cacheLoaded = false
                routineSeeded = false
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun hasValidSession(): Boolean {
        client.auth.awaitInitialization()
        return client.auth.currentSessionOrNull() != null
    }

    override fun observeRoutine(): Flow<Routine> = flow {
        refreshAll()
        emitAll(
            combine(exercisesCache, routineMeta) { exercises, meta ->
                Routine(
                    id = meta.id,
                    name = meta.name,
                    exercisesByDay = WeekDay.entries.associateWith { day ->
                        meta.exerciseIdsByDay[day].orEmpty().mapNotNull { id ->
                            exercises[id]
                        }
                    },
                )
            },
        )
    }

    override fun observeCatalog(): Flow<List<Exercise>> = flow {
        refreshAll()
        emitAll(
            exercisesCache.map { exercises ->
                exercises.values.sortedBy { it.name }
            }
        )
    }

    override fun observeExercise(exerciseId: String): Flow<Exercise?> = flow {
        refreshAll()
        emitAll(
            exercisesCache.map { exercises ->
                exercises[exerciseId]
            }
        )
    }

    override suspend fun addExerciseToDay(day: WeekDay, exerciseId: String): Result<Unit> {
        if (exercisesCache.value[exerciseId] == null) {
            return Result.failure(IllegalArgumentException("El ejercicio no existe."))
        }
        val ids = routineMeta.value.exerciseIdsByDay[day].orEmpty()
        if (exerciseId in ids) {
            return Result.failure(IllegalArgumentException("Ese ejercicio ya está en ${day.displayName}."))
        }
        routineMeta.update { current ->
            current.copy(exerciseIdsByDay = current.exerciseIdsByDay + (day to ids + exerciseId))
        }
        return Result.success(Unit)
    }

    override suspend fun createExerciseForDay(
        day: WeekDay,
        name: String,
        muscleGroup: String,
    ): Result<Unit> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("El nombre del ejercicio no puede estar vacío."))
        }
        val finalGroup = muscleGroup.trim().ifEmpty { "General" }
        return mutate {
            val created = client.from("exercises")
                .insert(ExerciseInsert(name = trimmedName, muscleGroup = finalGroup)) {
                    select()
                }
                .decodeSingle<Exercise>()
            val ids = routineMeta.value.exerciseIdsByDay[day].orEmpty()
            routineMeta.update { current ->
                current.copy(exerciseIdsByDay = current.exerciseIdsByDay + (day to ids + created.id))
            }
        }
    }

    override suspend fun removeExerciseFromDay(day: WeekDay, exerciseId: String): Result<Unit> {
        routineMeta.update { current ->
            val ids = current.exerciseIdsByDay[day].orEmpty().filterNot { it == exerciseId }
            current.copy(exerciseIdsByDay = current.exerciseIdsByDay + (day to ids))
        }
        return Result.success(Unit)
    }

    override suspend fun addSession(exerciseId: String): Result<Unit> = mutate {
        client.from("sessions").insert(SessionInsert(exerciseId = exerciseId))
    }

    override suspend fun deleteSession(exerciseId: String, sessionId: String): Result<Unit> = mutate {
        client.from("sessions").delete {
            filter { eq("id", sessionId) }
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
        return mutate {
            client.from("sets").insert(
                SetInsert(
                    sessionId = sessionId,
                    weight = weightKg,
                    reps = repetitions,
                ),
            )
        }
    }

    override suspend fun deleteSet(
        exerciseId: String,
        sessionId: String,
        setId: String,
    ): Result<Unit> = mutate {
        client.from("sets").delete {
            filter { eq("id", setId) }
        }
    }

    private suspend fun mutate(block: suspend () -> Unit): Result<Unit> {
        return try {
            _isLoading.value = true
            _error.value = null
            block()
            refreshAll(force = true)
            Result.success(Unit)
        } catch (error: Exception) {
            val message = error.message ?: "Error al hablar con Supabase."
            _error.value = message
            Result.failure(error)
        } finally {
            _isLoading.value = false
        }
    }

    private suspend fun refreshAll(force: Boolean = false) {
        mutex.withLock {
            if (cacheLoaded && !force) return
            _isLoading.value = true
            try {
                val rows = client.from("exercises")
                    .select(Columns.raw("*, sessions(*, sets(*))"))
                    .decodeList<Exercise>()
                val mapped = rows.associate { exercise ->
                    val sessions = exercise.sessions
                        .map { session ->
                            session.copy(
                                sets = session.sets.sortedWith(
                                    compareBy<com.example.vexorgym.data.model.WorkoutSet> { it.id },
                                ),
                            )
                        }
                        .sortedBy { it.createdAtMillis }
                    exercise.id to exercise.copy(sessions = sessions)
                }
                exercisesCache.value = mapped
                seedRoutineIfNeeded(mapped.keys.toList())
                cacheLoaded = true
                _error.value = null
            } catch (error: Exception) {
                _error.value = error.message ?: "Error al conectar con el servidor."
                if (!cacheLoaded) {
                    exercisesCache.value = emptyMap()
                }
                throw error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun seedRoutineIfNeeded(exerciseIds: List<String>) {
        if (routineSeeded) return
        routineSeeded = true
        val hasAssignments = routineMeta.value.exerciseIdsByDay.values.any { it.isNotEmpty() }
        if (!hasAssignments && exerciseIds.isNotEmpty()) {
            routineMeta.update { current ->
                current.copy(
                    exerciseIdsByDay = current.exerciseIdsByDay + (WeekDay.MONDAY to exerciseIds),
                )
            }
        }
    }

    private data class RoutineMeta(
        val id: String,
        val name: String,
        val exerciseIdsByDay: Map<WeekDay, List<String>>,
    )
}

@Serializable
private data class ExerciseInsert(
    val name: String,
    @SerialName("muscle_group") val muscleGroup: String
)

@Serializable
private data class SessionInsert(
    @SerialName("exercise_id") val exerciseId: String,
)

@Serializable
private data class SetInsert(
    @SerialName("session_id") val sessionId: String,
    val weight: Double,
    val reps: Int,
)
