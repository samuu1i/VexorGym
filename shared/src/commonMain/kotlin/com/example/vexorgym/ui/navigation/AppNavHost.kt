package com.example.vexorgym.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import com.example.vexorgym.ui.exercise.ExerciseDetailRoute
import com.example.vexorgym.ui.login.LoginRoute
import com.example.vexorgym.ui.routine.WeeklyRoutineRoute

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.LOGIN,
        modifier = modifier,
    ) {
        composable(AppRoutes.LOGIN) {
            LoginRoute(
                onLoginSuccess = {
                    navController.navigate(AppRoutes.WEEKLY_ROUTINE) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(AppRoutes.WEEKLY_ROUTINE) {
            WeeklyRoutineRoute(
                onExerciseClick = { exerciseId ->
                    navController.navigate(AppRoutes.exerciseDetail(exerciseId))
                },
            )
        }
        composable(
            route = AppRoutes.EXERCISE_DETAIL,
            arguments = listOf(
                navArgument("exerciseId") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.read {
                getStringOrNull("exerciseId")
            }.orEmpty()
            ExerciseDetailRoute(
                exerciseId = exerciseId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
