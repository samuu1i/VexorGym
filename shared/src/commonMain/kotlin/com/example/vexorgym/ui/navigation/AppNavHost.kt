package com.example.vexorgym.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import com.example.vexorgym.ui.exercise.ExerciseDetailRoute
import com.example.vexorgym.ui.login.ForgotPasswordRoute
import com.example.vexorgym.ui.login.LoginRoute
import com.example.vexorgym.ui.login.ResetPasswordRoute
import com.example.vexorgym.ui.routine.WeeklyRoutineRoute

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val navigateToReset by DeepLinkManager.navigateToResetPassword.collectAsState()

    LaunchedEffect(navigateToReset) {
        if (navigateToReset) {
            navController.navigate(AppRoutes.RESET_PASSWORD) {
                // Remove everything from the backstack to prevent going back to deep link loops
                popUpTo(0) { inclusive = true }
            }
            DeepLinkManager.onResetPasswordHandled()
        }
    }

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
                onForgotPasswordClick = {
                    navController.navigate(AppRoutes.FORGOT_PASSWORD)
                }
            )
        }
        composable(AppRoutes.FORGOT_PASSWORD) {
            ForgotPasswordRoute(
                onBack = { navController.popBackStack() }
            )
        }
        composable(AppRoutes.RESET_PASSWORD) {
            ResetPasswordRoute(
                onPasswordUpdated = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(AppRoutes.WEEKLY_ROUTINE) {
            WeeklyRoutineRoute(
                onExerciseClick = { exerciseId ->
                    navController.navigate(AppRoutes.exerciseDetail(exerciseId))
                },
                onLogoutSuccess = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
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
