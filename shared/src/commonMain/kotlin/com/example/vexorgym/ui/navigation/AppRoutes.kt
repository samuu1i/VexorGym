package com.example.vexorgym.ui.navigation

object AppRoutes {
    const val LOGIN = "login"
    const val WEEKLY_ROUTINE = "weekly_routine"
    const val EXERCISE_DETAIL = "exercise/{exerciseId}"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password"

    fun exerciseDetail(exerciseId: String): String = "exercise/$exerciseId"
}
