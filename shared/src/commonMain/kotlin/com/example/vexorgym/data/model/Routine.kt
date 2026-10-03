package com.example.vexorgym.data.model

data class Routine(
    val id: String,
    val name: String,
    val exercisesByDay: Map<WeekDay, List<Exercise>>,
)
