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

    private val activeOperations = Mutex()
    private val inFlight = mutableSetOf<String>()

    private suspend fun acquireOp(key: String): Boolean = activeOperations.withLock {
        if (inFlight.contains(key)) false else { inFlight.add(key); true }
    }
    private suspend fun releaseOp(key: String) = activeOperations.withLock {
        inFlight.remove(key)
    }

    private fun generateUUID(): String {
        val chars = "0123456789abcdef"
        return CharArray(36) { i ->
            when (i) {
                8, 13, 18, 23 -> '-'
                14 -> '4'
                19 -> chars[kotlin.random.Random.nextInt(4) + 8]
                else -> chars[kotlin.random.Random.nextInt(16)]
            }
        }.concatToString()
    }

    private fun generateSetId(): String {
        return generateUUID()
    }

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

    private suspend fun saveRoutine(newMap: Map<WeekDay, List<String>>) {
        try {
            val uid = client.auth.currentUserOrNull()?.id ?: return
            client.from("user_routines").upsert(UserRoutine(userId = uid, routineData = newMap))
        } catch (e: Exception) {
            println("Error saving routine: ${e.message}")
        }
    }

    override suspend fun addExerciseToDay(day: WeekDay, exerciseId: String): Result<Unit> {
        if (exercisesCache.value[exerciseId] == null) {
            return Result.failure(IllegalArgumentException("El ejercicio no existe."))
        }
        val ids = routineMeta.value.exerciseIdsByDay[day].orEmpty()
        if (exerciseId in ids) {
            return Result.failure(IllegalArgumentException("Ese ejercicio ya está en ${day.displayName}."))
        }
        val newMap = routineMeta.value.exerciseIdsByDay + (day to ids + exerciseId)
        routineMeta.update { current ->
            current.copy(exerciseIdsByDay = newMap)
        }
        saveRoutine(newMap)
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
        return try {
            val created = client.from("exercises")
                .insert(ExerciseInsert(name = trimmedName, muscleGroup = finalGroup)) {
                    select()
                }
                .decodeSingle<Exercise>()
                
            var newMapToSave: Map<WeekDay, List<String>>? = null
            mutex.withLock {
                exercisesCache.update { cache -> cache + (created.id to created) }
                val ids = routineMeta.value.exerciseIdsByDay[day].orEmpty()
                newMapToSave = routineMeta.value.exerciseIdsByDay + (day to ids + created.id)
                routineMeta.update { current ->
                    current.copy(exerciseIdsByDay = newMapToSave!!)
                }
            }
            saveRoutine(newMapToSave!!)
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(Exception("Error al crear el ejercicio: ${error.message}"))
        }
    }

    override suspend fun removeExerciseFromDay(day: WeekDay, exerciseId: String): Result<Unit> {
        val opKey = "delExercise_$exerciseId"
        if (!acquireOp(opKey)) return Result.success(Unit)
        
        // 1. Encontramos la rutina actual y preparamos el nuevo estado sin el ID a borrar
        val currentMeta = routineMeta.value
        val currentIdsForDay = currentMeta.exerciseIdsByDay[day].orEmpty()
        if (exerciseId !in currentIdsForDay) {
            releaseOp(opKey)
            return Result.success(Unit) // Ya no está
        }
        
        val newIdsForDay = currentIdsForDay.filterNot { it == exerciseId }
        val newMap = currentMeta.exerciseIdsByDay + (day to newIdsForDay)
        val newMeta = currentMeta.copy(
            exerciseIdsByDay = newMap
        )
        
        // 2. Actualización optimista
        routineMeta.value = newMeta
        
        return try {
            // Borramos el ejercicio de la base de datos de Supabase.
            // Gracias a ON DELETE CASCADE, también se borran sus sesiones y series.
            client.from("exercises").delete { filter { eq("id", exerciseId) } }
            
            // Si la base de datos respondió OK, eliminamos localmente del caché para no verlo más
            mutex.withLock {
                exercisesCache.update { current -> current - exerciseId }
            }
            saveRoutine(newMap)
            Result.success(Unit)
        } catch (error: Exception) {
            // 3. Rollback
            routineMeta.value = currentMeta
            Result.failure(Exception("Error al eliminar el ejercicio: ${error.message}"))
        } finally {
            releaseOp(opKey)
        }
    }

    override suspend fun updateRoutineOrder(day: WeekDay, orderedIds: List<String>): Result<Unit> {
        val currentMeta = routineMeta.value
        val newMap = currentMeta.exerciseIdsByDay + (day to orderedIds)
        routineMeta.value = currentMeta.copy(exerciseIdsByDay = newMap)
        
        return try {
            val uid = client.auth.currentUserOrNull()?.id ?: return Result.failure(Exception("No session"))
            client.from("user_routines").upsert(UserRoutine(userId = uid, routineData = newMap))
            Result.success(Unit)
        } catch (e: Exception) {
            routineMeta.value = currentMeta
            Result.failure(Exception("Error al guardar el orden: ${e.message}"))
        }
    }

    override suspend fun addSession(exerciseId: String): Result<Unit> {
        val opKey = "addSession_$exerciseId"
        if (!acquireOp(opKey)) return Result.success(Unit) // Evitar doble click

        val sessionId = generateUUID()
        val optimisticSession = WorkoutSession(
            id = sessionId,
            exerciseId = exerciseId,
            dateCreated = kotlin.time.Clock.System.now().toString(),
            sets = emptyList()
        )

        // 1. Actualización Optimista local instantánea
        mutex.withLock {
            exercisesCache.update { cache ->
                val exercise = cache[exerciseId] ?: return@update cache
                val updatedSessions = exercise.sessions + optimisticSession
                cache + (exerciseId to exercise.copy(sessions = updatedSessions.sortedBy { it.createdAtMillis }))
            }
        }

        return try {
            // 2. Operación de red en segundo plano (sin decodeSingle, fire-and-forget con confirmación)
            client.from("sessions").insert(SessionInsert(id = sessionId, exerciseId = exerciseId))
            Result.success(Unit)
        } catch (error: Exception) {
            // 3. Rollback exacto en caso de fallo (RLS, Red, etc)
            mutex.withLock {
                exercisesCache.update { cache ->
                    val exercise = cache[exerciseId] ?: return@update cache
                    val rolledBackSessions = exercise.sessions.filterNot { it.id == sessionId }
                    cache + (exerciseId to exercise.copy(sessions = rolledBackSessions))
                }
            }
            Result.failure(Exception("Error al crear la sesión: ${error.message}"))
        } finally {
            releaseOp(opKey)
        }
    }

    override suspend fun deleteSession(exerciseId: String, sessionId: String): Result<Unit> {
        val opKey = "delSession_$sessionId"
        if (!acquireOp(opKey)) return Result.success(Unit)

        var deletedSession: WorkoutSession? = null
        
        // 1. Actualización Optimista
        mutex.withLock {
            exercisesCache.update { cache ->
                val exercise = cache[exerciseId] ?: return@update cache
                deletedSession = exercise.sessions.find { it.id == sessionId }
                if (deletedSession == null) return@update cache
                val updatedSessions = exercise.sessions.filterNot { it.id == sessionId }
                cache + (exerciseId to exercise.copy(sessions = updatedSessions))
            }
        }

        val capturedSession = deletedSession
        if (capturedSession == null) {
            releaseOp(opKey)
            return Result.success(Unit)
        }

        return try {
            // 2. Red
            client.from("sessions").delete { filter { eq("id", sessionId) } }
            Result.success(Unit)
        } catch (error: Exception) {
            // 3. Rollback: Volvemos a insertar la sesión eliminada optimísticamente
            mutex.withLock {
                exercisesCache.update { cache ->
                    val exercise = cache[exerciseId] ?: return@update cache
                    val rolledBackSessions = (exercise.sessions + capturedSession).sortedBy { it.createdAtMillis }
                    cache + (exerciseId to exercise.copy(sessions = rolledBackSessions))
                }
            }
            Result.failure(Exception("Error al eliminar la sesión: ${error.message}"))
        } finally {
            releaseOp(opKey)
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
        val opKey = "addSet_$sessionId"
        if (!acquireOp(opKey)) return Result.success(Unit)

        val setId = generateSetId()
        val optimisticSet = com.example.vexorgym.data.model.WorkoutSet(
            id = setId,
            sessionId = sessionId,
            weightKg = weightKg,
            repetitions = repetitions,
            dateCreated = kotlin.time.Clock.System.now().toString()
        )

        // 1. Actualización Optimista
        mutex.withLock {
            exercisesCache.update { cache ->
                val exercise = cache[exerciseId] ?: return@update cache
                val updatedSessions = exercise.sessions.map { session ->
                    if (session.id == sessionId) {
                        session.copy(sets = (session.sets + optimisticSet).sortedBy { it.createdAtMillis })
                    } else {
                        session
                    }
                }
                cache + (exerciseId to exercise.copy(sessions = updatedSessions))
            }
        }

        return try {
            // 2. Red
            client.from("sets").insert(
                SetInsert(id = setId, sessionId = sessionId, weight = weightKg, reps = repetitions)
            )
            Result.success(Unit)
        } catch (error: Exception) {
            // 3. Rollback
            mutex.withLock {
                exercisesCache.update { cache ->
                    val exercise = cache[exerciseId] ?: return@update cache
                    val updatedSessions = exercise.sessions.map { session ->
                        if (session.id == sessionId) {
                            session.copy(sets = session.sets.filterNot { it.id == setId })
                        } else {
                            session
                        }
                    }
                    cache + (exerciseId to exercise.copy(sessions = updatedSessions))
                }
            }
            Result.failure(Exception("Error al agregar la serie: ${error.message}"))
        } finally {
            releaseOp(opKey)
        }
    }

    override suspend fun deleteSet(
        exerciseId: String,
        sessionId: String,
        setId: String,
    ): Result<Unit> {
        val opKey = "delSet_$setId"
        if (!acquireOp(opKey)) return Result.success(Unit)

        var deletedSet: com.example.vexorgym.data.model.WorkoutSet? = null

        // 1. Actualización Optimista
        mutex.withLock {
            exercisesCache.update { cache ->
                val exercise = cache[exerciseId] ?: return@update cache
                val sessionTarget = exercise.sessions.find { it.id == sessionId }
                deletedSet = sessionTarget?.sets?.find { it.id == setId }
                if (deletedSet == null) return@update cache

                val updatedSessions = exercise.sessions.map { session ->
                    if (session.id == sessionId) {
                        session.copy(sets = session.sets.filterNot { it.id == setId })
                    } else {
                        session
                    }
                }
                cache + (exerciseId to exercise.copy(sessions = updatedSessions))
            }
        }

        val capturedSet = deletedSet
        if (capturedSet == null) {
            releaseOp(opKey)
            return Result.success(Unit)
        }

        return try {
            // 2. Red
            client.from("sets").delete { filter { eq("id", setId) } }
            Result.success(Unit)
        } catch (error: Exception) {
            // 3. Rollback
            mutex.withLock {
                exercisesCache.update { cache ->
                    val exercise = cache[exerciseId] ?: return@update cache
                    val updatedSessions = exercise.sessions.map { session ->
                        if (session.id == sessionId) {
                            session.copy(sets = (session.sets + capturedSet).sortedBy { it.createdAtMillis })
                        } else {
                            session
                        }
                    }
                    cache + (exerciseId to exercise.copy(sessions = updatedSessions))
                }
            }
            Result.failure(Exception("Error al eliminar la serie: ${error.message}"))
        } finally {
            releaseOp(opKey)
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
                                    // Para que Compose y la UI muestren el orden correcto, 
                                    // ordenamos los sets por su ID de inserción original en DB o UUID
                                    compareBy<com.example.vexorgym.data.model.WorkoutSet> { it.createdAtMillis },
                                ),
                            )
                        }
                        .sortedBy { it.createdAtMillis }
                    exercise.id to exercise.copy(sessions = sessions)
                }
                
                val routineRows = try {
                    client.from("user_routines").select().decodeList<UserRoutine>()
                } catch (e: Exception) {
                    emptyList()
                }
                val userMap = routineRows.firstOrNull()?.routineData
                
                exercisesCache.value = mapped
                
                if (userMap != null) {
                    routineMeta.update { current -> current.copy(exerciseIdsByDay = userMap) }
                    routineSeeded = true
                } else {
                    seedRoutineIfNeeded(mapped.keys.toList())
                }
                
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
private data class UserRoutine(
    @SerialName("user_id") val userId: String,
    @SerialName("routine_data") val routineData: Map<WeekDay, List<String>>
)

@Serializable
private data class ExerciseInsert(
    val name: String,
    @SerialName("muscle_group") val muscleGroup: String
)

@Serializable
private data class SessionInsert(
    val id: String,
    @SerialName("exercise_id") val exerciseId: String,
)

@Serializable
private data class SetInsert(
    val id: String,
    @SerialName("session_id") val sessionId: String,
    val weight: Double,
    val reps: Int,
)
