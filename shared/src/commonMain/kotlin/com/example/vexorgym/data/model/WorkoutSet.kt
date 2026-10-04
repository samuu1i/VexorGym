package com.example.vexorgym.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkoutSet(
    val id: String = "",
    @SerialName("session_id") val sessionId: String = "",
    @SerialName("weight") val weightKg: Double,
    @SerialName("reps") val repetitions: Int,
)
