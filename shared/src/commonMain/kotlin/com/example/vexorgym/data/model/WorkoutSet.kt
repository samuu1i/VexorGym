package com.example.vexorgym.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Instant

@Serializable
data class WorkoutSet(
    val id: String = "",
    @SerialName("session_id") val sessionId: String = "",
    @SerialName("weight") val weightKg: Double,
    @SerialName("reps") val repetitions: Int,
    @SerialName("created_at") val dateCreated: String? = null,
    @Transient val createdAtMillis: Long = parsePostgresTimestamp(dateCreated),
)
