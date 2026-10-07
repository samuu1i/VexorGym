package com.example.vexorgym

import androidx.compose.ui.window.ComposeUIViewController
import com.example.vexorgym.data.supabaseClient
import com.example.vexorgym.ui.navigation.DeepLinkManager
import com.example.vexorgym.di.AppContainer
import com.example.vexorgym.data.local.DatabaseDriverFactory
import io.github.jan.supabase.auth.handleDeeplinks
import platform.Foundation.NSURL

fun MainViewController() = ComposeUIViewController {
    AppContainer.init(DatabaseDriverFactory().createDriver())
    App()
}

fun handleSupabaseUrl(url: NSURL) {
    supabaseClient.handleDeeplinks(url)
    
    val dataString = url.absoluteString
    if (dataString?.contains("reset-password") == true || dataString?.contains("type=recovery") == true) {
        DeepLinkManager.navigateToResetPassword.value = true
    }
}