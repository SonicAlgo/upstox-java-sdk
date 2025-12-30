package io.github.sonicalgo.upstox.websocket.portfolio

/**
 * Types of updates available in Portfolio Stream.
 */
enum class PortfolioUpdateType(val value: String) {
    ORDER("order"),
    GTT_ORDER("gtt_order"),
    POSITION("position"),
    HOLDING("holding")
}

/**
 * Listener interface for portfolio stream events.
 *
 * Core lifecycle callbacks ([onConnected], [onDisconnected], [onError]) are required.
 * All other callbacks have empty default implementations, allowing you to override only what you need.
 *
 * ## Kotlin Example
 * ```kotlin
 * client.addListener(object : PortfolioStreamListener {
 *     override fun onConnected() { println("Connected") }
 *     override fun onDisconnected(code: Int, reason: String) { }
 *     override fun onError(error: Throwable) { error.printStackTrace() }
 *
 *     // Optional: handle reconnection success
 *     override fun onReconnected() {
 *         println("Reconnected successfully!")
 *     }
 *
 *     // Only override data callbacks you need
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
 *     @Override public void onConnected() { }
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
     * Called when WebSocket connection is established for the first time.
     *
     * For reconnection events, see [onReconnected].
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

    /**
     * Called when the client is attempting to reconnect.
     *
     * @param attempt Current reconnection attempt number (1-based)
     * @param delayMs Delay in milliseconds before the reconnection attempt
     */
    fun onReconnecting(attempt: Int, delayMs: Long) {}

    /**
     * Called when WebSocket successfully reconnects after a disconnection.
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
