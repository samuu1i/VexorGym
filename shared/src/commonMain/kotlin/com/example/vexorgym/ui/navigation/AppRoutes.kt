package com.example.vexorgym.ui.navigation

object AppRoutes {
    const val LOGIN = "login"
    const val WEEKLY_ROUTINE = "weekly_routine"
    const val EXERCISE_DETAIL = "exercise/{exerciseId}"

    fun exerciseDetail(exerciseId: String): String = "exercise/$exerciseId"
}
