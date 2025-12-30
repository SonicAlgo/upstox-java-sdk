package io.github.sonicalgo.upstox.websocket.marketData

/**
 * Feed modes for market data subscription.
 */
enum class FeedMode(val value: String) {
    /** Last traded price and close price only */
    LTPC("ltpc"),
    /** Full market data with 5 levels of depth */
    FULL("full"),
    /** Option Greeks with first-level depth */
    OPTION_GREEKS("option_greeks"),
    /** Full market data with 30 levels of depth (Upstox Plus only) */
    FULL_D30("full_d30")
}

/**
 * Listener interface for market data feed events.
 *
 * Core lifecycle callbacks ([onConnected], [onDisconnected], [onError]) are required.
 * All other callbacks have empty default implementations, allowing you to override only what you need.
 *
 * ## Kotlin Example
 * ```kotlin
 * client.addListener(object : MarketDataListener {
 *     override fun onConnected() {
 *         client.subscribe(listOf("NSE_EQ|INE002A01018"))
 *     }
 *     override fun onDisconnected(code: Int, reason: String) { }
 *     override fun onError(error: Throwable) { error.printStackTrace() }
 *
 *     // Optional: handle reconnection success
 *     override fun onReconnected() {
 *         println("Reconnected successfully!")
 *     }
 *
 *     // Only override data callbacks you need
 *     override fun onLtpcUpdate(key: String, tick: LtpcTick) {
 *         println("$key: ${tick.ltp}")
 *     }
 * })
 * client.connect()
 * ```
 *
 * ## Java Example
 * ```java
 * client.addListener(new MarketDataListener() {
 *     @Override public void onConnected() { client.subscribe(List.of("NSE_EQ|INE002A01018")); }
 *     @Override public void onDisconnected(int code, String reason) { }
 *     @Override public void onError(Throwable error) { error.printStackTrace(); }
 *
 *     @Override
 *     public void onLtpcUpdate(String key, LtpcTick tick) {
 *         System.out.println(key + ": " + tick.getLtp());
 *     }
 * });
 * client.connect();
 * ```
 */
interface MarketDataListener {
    // ==================== Connection Lifecycle ====================

    /**
     * Called when WebSocket connection is established for the first time.
     *
     * Use this to perform initial subscriptions. For reconnection events, see [onReconnected].
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
     *
     * Note: Subscriptions are automatically restored if auto-resubscribe is enabled.
     */
    fun onReconnected() {}

    // ==================== Market Data Callbacks (All Optional) ====================

    /**
     * Called when market status information is received (on initial connection).
     *
     * Contains the trading status of all market segments (e.g., NSE_EQ: NORMAL_OPEN).
     *
     * @param status Market status event with segment statuses
     */
    fun onMarketStatus(status: MarketStatusEvent) {}

    /**
     * Called when LTPC (Last Traded Price & Close) data is received.
     *
     * Triggered for subscriptions with [FeedMode.LTPC].
     *
     * @param instrumentKey Instrument identifier (e.g., "NSE_EQ|INE002A01018")
     * @param tick LTPC tick data containing ltp, close price, etc.
     */
    fun onLtpcUpdate(instrumentKey: String, tick: LtpcTick) {}

    /**
     * Called when full market feed data is received for stocks/F&O instruments.
     *
     * Triggered for subscriptions with [FeedMode.FULL] or [FeedMode.FULL_D30].
     * Contains market depth (5 or 30 levels), OHLC, option greeks, and more.
     *
     * @param instrumentKey Instrument identifier (e.g., "NSE_EQ|INE002A01018")
     * @param feed Full feed tick with market depth and extended data
     */
    fun onFullFeedUpdate(instrumentKey: String, feed: FullFeedTick) {}

    /**
     * Called when full market feed data is received for index instruments.
     *
     * Triggered for index subscriptions with [FeedMode.FULL].
     * Contains LTPC and OHLC data (no market depth for indices).
     *
     * @param instrumentKey Instrument identifier (e.g., "NSE_INDEX|Nifty 50")
     * @param feed Index feed tick with LTPC and OHLC data
     */
    fun onIndexFeedUpdate(instrumentKey: String, feed: IndexFeedTick) {}

    /**
     * Called when option greeks data is received.
     *
     * Triggered for subscriptions with [FeedMode.OPTION_GREEKS].
     * Contains option greeks (delta, theta, gamma, vega, rho) with first-level depth.
     *
     * @param instrumentKey Instrument identifier (e.g., "NSE_FO|45450")
     * @param feed Option greeks tick with greeks and first-level depth
     */
    fun onOptionGreeksUpdate(instrumentKey: String, feed: OptionGreeksTick) {}
}
