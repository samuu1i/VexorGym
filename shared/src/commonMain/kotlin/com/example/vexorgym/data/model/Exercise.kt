package com.example.vexorgym.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Exercise(
    val id: String,
    val name: String,
    @SerialName("muscle_group")
    val muscleGroup: String = "General",
    val sessions: List<WorkoutSession> = emptyList(),
)
