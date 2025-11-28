package io.github.sonicalgo.upstox.websocket

import io.github.sonicalgo.upstox.api.PortfolioUpdateType
import io.github.sonicalgo.upstox.api.WebSocketApi
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.OkHttpClientFactory
import io.github.sonicalgo.upstox.config.UpstoxConfig
import io.github.sonicalgo.upstox.model.websocket.*

/**
 * Client for real-time portfolio updates via WebSocket.
 *
 * Provides real-time updates for orders, positions, holdings, and GTT orders
 * with automatic reconnection support.
 *
 * ## Features
 * - Automatic reconnection with exponential backoff on disconnection
 * - Update type tracking - update types are automatically restored after reconnect
 * - Ping/pong keepalive (configured at OkHttpClient level)
 * - Thread-safe state management
 *
 * ## Example usage
 * ```kotlin
 * val client = Upstox.createPortfolioStreamClient()
 * client.connect(object : PortfolioStreamListener {
 *     override fun onConnected() {
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
 *     override fun onReconnected() {
 *         println("Reconnected successfully!")
 *     }
 *     override fun onError(error: Throwable) {
 *         error.printStackTrace()
 *     }
 * })
 *
 * // Later, to stop reconnection attempts and disconnect:
 * client.close()
 * ```
 */
class PortfolioStreamClient internal constructor() : BaseWebSocketClient(
    OkHttpClientFactory.wsHttpClient,
    "PortfolioStream"
) {

    @Volatile
    private var listener: PortfolioStreamListener? = null

    // Update types to restore after reconnection
    @Volatile
    private var updateTypes: Set<PortfolioUpdateType> = PortfolioUpdateType.entries.toSet()

    /**
     * Connect to the portfolio stream WebSocket.
     *
     * Automatically obtains the authorized WebSocket URL before connecting.
     * If auto-reconnect is enabled (default from global config), the client will automatically
     * attempt to reconnect on disconnection with exponential backoff.
     *
     * @param listener Listener for portfolio events
     * @param updateTypes Types of updates to subscribe to. Defaults to all types.
     * @param autoReconnect Whether to automatically reconnect on disconnection (default: uses global config)
     */
    @JvmOverloads
    fun connect(
        listener: PortfolioStreamListener,
        updateTypes: Set<PortfolioUpdateType> = PortfolioUpdateType.entries.toSet(),
        autoReconnect: Boolean = UpstoxConfig.webSocketAutoReconnectEnabled
    ) {
        this.listener = listener
        this.updateTypes = updateTypes
        initiateConnection(autoReconnect)
    }

    override fun getWebSocketUrl(): String {
        val authResponse = WebSocketApi.instance.authorizePortfolioStream(updateTypes)
        return authResponse.authorizedRedirectUri
    }

    override fun onWebSocketMessage(text: String) {
        parseAndDispatch(text)
    }

    override fun onConnectionEstablished(isReconnect: Boolean) {
        if (isReconnect) {
            listener?.onReconnected()
        } else {
            listener?.onConnected()
        }
    }

    override fun onWebSocketDisconnected(code: Int, reason: String) {
        listener?.onDisconnected(code, reason)
    }

    override fun onWebSocketReconnecting(attempt: Int, delayMs: Long) {
        listener?.onReconnecting(attempt, delayMs)
    }

    override fun onWebSocketError(error: Throwable) {
        listener?.onError(error)
    }

    private fun parseAndDispatch(json: String) {
        val message = ApiClient.gson.fromJson(json, PortfolioStreamMessage::class.java)
        val data = message.data ?: return

        when {
            data.order != null -> listener?.onOrderUpdate(data.order)
            data.position != null -> listener?.onPositionUpdate(data.position)
            data.holding != null -> listener?.onHoldingUpdate(data.holding)
            data.gttOrder != null -> listener?.onGttOrderUpdate(data.gttOrder)
        }
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
 * client.connect(object : PortfolioStreamListener {
 *     override fun onConnected() { println("Connected") }
 *     override fun onDisconnected(code: Int, reason: String) { }
 *     override fun onError(error: Throwable) { error.printStackTrace() }
 *
 *     // Only override what you need
 *     override fun onOrderUpdate(order: OrderUpdate) {
 *         println("Order: ${order.orderId} - ${order.status}")
 *     }
 * })
 * ```
 *
 * ## Java Example
 * ```java
 * client.connect(new PortfolioStreamListener() {
 *     @Override public void onConnected() { }
 *     @Override public void onDisconnected(int code, String reason) { }
 *     @Override public void onError(Throwable error) { error.printStackTrace(); }
 *
 *     @Override
 *     public void onOrderUpdate(OrderUpdate order) {
 *         System.out.println("Order: " + order.getOrderId());
 *     }
 * });
 * ```
 */
interface PortfolioStreamListener {
    // ==================== Connection Lifecycle (Required) ====================

    /**
     * Called when WebSocket connection is established for the first time.
     */
    fun onConnected()

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

    // ==================== Connection Lifecycle (Optional) ====================

    /**
     * Called when the client is attempting to reconnect.
     *
     * @param attempt Current reconnection attempt number (1-based)
     * @param delayMs Delay in milliseconds before the reconnection attempt
     */
    fun onReconnecting(attempt: Int, delayMs: Long) {}

    /**
     * Called when successfully reconnected after a disconnection.
     */
    fun onReconnected() {}

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
