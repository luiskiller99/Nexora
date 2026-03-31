package com.example.nexora.data.remote

import com.example.nexora.BuildConfig

data class SupabaseConfig(
    val url: String,
    val apiKey: String
) {
    val isConfigured: Boolean
        get() = url.isNotBlank() && apiKey.isNotBlank()

    companion object {
        fun fromBuildConfig(): SupabaseConfig {
            return SupabaseConfig(
                url = BuildConfig.SUPABASE_URL,
                apiKey = BuildConfig.SUPABASE_KEY
            )
        }
    }
}

class SupabaseClientProvider(
    private val config: SupabaseConfig = SupabaseConfig.fromBuildConfig()
) {
    fun getConfig(): SupabaseConfig = config
    fun isReady(): Boolean = config.isConfigured
}
