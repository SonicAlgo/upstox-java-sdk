package io.github.sonicalgo.upstox.websocket

import io.github.sonicalgo.upstox.api.WebSocketApi
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.OkHttpClientFactory
import io.github.sonicalgo.upstox.config.UpstoxConstants
import io.github.sonicalgo.upstox.model.marketdata.*
import io.github.sonicalgo.upstox.websocket.proto.MarketDataFeedProto.*
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Client for real-time market data feed via WebSocket.
 *
 * Provides tick-by-tick market data using protobuf binary format with automatic
 * reconnection support and subscription state management.
 *
 * ## Features
 * - Automatic reconnection with exponential backoff on disconnection
 * - Subscription state tracking - subscriptions are automatically restored after reconnect
 * - Ping/pong keepalive (configured at OkHttpClient level)
 * - Thread-safe subscription management
 * - Type-safe callbacks for different feed modes
 *
 * ## Example usage
 * ```kotlin
 * val client = Upstox.createMarketDataFeedClient()
 * client.connect(object : MarketDataListener {
 *     override fun onConnected() {
 *         // Subscribe with default LTPC mode (minimal bandwidth)
 *         client.subscribe(listOf("NSE_EQ|INE669E01016"))
 *     }
 *     override fun onDisconnected(code: Int, reason: String) {
 *         println("Disconnected: $reason")
 *     }
 *     override fun onError(error: Throwable) {
 *         error.printStackTrace()
 *     }
 *
 *     // Only override the callbacks you need!
 *     override fun onLtpcUpdate(instrumentKey: String, tick: LtpcTick) {
 *         println("$instrumentKey: LTP=${tick.ltp}")
 *     }
 * })
 *
 * // Later, to stop reconnection attempts and disconnect:
 * client.close()
 * ```
 */
class MarketDataFeedClient internal constructor() : BaseWebSocketClient(
    OkHttpClientFactory.wsHttpClient,
    "MarketDataFeed"
) {

    @Volatile
    private var listener: MarketDataListener? = null

    // Subscription state tracking for automatic restoration after reconnect
    private val subscriptions = ConcurrentHashMap<String, FeedMode>()

    /**
     * Connect to the market data feed WebSocket.
     *
     * Automatically obtains the authorized WebSocket URL before connecting.
     * If auto-reconnect is enabled (default), the client will automatically
     * attempt to reconnect on disconnection with exponential backoff.
     *
     * @param listener Listener for feed events
     * @param autoReconnect Whether to automatically reconnect on disconnection (default: true)
     */
    @JvmOverloads
    fun connect(listener: MarketDataListener, autoReconnect: Boolean = true) {
        this.listener = listener
        initiateConnection(autoReconnect)
    }

    override fun getWebSocketUrl(): String {
        val authResponse = WebSocketApi.instance.authorizeMarketDataFeed()
        return authResponse.authorizedRedirectUri
    }

    override fun onWebSocketBinaryMessage(bytes: ByteString) {
        val response = FeedResponse.parseFrom(bytes.toByteArray())

        when (response.type) {
            Type.market_info -> {
                val status = MarketStatusEvent(
                    segmentStatus = response.marketInfo.toSegmentStatusMap(),
                    timestamp = response.currentTs
                )
                listener?.onMarketStatus(status)
            }
            Type.live_feed, Type.initial_feed -> {
                response.feedsMap.forEach { (instrumentKey, feed) ->
                    dispatchFeed(instrumentKey, feed)
                }
            }
            else -> {}
        }
    }

    private fun dispatchFeed(instrumentKey: String, feed: Feed) {
        when (feed.feedUnionCase) {
            Feed.FeedUnionCase.LTPC -> {
                val tick = feed.ltpc.toLtpcTick()
                listener?.onLtpcUpdate(instrumentKey, tick)
            }
            Feed.FeedUnionCase.FULLFEED -> {
                val fullFeed = feed.fullFeed
                when (fullFeed.fullFeedUnionCase) {
                    FullFeed.FullFeedUnionCase.MARKETFF -> {
                        val tick = fullFeed.marketFF.toFullFeedTick(feed.requestMode)
                        listener?.onFullFeedUpdate(instrumentKey, tick)
                    }
                    FullFeed.FullFeedUnionCase.INDEXFF -> {
                        val tick = fullFeed.indexFF.toIndexFeedTick()
                        listener?.onIndexFeedUpdate(instrumentKey, tick)
                    }
                    else -> {}
                }
            }
            Feed.FeedUnionCase.FIRSTLEVELWITHGREEKS -> {
                val tick = feed.firstLevelWithGreeks.toOptionGreeksTick()
                listener?.onOptionGreeksUpdate(instrumentKey, tick)
            }
            else -> {}
        }
    }

    override fun onConnectionEstablished(isReconnect: Boolean) {
        if (isReconnect) {
            listener?.onReconnected()
            restoreSubscriptions()
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

    private fun restoreSubscriptions() {
        if (subscriptions.isEmpty()) return

        // Group subscriptions by mode for efficient restoration
        val subscriptionsByMode = subscriptions.entries.groupBy(
            keySelector = { it.value },
            valueTransform = { it.key }
        )

        subscriptionsByMode.forEach { (mode, instrumentKeys) ->
            sendSubscriptionMessage("sub", instrumentKeys, mode)
        }
    }

    /**
     * Subscribe to market data for the given instruments.
     *
     * Subscriptions are tracked internally and will be automatically restored
     * after a reconnection.
     *
     * @param instrumentKeys List of instrument keys (e.g., "NSE_EQ|INE669E01016")
     * @param mode Feed mode determining the level of data (default: LTPC for minimal bandwidth)
     */
    fun subscribe(instrumentKeys: List<String>, mode: FeedMode = FeedMode.LTPC) {
        // Track subscriptions for reconnection
        instrumentKeys.forEach { key -> subscriptions[key] = mode }

        if (isConnected) {
            sendSubscriptionMessage("sub", instrumentKeys, mode)
        }
    }

    /**
     * Unsubscribe from market data for the given instruments.
     *
     * @param instrumentKeys List of instrument keys to unsubscribe from
     */
    fun unsubscribe(instrumentKeys: List<String>) {
        // Remove from tracked subscriptions
        instrumentKeys.forEach { key -> subscriptions.remove(key) }

        if (isConnected) {
            sendSubscriptionMessage("unsub", instrumentKeys, null)
        }
    }

    /**
     * Change the feed mode for subscribed instruments.
     *
     * @param instrumentKeys List of instrument keys
     * @param mode New feed mode
     */
    fun changeMode(instrumentKeys: List<String>, mode: FeedMode) {
        // Update tracked subscriptions
        instrumentKeys.forEach { key ->
            if (subscriptions.containsKey(key)) {
                subscriptions[key] = mode
            }
        }

        if (isConnected) {
            sendSubscriptionMessage("change_mode", instrumentKeys, mode)
        }
    }

    /**
     * Get all currently tracked subscriptions.
     *
     * @return Map of instrument keys to their feed modes
     */
    fun getSubscriptions(): Map<String, FeedMode> = subscriptions.toMap()

    /**
     * Clear all tracked subscriptions.
     *
     * Note: This does not send unsubscribe messages to the server.
     * Use [unsubscribe] to properly unsubscribe from instruments.
     */
    fun clearSubscriptions() {
        subscriptions.clear()
    }

    private fun sendSubscriptionMessage(method: String, instrumentKeys: List<String>, mode: FeedMode?) {
        val message = SubscriptionMessage(
            guid = UUID.randomUUID().toString().replace("-", "").take(UpstoxConstants.SUBSCRIPTION_GUID_LENGTH),
            method = method,
            data = SubscriptionData(
                mode = mode?.value,
                instrumentKeys = instrumentKeys
            )
        )
        val json = ApiClient.gson.toJson(message)
        sendBinaryMessage(json.encodeToByteArray().toByteString())
    }
}

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
 * All market data callbacks have empty default implementations, allowing you to
 * override only the callbacks you need.
 *
 * ## Kotlin Example
 * ```kotlin
 * client.connect(object : MarketDataListener {
 *     override fun onConnected() { client.subscribe(listOf("NSE_EQ|INE002A01018")) }
 *     override fun onDisconnected(code: Int, reason: String) { }
 *     override fun onError(error: Throwable) { error.printStackTrace() }
 *
 *     // Only override what you need
 *     override fun onLtpcUpdate(key: String, tick: LtpcTick) {
 *         println("$key: ${tick.ltp}")
 *     }
 * })
 * ```
 *
 * ## Java Example
 * ```java
 * client.connect(new MarketDataListener() {
 *     @Override public void onConnected() { client.subscribe(List.of("NSE_EQ|INE002A01018")); }
 *     @Override public void onDisconnected(int code, String reason) { }
 *     @Override public void onError(Throwable error) { error.printStackTrace(); }
 *
 *     @Override
 *     public void onLtpcUpdate(String key, LtpcTick tick) {
 *         System.out.println(key + ": " + tick.getLtp());
 *     }
 * });
 * ```
 */
interface MarketDataListener {
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
     *
     * Subscriptions are automatically restored before this callback is invoked.
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

// Internal message classes for subscription
private data class SubscriptionMessage(
    val guid: String,
    val method: String,
    val data: SubscriptionData
)

private data class SubscriptionData(
    val mode: String?,
    val instrumentKeys: List<String>
)
