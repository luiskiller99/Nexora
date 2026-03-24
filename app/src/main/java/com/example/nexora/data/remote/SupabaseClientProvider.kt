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
            val url = runCatching {
                BuildConfig::class.java.getField("https://xqildyppsdfnzruakwen.supabase.co").get(null) as? String
            }.getOrNull().orEmpty()

            val key = runCatching {
                BuildConfig::class.java.getField("sb_publishable_j07UF5UZxsNJ9kYSNbqWoQ_rXXxL_Fc").get(null) as? String
            }.getOrNull().orEmpty()

            return SupabaseConfig(url = url, apiKey = key)
        }
    }
}

/**
 * Minimal entry point to provide Supabase configuration without hardcoding secrets.
 * The actual Supabase client implementation can be plugged in later.
 */
class SupabaseClientProvider(
    private val config: SupabaseConfig = SupabaseConfig.fromBuildConfig()
) {
    fun getConfig(): SupabaseConfig = config

    fun isReady(): Boolean = config.isConfigured
}
