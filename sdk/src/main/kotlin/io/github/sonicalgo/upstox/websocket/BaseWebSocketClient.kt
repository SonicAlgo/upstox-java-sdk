package io.github.sonicalgo.upstox.websocket

import io.github.sonicalgo.upstox.config.UpstoxConfig
import io.github.sonicalgo.upstox.config.UpstoxConstants
import io.github.sonicalgo.upstox.config.UpstoxWebSocketConfig
import okhttp3.OkHttpClient
import io.github.sonicalgo.core.websocket.BaseWebSocketClient as CoreBaseWebSocketClient

/**
 * Base class for Upstox WebSocket clients.
 *
 * Provides Upstox-specific WebSocket functionality:
 * - Access to [UpstoxConfig] for credentials (accessToken)
 * - Credential validation helper
 * - Upstox-specific reconnection delays
 *
 * @param httpClient OkHttpClient configured for WebSocket connections
 * @param upstoxConfig Upstox configuration for credentials
 * @param wsConfig WebSocket-specific configuration
 * @param clientName Name used for the reconnection thread
 */
abstract class BaseWebSocketClient(
    httpClient: OkHttpClient,
    protected val upstoxConfig: UpstoxConfig,
    protected val wsConfig: UpstoxWebSocketConfig,
    clientName: String
) : CoreBaseWebSocketClient(
    httpClient = httpClient,
    config = wsConfig,
    clientName = clientName,
    initialReconnectDelayMs = UpstoxConstants.WEBSOCKET_RECONNECT_INITIAL_DELAY_MS,
    maxReconnectDelayMs = UpstoxConstants.WEBSOCKET_RECONNECT_MAX_DELAY_MS
) {

    /**
     * Checks if credentials (accessToken) are missing or blank.
     *
     * @return true if credentials are missing, false if valid
     */
    protected fun hasCredentialsError(): Boolean {
        return upstoxConfig.accessToken.isBlank()
    }
}
