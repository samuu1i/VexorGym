package com.example.vexorgym.ui.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

object DeepLinkManager {
    val navigateToResetPassword = MutableStateFlow(false)

    fun onResetPasswordHandled() {
        navigateToResetPassword.update { false }
    }
}