package com.example.vexorgym.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth

/**
 * URL del proyecto (sin `/rest/v1/`: el SDK lo agrega).
 * La publishable/anon key es de cliente; RLS en Supabase es lo que protege los datos.
 */
fun createVexorGymSupabaseClient(): SupabaseClient = createSupabaseClient(
    supabaseUrl = "https://tmbwerjcvstfbjcenovv.supabase.co",
    supabaseKey = "sb_publishable_IyOIy-medA5Or3MxOYoLwg_OqW66jg2",
) {
    install(Postgrest)
    install(Auth)
}

val supabaseClient: SupabaseClient = createVexorGymSupabaseClient()
