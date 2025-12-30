package io.github.sonicalgo.upstox.websocket.portfolio

import io.github.sonicalgo.core.client.HttpClient
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConfig
import io.github.sonicalgo.upstox.usecase.executeAuthorizePortfolioStream
import io.github.sonicalgo.upstox.config.UpstoxWebSocketConfig
import io.github.sonicalgo.upstox.websocket.BaseWebSocketClient
import okhttp3.OkHttpClient
import okio.ByteString
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Client for real-time portfolio updates via WebSocket.
 *
 * Provides real-time updates for orders, positions, holdings, and GTT orders
 * with automatic reconnection support.
 *
 * ## Features
 * - Automatic reconnection with exponential backoff on disconnection
 * - Update type tracking - update types are automatically restored after reconnect
 * - Multiple listener support - add/remove listeners at any time
 * - Ping/pong keepalive (configured at OkHttpClient level)
 * - Thread-safe state management
 *
 * ## Example usage
 * ```kotlin
 * val upstox = Upstox.builder()
 *     .accessToken("your-token")
 *     .build()
 *
 * val client = upstox.createPortfolioStreamClient()
 *
 * // Add listener
 * client.addListener(object : PortfolioStreamListener {
 *     override fun onConnected() {
 *         println("Connected to portfolio stream")
 *     }
 *     override fun onDisconnected(code: Int, reason: String) {
 *         println("Disconnected: $reason")
 *     }
 *     override fun onError(error: Throwable) {
 *         error.printStackTrace()
 *     }
 *
 *     // Optional: handle reconnection
 *     override fun onReconnected() {
 *         println("Reconnected successfully!")
 *     }
 *
 *     // Only override the data callbacks you need
 *     override fun onOrderUpdate(order: OrderUpdate) {
 *         println("Order update: ${order.orderId} - ${order.status}")
 *     }
 *     override fun onPositionUpdate(position: PositionUpdate) {
 *         println("Position update: ${position.tradingSymbol}")
 *     }
 * })
 *
 * // Connect
 * client.connect()
 *
 * // Later, to stop reconnection attempts and disconnect:
 * client.close()
 * ```
 *
 * @see PortfolioStreamListener
 * @see <a href="https://upstox.com/developer/api-documentation/get-portfolio-stream-feed">Portfolio Stream API</a>
 */
class PortfolioStreamClient internal constructor(
    upstoxConfig: UpstoxConfig,
    wsConfig: UpstoxWebSocketConfig,
    private val apiClient: ApiClient,
    wsHttpClient: OkHttpClient
) : BaseWebSocketClient(wsHttpClient, upstoxConfig, wsConfig, "PortfolioStream") {

    private val listeners = CopyOnWriteArrayList<PortfolioStreamListener>()

    // Update types to restore after reconnection
    @Volatile
    private var updateTypes: Set<PortfolioUpdateType> = PortfolioUpdateType.entries.toSet()

    /**
     * Adds a listener to receive portfolio stream events.
     *
     * @param listener Listener to add
     */
    fun addListener(listener: PortfolioStreamListener) {
        listeners.add(listener)
    }

    /**
     * Removes a listener.
     *
     * @param listener Listener to remove
     */
    fun removeListener(listener: PortfolioStreamListener) {
        listeners.remove(listener)
    }

    /**
     * Connect to the portfolio stream WebSocket.
     *
     * Automatically obtains the authorized WebSocket URL before connecting.
     * If auto-reconnect is enabled, the client will automatically
     * attempt to reconnect on disconnection with exponential backoff.
     *
     * @param updateTypes Types of updates to subscribe to. Defaults to all types.
     * @param autoReconnect Whether to automatically reconnect on disconnection (default: from config)
     */
    @JvmOverloads
    fun connect(
        updateTypes: Set<PortfolioUpdateType> = PortfolioUpdateType.entries.toSet(),
        autoReconnect: Boolean = wsConfig.autoReconnectEnabled
    ) {
        if (hasCredentialsError()) {
            throw IllegalStateException("Access token not set")
        }
        this.updateTypes = updateTypes
        initiateConnection(autoReconnect)
    }

    override fun getWebSocketUrl(): String {
        return executeAuthorizePortfolioStream(apiClient, updateTypes).authorizedRedirectUri
            ?: throw IllegalStateException("Portfolio stream authorization failed: no WebSocket URL received")
    }

    override fun onWebSocketMessage(text: String) {
        parseAndDispatch(text)
    }

    override fun onWebSocketBinaryMessage(bytes: ByteString) {
        // Portfolio stream uses text messages only
    }

    /**
     * Notifies all listeners with exception guarding.
     * If a listener throws, other listeners still receive the notification.
     */
    private inline fun notifyListeners(action: (PortfolioStreamListener) -> Unit) {
        listeners.forEach { listener ->
            try {
                action(listener)
            } catch (e: Exception) {
                try {
                    listener.onError(e)
                } catch (_: Exception) {
                    // Ignore errors from error handler
                }
            }
        }
    }

    override fun onConnectionEstablished(isReconnect: Boolean) {
        // Validate credentials before proceeding
        if (hasCredentialsError()) {
            notifyListeners { it.onError(IllegalStateException("Access token not set")) }
            return
        }

        // Notify listeners based on connection type
        if (isReconnect) {
            notifyListeners { it.onReconnected() }
        } else {
            notifyListeners { it.onConnected() }
        }
    }

    override fun onWebSocketDisconnected(code: Int, reason: String) {
        notifyListeners { it.onDisconnected(code, reason) }
    }

    override fun onWebSocketReconnecting(attempt: Int, delayMs: Long) {
        notifyListeners { it.onReconnecting(attempt, delayMs) }
    }

    override fun onWebSocketError(error: Throwable) {
        notifyListeners { it.onError(error) }
    }

    /**
     * Parses WebSocket message and dispatches to appropriate listener.
     *
     * The Portfolio Stream sends flat JSON messages with "update_type" at root level.
     * We first detect the type, then deserialize the full message to the specific class.
     * This matches the official Upstox SDK's parsing pattern.
     */
    private fun parseAndDispatch(json: String) {
        try {
            // Step 1: Detect the update type from the flat JSON message
            val typeDetector = HttpClient.objectMapper.readValue(json, PortfolioStreamTypeDetector::class.java)

            // Step 2: Deserialize entire JSON to specific class based on type
            when (typeDetector.updateType) {
                "order" -> {
                    val order = HttpClient.objectMapper.readValue(json, OrderUpdate::class.java)
                    notifyListeners { it.onOrderUpdate(order) }
                }
                "position" -> {
                    val position = HttpClient.objectMapper.readValue(json, PositionUpdate::class.java)
                    notifyListeners { it.onPositionUpdate(position) }
                }
                "holding" -> {
                    val holding = HttpClient.objectMapper.readValue(json, HoldingUpdate::class.java)
                    notifyListeners { it.onHoldingUpdate(holding) }
                }
                "gtt_order" -> {
                    val gttOrder = HttpClient.objectMapper.readValue(json, GttOrderUpdate::class.java)
                    notifyListeners { it.onGttOrderUpdate(gttOrder) }
                }
                // Unknown types silently ignored (matching official SDK behavior)
            }
        } catch (e: Exception) {
            notifyListeners { it.onError(e) }
        }
    }

    /**
     * Closes the client and releases resources.
     *
     * Closes the WebSocket and clears all listeners.
     */
    override fun close() {
        // Close the WebSocket
        super.close()

        // Clear listeners last
        listeners.clear()
    }
}
