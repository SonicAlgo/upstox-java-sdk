package io.github.sonicalgo.upstox.websocket.marketData

import io.github.sonicalgo.core.client.HttpClient
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConfig
import io.github.sonicalgo.upstox.usecase.executeAuthorizeMarketDataFeed
import io.github.sonicalgo.upstox.config.UpstoxConstants
import io.github.sonicalgo.upstox.config.UpstoxWebSocketConfig
import io.github.sonicalgo.upstox.websocket.BaseWebSocketClient
import io.github.sonicalgo.upstox.websocket.proto.MarketDataFeedProto.*
import okhttp3.OkHttpClient
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Client for real-time market data feed via WebSocket.
 *
 * Provides tick-by-tick market data using protobuf binary format with automatic
 * reconnection support and subscription state management.
 *
 * ## Features
 * - Automatic reconnection with exponential backoff on disconnection
 * - Subscription state tracking - subscriptions are automatically restored after reconnect
 * - Multiple listener support - add/remove listeners at any time
 * - Ping/pong keepalive (configured at OkHttpClient level)
 * - Thread-safe subscription management
 * - Type-safe callbacks for different feed modes
 *
 * ## Example usage
 * ```kotlin
 * val upstox = Upstox.builder()
 *     .accessToken("your-token")
 *     .build()
 *
 * val client = upstox.createMarketDataFeedClient()
 *
 * // Add listener
 * client.addListener(object : MarketDataListener {
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
 *     // Optional: handle reconnection
 *     override fun onReconnected() {
 *         println("Reconnected successfully!")
 *     }
 *
 *     // Only override the data callbacks you need!
 *     override fun onLtpcUpdate(instrumentKey: String, tick: LtpcTick) {
 *         println("$instrumentKey: LTP=${tick.ltp}")
 *     }
 * })
 *
 * // Connect (listeners notified automatically)
 * client.connect()
 *
 * // Later, to stop reconnection attempts and disconnect:
 * client.close()
 * ```
 *
 * @see MarketDataListener
 * @see <a href="https://upstox.com/developer/api-documentation/v3/get-market-data-feed">Market Data Feed API</a>
 */
class MarketDataFeedClient internal constructor(
    upstoxConfig: UpstoxConfig,
    wsConfig: UpstoxWebSocketConfig,
    private val apiClient: ApiClient,
    wsHttpClient: OkHttpClient
) : BaseWebSocketClient(wsHttpClient, upstoxConfig, wsConfig, "MarketDataFeed") {

    private val listeners = CopyOnWriteArrayList<MarketDataListener>()

    // Lock for coordinating subscription state with connection state
    // Ensures atomic check-then-act operations in subscribe/unsubscribe
    private val subscriptionLock = ReentrantLock()

    // Subscription state tracking for automatic restoration after reconnect
    private val subscriptions = ConcurrentHashMap<String, FeedMode>()

    // Pending subscriptions buffer - stores subscriptions made before connection is ready
    private val pendingSubscriptions = ConcurrentHashMap<String, FeedMode>()

    /**
     * Adds a listener to receive market data feed events.
     *
     * @param listener Listener to add
     */
    fun addListener(listener: MarketDataListener) {
        listeners.add(listener)
    }

    /**
     * Removes a listener.
     *
     * @param listener Listener to remove
     */
    fun removeListener(listener: MarketDataListener) {
        listeners.remove(listener)
    }

    /**
     * Connect to the market data feed WebSocket.
     *
     * Automatically obtains the authorized WebSocket URL before connecting.
     * If auto-reconnect is enabled, the client will automatically
     * attempt to reconnect on disconnection with exponential backoff.
     *
     * @param autoReconnect Whether to automatically reconnect on disconnection (default: from config)
     */
    fun connect(autoReconnect: Boolean = wsConfig.autoReconnectEnabled) {
        if (hasCredentialsError()) {
            throw IllegalStateException("Access token not set")
        }
        initiateConnection(autoReconnect)
    }

    override fun getWebSocketUrl(): String {
        return executeAuthorizeMarketDataFeed(apiClient).authorizedRedirectUri
            ?: throw IllegalStateException("Market data feed authorization failed: no WebSocket URL received")
    }

    override fun onWebSocketMessage(text: String) {
        // Market data uses binary messages only
    }

    override fun onWebSocketBinaryMessage(bytes: ByteString) {
        val response = FeedResponse.parseFrom(bytes.toByteArray())

        when (response.type) {
            Type.market_info -> {
                val status = MarketStatusEvent(
                    segmentStatus = response.marketInfo.toSegmentStatusMap(),
                    timestamp = response.currentTs
                )
                notifyListeners { it.onMarketStatus(status) }
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
                notifyListeners { it.onLtpcUpdate(instrumentKey, tick) }
            }
            Feed.FeedUnionCase.FULLFEED -> {
                val fullFeed = feed.fullFeed
                when (fullFeed.fullFeedUnionCase) {
                    FullFeed.FullFeedUnionCase.MARKETFF -> {
                        val tick = fullFeed.marketFF.toFullFeedTick(feed.requestMode)
                        notifyListeners { it.onFullFeedUpdate(instrumentKey, tick) }
                    }
                    FullFeed.FullFeedUnionCase.INDEXFF -> {
                        val tick = fullFeed.indexFF.toIndexFeedTick()
                        notifyListeners { it.onIndexFeedUpdate(instrumentKey, tick) }
                    }
                    else -> {}
                }
            }
            Feed.FeedUnionCase.FIRSTLEVELWITHGREEKS -> {
                val tick = feed.firstLevelWithGreeks.toOptionGreeksTick()
                notifyListeners { it.onOptionGreeksUpdate(instrumentKey, tick) }
            }
            else -> {}
        }
    }

    /**
     * Notifies all listeners with exception guarding.
     * If a listener throws, other listeners still receive the notification.
     */
    private inline fun notifyListeners(action: (MarketDataListener) -> Unit) {
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

        // Restore subscriptions if this is a reconnection and auto-resubscribe is enabled
        if (isReconnect && wsConfig.autoResubscribeEnabled && subscriptions.isNotEmpty()) {
            restoreSubscriptions()
        }

        // Process any pending subscriptions that were buffered before connection
        processPendingSubscriptions()
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
     * Restores all tracked subscriptions after reconnection.
     * Uses lock to prevent race conditions with concurrent subscribe/unsubscribe calls.
     */
    private fun restoreSubscriptions() = subscriptionLock.withLock {
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
     * Processes pending subscriptions that were buffered before connection was established.
     * Uses lock to prevent race conditions with concurrent subscribe calls.
     */
    private fun processPendingSubscriptions() = subscriptionLock.withLock {
        if (pendingSubscriptions.isEmpty()) return

        // Move pending to active subscriptions
        pendingSubscriptions.forEach { (key, mode) -> subscriptions[key] = mode }

        // Group by mode and send subscription messages
        val subscriptionsByMode = pendingSubscriptions.entries.groupBy(
            keySelector = { it.value },
            valueTransform = { it.key }
        )

        subscriptionsByMode.forEach { (mode, instrumentKeys) ->
            sendSubscriptionMessage("sub", instrumentKeys, mode)
        }

        // Clear pending subscriptions
        pendingSubscriptions.clear()
    }

    /**
     * Subscribe to market data for the given instruments.
     *
     * If the connection is established, subscriptions are sent immediately.
     * If not connected, subscriptions are buffered as pending and will be
     * processed automatically when the connection is established.
     *
     * Subscriptions are tracked internally and will be automatically restored
     * after a reconnection (if autoResubscribeEnabled is true).
     *
     * Thread-safe: uses lock to coordinate with connection state changes.
     *
     * @param instrumentKeys List of instrument keys (e.g., "NSE_EQ|INE669E01016")
     * @param mode Feed mode determining the level of data (default: LTPC for minimal bandwidth)
     * @return true if subscription request was sent immediately, false if buffered as pending
     */
    fun subscribe(instrumentKeys: List<String>, mode: FeedMode = FeedMode.LTPC): Boolean = subscriptionLock.withLock {
        if (isConnected) {
            // Track subscriptions and send immediately
            instrumentKeys.forEach { key -> subscriptions[key] = mode }
            sendSubscriptionMessage("sub", instrumentKeys, mode)
            true
        } else {
            // Buffer as pending - will be processed when connection is established
            instrumentKeys.forEach { key -> pendingSubscriptions[key] = mode }
            false
        }
    }

    /**
     * Unsubscribe from market data for the given instruments.
     *
     * Thread-safe: uses lock to coordinate with connection state changes.
     *
     * @param instrumentKeys List of instrument keys to unsubscribe from
     * @return true if unsubscription request was sent, false if not connected
     */
    fun unsubscribe(instrumentKeys: List<String>): Boolean = subscriptionLock.withLock {
        // Remove from tracked subscriptions and pending subscriptions
        instrumentKeys.forEach { key ->
            subscriptions.remove(key)
            pendingSubscriptions.remove(key)
        }

        if (isConnected) {
            sendSubscriptionMessage("unsub", instrumentKeys, null)
            true
        } else {
            false
        }
    }

    /**
     * Unsubscribe from all currently subscribed instruments.
     *
     * Thread-safe: ReentrantLock allows nested calls from same thread.
     *
     * @return true if unsubscription request was sent, false if not connected or no subscriptions
     */
    fun unsubscribeAll(): Boolean = subscriptionLock.withLock {
        // Get keys snapshot while holding lock, then delegate to unsubscribe
        // ReentrantLock allows the same thread to re-acquire the lock
        if (subscriptions.isNotEmpty()) {
            unsubscribe(subscriptions.keys.toList())
        } else {
            true // Nothing to unsubscribe
        }
    }

    /**
     * Change the feed mode for subscribed instruments.
     *
     * Thread-safe: uses lock to coordinate with connection state changes.
     *
     * @param instrumentKeys List of instrument keys
     * @param mode New feed mode
     * @return true if change mode request was sent, false if not connected
     */
    fun changeMode(instrumentKeys: List<String>, mode: FeedMode): Boolean = subscriptionLock.withLock {
        // Update tracked subscriptions
        instrumentKeys.forEach { key ->
            if (subscriptions.containsKey(key)) {
                subscriptions[key] = mode
            }
        }

        if (isConnected) {
            sendSubscriptionMessage("change_mode", instrumentKeys, mode)
            true
        } else {
            false
        }
    }

    /**
     * Get all currently tracked subscriptions.
     *
     * @return Map of instrument keys to their feed modes
     */
    fun getSubscriptions(): Map<String, FeedMode> = subscriptions.toMap()

    /**
     * Gets the count of currently subscribed instruments.
     */
    val subscriptionCount: Int
        get() = subscriptions.size

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
        val json = HttpClient.objectMapper.writeValueAsString(message)
        sendBinaryMessage(json.encodeToByteArray().toByteString())
    }

    /**
     * Closes the client and releases resources.
     *
     * Clears all subscriptions and listeners, then closes the WebSocket.
     * Note: No need to send unsub - server clears subscriptions when connection closes.
     */
    override fun close() {
        // Clear state
        subscriptions.clear()
        pendingSubscriptions.clear()

        // Close the WebSocket and scheduler
        super.close()

        // Clear listeners last
        listeners.clear()
    }
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
