package com.example.vexorgym

import android.content.Intent
import com.example.vexorgym.data.supabaseClient
import com.example.vexorgym.ui.navigation.DeepLinkManager
import io.github.jan.supabase.auth.handleDeeplinks

fun handleSupabaseIntent(intent: Intent?) {
    intent ?: return
    supabaseClient.handleDeeplinks(intent)
    
    val dataString = intent.data?.toString() ?: return
    if (dataString.contains("reset-password") || dataString.contains("type=recovery")) {
        DeepLinkManager.navigateToResetPassword.value = true
    }
}