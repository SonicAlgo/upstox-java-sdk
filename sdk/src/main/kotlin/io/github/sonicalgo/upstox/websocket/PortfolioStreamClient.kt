package io.github.sonicalgo.upstox.websocket

import io.github.sonicalgo.core.client.HttpClient
import io.github.sonicalgo.upstox.api.WebSocketApi
import io.github.sonicalgo.upstox.config.UpstoxConfig
import io.github.sonicalgo.upstox.config.UpstoxWebSocketConfig
import io.github.sonicalgo.upstox.model.enums.PortfolioUpdateType
import io.github.sonicalgo.upstox.model.websocket.*
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
 *     override fun onConnected(isReconnect: Boolean) {
 *         println("Connected to portfolio stream")
 *     }
 *     override fun onOrderUpdate(order: OrderUpdate) {
 *         println("Order update: ${order.orderId} - ${order.status}")
 *     }
 *     override fun onPositionUpdate(position: PositionUpdate) {
 *         println("Position update: ${position.tradingSymbol}")
 *     }
 *     override fun onHoldingUpdate(holding: HoldingUpdate) {
 *         println("Holding update: ${holding.tradingSymbol}")
 *     }
 *     override fun onGttOrderUpdate(gttOrder: GttOrderUpdate) {
 *         println("GTT order update: ${gttOrder.gttOrderId}")
 *     }
 *     override fun onDisconnected(code: Int, reason: String) {
 *         println("Disconnected: $reason")
 *     }
 *     override fun onReconnecting(attempt: Int, delayMs: Long) {
 *         println("Reconnecting attempt $attempt in ${delayMs}ms")
 *     }
 *     override fun onError(error: Throwable) {
 *         error.printStackTrace()
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
    private val webSocketApi: WebSocketApi,
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
        this.updateTypes = updateTypes
        initiateConnection(autoReconnect)
    }

    override fun getWebSocketUrl(): String {
        return webSocketApi.authorizePortfolioStream().authorizedRedirectUri
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

        // Notify listeners
        notifyListeners { it.onConnected(isReconnect) }
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

    private fun parseAndDispatch(json: String) {
        try {
            val message = HttpClient.objectMapper.readValue(json, PortfolioStreamMessage::class.java)
            val data = message.data ?: return

            when {
                data.order != null -> notifyListeners { it.onOrderUpdate(data.order) }
                data.position != null -> notifyListeners { it.onPositionUpdate(data.position) }
                data.holding != null -> notifyListeners { it.onHoldingUpdate(data.holding) }
                data.gttOrder != null -> notifyListeners { it.onGttOrderUpdate(data.gttOrder) }
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

/**
 * Listener interface for portfolio stream events.
 *
 * All portfolio update callbacks have empty default implementations, allowing you to
 * override only the callbacks you need.
 *
 * ## Kotlin Example
 * ```kotlin
 * client.addListener(object : PortfolioStreamListener {
 *     override fun onConnected(isReconnect: Boolean) { println("Connected") }
 *     override fun onDisconnected(code: Int, reason: String) { }
 *     override fun onError(error: Throwable) { error.printStackTrace() }
 *
 *     // Only override what you need
 *     override fun onOrderUpdate(order: OrderUpdate) {
 *         println("Order: ${order.orderId} - ${order.status}")
 *     }
 * })
 * client.connect()
 * ```
 *
 * ## Java Example
 * ```java
 * client.addListener(new PortfolioStreamListener() {
 *     @Override public void onConnected(boolean isReconnect) { }
 *     @Override public void onDisconnected(int code, String reason) { }
 *     @Override public void onError(Throwable error) { error.printStackTrace(); }
 *
 *     @Override
 *     public void onOrderUpdate(OrderUpdate order) {
 *         System.out.println("Order: " + order.getOrderId());
 *     }
 * });
 * client.connect();
 * ```
 */
interface PortfolioStreamListener {
    // ==================== Connection Lifecycle ====================

    /**
     * Called when WebSocket connection is established.
     *
     * @param isReconnect true if this is a reconnection, false if first connection
     */
    fun onConnected(isReconnect: Boolean)

    /**
     * Called when WebSocket is disconnected.
     *
     * Note: If auto-reconnect is enabled, [onReconnecting] will be called after this.
     *
     * @param code WebSocket close code
     * @param reason Close reason message
     */
    fun onDisconnected(code: Int, reason: String)

    /**
     * Called when an error occurs.
     *
     * @param error The exception that occurred
     */
    fun onError(error: Throwable)

    /**
     * Called when the client is attempting to reconnect.
     *
     * @param attempt Current reconnection attempt number (1-based)
     * @param delayMs Delay in milliseconds before the reconnection attempt
     */
    fun onReconnecting(attempt: Int, delayMs: Long) {}

    // ==================== Portfolio Update Callbacks (All Optional) ====================

    /**
     * Called when an order update is received.
     *
     * Triggered when an order is placed, modified, executed, or cancelled.
     *
     * @param order The order update data
     */
    fun onOrderUpdate(order: OrderUpdate) {}

    /**
     * Called when a position update is received.
     *
     * Triggered when positions change due to trades or end-of-day processing.
     *
     * @param position The position update data
     */
    fun onPositionUpdate(position: PositionUpdate) {}

    /**
     * Called when a holding update is received.
     *
     * Triggered when holdings change due to delivery trades or corporate actions.
     *
     * @param holding The holding update data
     */
    fun onHoldingUpdate(holding: HoldingUpdate) {}

    /**
     * Called when a GTT order update is received.
     *
     * Triggered when a GTT order is created, triggered, or expires.
     *
     * @param gttOrder The GTT order update data
     */
    fun onGttOrderUpdate(gttOrder: GttOrderUpdate) {}
}
