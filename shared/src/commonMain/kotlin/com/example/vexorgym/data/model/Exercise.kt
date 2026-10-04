package com.example.vexorgym.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Exercise(
    val id: String,
    val name: String,
    @Transient val muscleGroup: String = "",
    val sessions: List<WorkoutSession> = emptyList(),
)
