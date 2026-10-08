package com.example.vexorgym.data.local

import com.example.vexorgym.data.model.Exercise
import com.example.vexorgym.data.model.WorkoutSession
import com.example.vexorgym.data.model.WorkoutSet
import com.example.vexorgym.data.model.WeekDay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class LocalGymDataSource(private val db: GymDatabase) {
    private val queries = db.gymDatabaseQueries

    suspend fun getExercises(userId: String): List<Exercise> = withContext(Dispatchers.IO) {
        val exercises = queries.getExercisesByUserId(userId).executeAsList()
        exercises.map { ext ->
            val sessions = queries.getSessionsByExerciseId(ext.id).executeAsList().map { sess ->
                val sets = queries.getSetsBySessionId(sess.id).executeAsList().map { set ->
                    WorkoutSet(
                        id = set.id,
                        sessionId = set.session_id,
                        weightKg = set.weight,
                        repetitions = set.reps.toInt(),
                        dateCreated = set.date_created,
                        isPending = set.is_pending != 0L
                    )
                }.sortedBy { it.createdAtMillis }
                
                WorkoutSession(
                    id = sess.id,
                    exerciseId = sess.exercise_id,
                    dateCreated = sess.date_created,
                    sets = sets
                )
            }.sortedBy { it.createdAtMillis }
            
            Exercise(
                id = ext.id,
                name = ext.name,
                muscleGroup = ext.muscle_group,
                sessions = sessions
            )
        }
    }

    suspend fun getUserRoutine(userId: String): Map<WeekDay, List<String>>? = withContext(Dispatchers.IO) {
        val row = queries.getUserRoutine(userId).executeAsOneOrNull()
        row?.let {
            Json.decodeFromString(it.routine_data)
        }
    }

    suspend fun getPendingSessions(userId: String): List<WorkoutSession> = withContext(Dispatchers.IO) {
        queries.getPendingSessionsByUserId(userId).executeAsList().map { sess ->
            WorkoutSession(
                id = sess.id,
                exerciseId = sess.exercise_id,
                dateCreated = sess.date_created,
                sets = emptyList(),
                isPending = sess.is_pending != 0L
            )
        }
    }

    suspend fun markSessionSynced(sessionId: String) = withContext(Dispatchers.IO) {
        queries.markSessionSynced(sessionId)
    }

    suspend fun getPendingSets(userId: String): List<WorkoutSet> = withContext(Dispatchers.IO) {
        queries.getPendingSetsByUserId(userId).executeAsList().map { set ->
            WorkoutSet(
                id = set.id,
                sessionId = set.session_id,
                weightKg = set.weight,
                repetitions = set.reps.toInt(),
                dateCreated = set.date_created,
                isPending = set.is_pending != 0L
            )
        }
    }

    suspend fun markSetSynced(setId: String) = withContext(Dispatchers.IO) {
        queries.markSetSynced(setId)
    }

    suspend fun saveExercises(userId: String, exercises: List<Exercise>) = withContext(Dispatchers.IO) {
        queries.transaction {
            exercises.forEach { exercise ->
                queries.insertOrReplaceExercise(
                    id = exercise.id,
                    user_id = userId,
                    name = exercise.name,
                    muscle_group = exercise.muscleGroup
                )
                exercise.sessions.forEach { session ->
                    queries.insertOrReplaceSession(
                        id = session.id,
                        exercise_id = exercise.id,
                        date_created = session.dateCreated,
                        is_pending = if (session.isPending) 1L else 0L
                    )
                    session.sets.forEach { set ->
                        queries.insertOrReplaceSet(
                            id = set.id,
                            session_id = session.id,
                            weight = set.weightKg,
                            reps = set.repetitions.toLong(),
                            date_created = set.dateCreated,
                            is_pending = if (set.isPending) 1L else 0L
                        )
                    }
                }
            }
        }
    }

    suspend fun saveExercise(userId: String, exercise: Exercise) = withContext(Dispatchers.IO) {
        queries.transaction {
            queries.insertOrReplaceExercise(
                id = exercise.id,
                user_id = userId,
                name = exercise.name,
                muscle_group = exercise.muscleGroup
            )
        }
    }

    suspend fun deleteExercise(id: String) = withContext(Dispatchers.IO) {
        queries.deleteExerciseById(id)
    }

    suspend fun saveSession(session: WorkoutSession) = withContext(Dispatchers.IO) {
        queries.transaction {
            queries.insertOrReplaceSession(
                id = session.id,
                exercise_id = session.exerciseId,
                date_created = session.dateCreated,
                is_pending = if (session.isPending) 1L else 0L
            )
            session.sets.forEach { set ->
                queries.insertOrReplaceSet(
                    id = set.id,
                    session_id = session.id,
                    weight = set.weightKg,
                    reps = set.repetitions.toLong(),
                    date_created = set.dateCreated,
                    is_pending = if (set.isPending) 1L else 0L
                )
            }
        }
    }

    suspend fun deleteSession(id: String) = withContext(Dispatchers.IO) {
        queries.deleteSessionById(id)
    }

    suspend fun saveSet(set: WorkoutSet) = withContext(Dispatchers.IO) {
        queries.insertOrReplaceSet(
            id = set.id,
            session_id = set.sessionId,
            weight = set.weightKg,
            reps = set.repetitions.toLong(),
            date_created = set.dateCreated,
            is_pending = if (set.isPending) 1L else 0L
        )
    }

    suspend fun deleteSet(id: String) = withContext(Dispatchers.IO) {
        queries.deleteSetById(id)
    }

    suspend fun saveUserRoutine(userId: String, routineData: Map<WeekDay, List<String>>) = withContext(Dispatchers.IO) {
        val json = Json.encodeToString(routineData)
        queries.insertOrReplaceUserRoutine(userId, json)
    }
    
    suspend fun clearUserRoutine(userId: String) = withContext(Dispatchers.IO) {
        queries.deleteUserRoutine(userId)
    }
    
    suspend fun clearUserData(userId: String) = withContext(Dispatchers.IO) {
        queries.transaction {
            queries.deleteExercisesByUserId(userId)
            queries.deleteUserRoutine(userId)
        }
    }
}
