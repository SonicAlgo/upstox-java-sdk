package io.github.sonicalgo.upstox.exception

import io.github.sonicalgo.upstox.Upstox
import io.github.sonicalgo.upstox.websocket.MarketDataFeedClient
import io.github.sonicalgo.upstox.websocket.PortfolioStreamClient

/**
 * Exception thrown when maximum WebSocket reconnection attempts have been exceeded.
 *
 * This exception is thrown by WebSocket clients ([MarketDataFeedClient], [PortfolioStreamClient])
 * when they have exhausted all reconnection attempts after a disconnection.
 *
 * The number of reconnection attempts can be configured via
 * [Upstox.setMaxWebSocketReconnectAttempts].
 *
 * @param message Descriptive message indicating the failure
 */
class MaxReconnectAttemptsExceededException(message: String) : RuntimeException(message)
