package io.github.sonicalgo.core.config

/**
 * WebSocket client configuration interface.
 * Contains only WebSocket-specific settings, no credentials.
 * Credentials should come from the main SDK instance.
 */
interface WebSocketSdkConfig {
    /** Maximum reconnection attempts (1-20) */
    val maxReconnectAttempts: Int

    /** Enable automatic reconnection on disconnect */
    val autoReconnectEnabled: Boolean

    /** Ping interval in milliseconds for keepalive */
    val pingIntervalMs: Long
}
