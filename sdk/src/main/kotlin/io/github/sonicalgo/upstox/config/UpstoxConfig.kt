package io.github.sonicalgo.upstox.config

import io.github.sonicalgo.upstox.Upstox

/**
 * Internal configuration storage for the Upstox SDK.
 *
 * Public API is exposed via [Upstox].
 */
internal object UpstoxConfig {

    // ==================== Authentication ====================

    /** OAuth access token. */
    @Volatile
    var accessToken: String = ""

    /** Sandbox token for testing order operations. */
    @Volatile
    var sandboxToken: String = ""

    /** Whether sandbox mode is enabled. */
    @Volatile
    var sandboxEnabled: Boolean = false

    // ==================== HTTP Configuration ====================

    /** Rate limit retry attempts (0-5). */
    @Volatile
    var rateLimitRetries: Int = 0
        set(value) {
            require(value in 0..5) { "rateLimitRetries must be between 0 and 5" }
            field = value
        }

    /** HTTP logging enabled flag. */
    @Volatile
    var loggingEnabled: Boolean = false
        set(value) {
            field = value
            OkHttpClientFactory.rebuildClientIfNeeded()
        }

    /** Max WebSocket reconnection attempts (1-20). */
    @Volatile
    var maxWebSocketReconnectAttempts: Int = UpstoxConstants.WEBSOCKET_DEFAULT_MAX_RECONNECT_ATTEMPTS
        set(value) {
            require(value in 1..20) { "maxWebSocketReconnectAttempts must be between 1 and 20" }
            field = value
        }

    /** Whether WebSocket auto-reconnect is enabled globally. */
    @Volatile
    var webSocketAutoReconnectEnabled: Boolean = true

    /** Resets all configuration to defaults. */
    fun resetToDefaults() {
        // Authentication
        accessToken = ""
        sandboxToken = ""
        sandboxEnabled = false

        // HTTP configuration
        rateLimitRetries = 0
        loggingEnabled = false

        // WebSocket configuration
        maxWebSocketReconnectAttempts = UpstoxConstants.WEBSOCKET_DEFAULT_MAX_RECONNECT_ATTEMPTS
        webSocketAutoReconnectEnabled = true
    }
}
