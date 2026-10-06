package com.example.vexorgym

import androidx.compose.ui.window.ComposeUIViewController
import com.example.vexorgym.data.supabaseClient
import com.example.vexorgym.ui.navigation.DeepLinkManager
import io.github.jan.supabase.auth.handleDeeplinks
import platform.Foundation.NSURL

fun MainViewController() = ComposeUIViewController { App() }

fun handleSupabaseUrl(url: NSURL) {
    supabaseClient.handleDeeplinks(url)
    
    val dataString = url.absoluteString
    if (dataString?.contains("reset-password") == true || dataString?.contains("type=recovery") == true) {
        DeepLinkManager.navigateToResetPassword.value = true
    }
}