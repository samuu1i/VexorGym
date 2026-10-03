package com.example.vexorgym.data.model

data class WorkoutSession(
    val id: String,
    val createdAtMillis: Long,
    val sets: List<WorkoutSet>,
)
