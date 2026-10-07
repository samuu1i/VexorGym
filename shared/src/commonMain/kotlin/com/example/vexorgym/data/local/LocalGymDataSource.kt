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

    suspend fun saveExercises(exercises: List<Exercise>) = withContext(Dispatchers.IO) {
        queries.transaction {
            exercises.forEach { exercise ->
                queries.insertOrReplaceExercise(
                    id = exercise.id,
                    name = exercise.name,
                    muscle_group = exercise.muscleGroup
                )
                exercise.sessions.forEach { session ->
                    queries.insertOrReplaceSession(
                        id = session.id,
                        exercise_id = exercise.id,
                        date_created = session.dateCreated
                    )
                    session.sets.forEach { set ->
                        queries.insertOrReplaceSet(
                            id = set.id,
                            session_id = session.id,
                            weight = set.weightKg,
                            reps = set.repetitions.toLong(),
                            date_created = set.dateCreated
                        )
                    }
                }
            }
        }
    }

    suspend fun saveExercise(exercise: Exercise) = withContext(Dispatchers.IO) {
        queries.transaction {
            queries.insertOrReplaceExercise(
                id = exercise.id,
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
                date_created = session.dateCreated
            )
            session.sets.forEach { set ->
                queries.insertOrReplaceSet(
                    id = set.id,
                    session_id = session.id,
                    weight = set.weightKg,
                    reps = set.repetitions.toLong(),
                    date_created = set.dateCreated
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
            date_created = set.dateCreated
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
    
    suspend fun clearAll() = withContext(Dispatchers.IO) {
        queries.transaction {
            queries.deleteAllSets()
            queries.deleteAllSessions()
            queries.deleteAllExercises()
        }
    }
}
