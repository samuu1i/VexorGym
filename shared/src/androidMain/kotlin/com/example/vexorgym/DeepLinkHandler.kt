package com.example.vexorgym

import android.content.Intent
import com.example.vexorgym.data.supabaseClient
import com.example.vexorgym.ui.navigation.DeepLinkManager
import io.github.jan.supabase.auth.handleDeeplinks

fun handleSupabaseIntent(intent: Intent?) {
    intent ?: return
    
    try {
        supabaseClient.handleDeeplinks(intent)
    } catch (e: Exception) {
        val isAuthError = e is io.github.jan.supabase.exceptions.RestException ||
                e.message?.lowercase()?.let { 
                    it.contains("invalid") || it.contains("expired") || it.contains("token") 
                } == true
                
        if (isAuthError) {
            DeepLinkManager.deepLinkError.value = "El enlace de recuperación no es válido o ya expiró. Solicitá uno nuevo."
        } else {
            DeepLinkManager.deepLinkError.value = "Hubo un problema al abrir el enlace. Intentá nuevamente."
        }
        return
    }
    
    val dataString = intent.data?.toString() ?: return
    if (dataString.contains("reset-password") || dataString.contains("type=recovery")) {
        DeepLinkManager.navigateToResetPassword.value = true
    }
}