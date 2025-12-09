package io.github.sonicalgo.upstox.config

import io.github.sonicalgo.core.config.WebSocketSdkConfig
import io.github.sonicalgo.upstox.Upstox.*

/**
 * WebSocket configuration for Upstox SDK.
 *
 * Created via [io.github.sonicalgo.upstox.Upstox.createMarketDataFeedClient] or
 * [io.github.sonicalgo.upstox.Upstox.createPortfolioStreamClient] with custom settings.
 *
 * @property maxReconnectAttempts Maximum reconnection attempts (1-20)
 * @property autoReconnectEnabled Enable automatic reconnection on disconnect
 * @property pingIntervalMs Ping interval in milliseconds for keepalive
 * @property autoResubscribeEnabled Enable auto-resubscription after reconnect (market data only)
 */
class UpstoxWebSocketConfig internal constructor(
    override val maxReconnectAttempts: Int,
    override val autoReconnectEnabled: Boolean,
    override val pingIntervalMs: Long = UpstoxConstants.WEBSOCKET_PING_INTERVAL_MS,
    val autoResubscribeEnabled: Boolean = true
) : WebSocketSdkConfig {
    init {
        require(maxReconnectAttempts in 1..20) { "maxReconnectAttempts must be between 1 and 20" }
    }
}
