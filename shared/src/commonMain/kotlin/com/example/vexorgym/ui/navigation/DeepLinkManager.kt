package com.example.vexorgym.ui.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

object DeepLinkManager {
    val navigateToResetPassword = MutableStateFlow(false)
    val deepLinkError = MutableStateFlow<String?>(null)

    fun onResetPasswordHandled() {
        navigateToResetPassword.update { false }
    }

    fun onErrorHandled() {
        deepLinkError.update { null }
    }
}