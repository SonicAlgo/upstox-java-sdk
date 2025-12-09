package io.github.sonicalgo.upstox

import io.github.sonicalgo.upstox.Upstox.Companion.builder
import io.github.sonicalgo.upstox.api.*
import io.github.sonicalgo.upstox.config.*
import io.github.sonicalgo.upstox.websocket.MarketDataFeedClient
import io.github.sonicalgo.upstox.websocket.PortfolioStreamClient
import java.io.Closeable
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Main entry point for the Upstox SDK.
 *
 * Provides access to all Upstox trading APIs including orders, portfolio,
 * market data, and WebSocket clients for real-time updates.
 *
 * ## Getting Started
 *
 * Create an instance using the builder:
 * ```kotlin
 * val upstox = Upstox.builder()
 *     .accessToken("your-access-token")
 *     .loggingEnabled(true)
 *     .build()
 *
 * // Get user profile
 * val profile = upstox.getUserApi().getProfile()
 * println("Welcome, ${profile.userName}!")
 * ```
 *
 * ## Authentication Flow
 *
 * For initial authentication, create an instance without access token, then use LoginApi:
 * ```kotlin
 * val upstox = Upstox.builder().build()
 *
 * // Step 1: Get authorization URL
 * val authUrl = upstox.getLoginApi().getAuthorizationUrl(AuthorizeParams(
 *     clientId = "your-api-key",
 *     redirectUri = "https://yourapp.com/callback"
 * ))
 * // Redirect user to authUrl
 *
 * // Step 2: Exchange code for token (in callback handler)
 * val tokenResponse = upstox.getLoginApi().getToken(GetTokenParams(
 *     code = "authorization-code",
 *     clientId = "your-api-key",
 *     clientSecret = "your-api-secret",
 *     redirectUri = "https://yourapp.com/callback"
 * ))
 *
 * // Step 3: Set the access token
 * upstox.setAccessToken(tokenResponse.accessToken)
 * ```
 *
 * ## Placing Orders
 *
 * ```kotlin
 * val ordersApi = upstox.getOrdersApi()
 *
 * val response = ordersApi.placeOrder(PlaceOrderParams(
 *     instrumentToken = "NSE_EQ|INE669E01016",
 *     quantity = 1,
 *     product = Product.DELIVERY,
 *     validity = Validity.DAY,
 *     price = 0.0,
 *     orderType = OrderType.MARKET,
 *     transactionType = TransactionType.BUY,
 *     disclosedQuantity = 0,
 *     triggerPrice = 0.0,
 *     isAmo = false
 * ))
 * println("Order ID: ${response.orderId}")
 * ```
 *
 * ## Real-Time Market Data
 *
 * ```kotlin
 * val feedClient = upstox.createMarketDataFeedClient()
 * feedClient.addListener(object : MarketDataListener {
 *     override fun onConnected() {
 *         feedClient.subscribe(listOf("NSE_EQ|INE669E01016"), FeedMode.FULL)
 *     }
 *     override fun onFullFeedUpdate(instrumentKey: String, feed: FullFeedTick) {
 *         println("$instrumentKey: LTP=${feed.ltp}, Volume=${feed.volume}")
 *     }
 *     override fun onDisconnected(code: Int, reason: String) {}
 *     override fun onError(error: Throwable) { error.printStackTrace() }
 * })
 *
 * feedClient.connect()
 * ```
 *
 * ## Real-Time Portfolio Updates
 *
 * ```kotlin
 * val portfolioClient = upstox.createPortfolioStreamClient()
 * portfolioClient.addListener(object : PortfolioStreamListener {
 *     override fun onOrderUpdate(orderUpdate: OrderUpdate) {
 *         println("Order ${orderUpdate.orderId}: ${orderUpdate.status}")
 *     }
 *     override fun onPositionUpdate(position: PositionUpdate) {
 *         println("Position: ${position.instrumentToken}")
 *     }
 *     override fun onConnected() {}
 *     override fun onDisconnected(code: Int, reason: String) {}
 *     override fun onError(error: Throwable) {}
 * })
 *
 * portfolioClient.connect()
 * ```
 *
 * ## Configuration via Builder
 *
 * ```kotlin
 * val upstox = Upstox.builder()
 *     .accessToken("your-access-token")    // Optional at build, can set later
 *     .loggingEnabled(true)                // Enable HTTP logging
 *     .rateLimitRetries(3)                 // Auto-retry on rate limit
 *     .build()
 *
 * // Access token can be updated later (e.g., for token refresh)
 * upstox.setAccessToken("new-token")
 * ```
 *
 * ## Sandbox Mode
 *
 * For testing order APIs in sandbox environment:
 * ```kotlin
 * val upstox = Upstox.builder()
 *     .accessToken("production-token")
 *     .sandboxMode(true, "sandbox-token")
 *     .build()
 *
 * // Order APIs now use sandbox token
 * upstox.getOrdersApi().placeOrder(params)
 * ```
 *
 * ## Multiple Instances
 *
 * Each `build()` call creates an independent instance with its own
 * HTTP client and configuration:
 *
 * ```kotlin
 * val client1 = Upstox.builder().accessToken("token1").build()
 * val client2 = Upstox.builder().accessToken("token2").build()
 * ```
 *
 * ## Thread Safety
 *
 * All API modules are thread-safe and can be called from any thread.
 * WebSocket callbacks are invoked on background threads.
 *
 * ## Resource Management
 *
 * The SDK implements [Closeable] for easy resource cleanup. Call [close]
 * to release all resources including HTTP clients and WebSocket connections:
 *
 * ```kotlin
 * // Manual close
 * val upstox = Upstox.builder().accessToken("token").build()
 * val feed = upstox.createMarketDataFeedClient()
 * // ... use SDK ...
 * upstox.close() // Closes all WebSocket clients and HTTP resources
 *
 * // Or use try-with-resources (Kotlin)
 * Upstox.builder().accessToken("token").build().use { upstox ->
 *     val feed = upstox.createMarketDataFeedClient()
 *     // ... use SDK ...
 * } // Auto-closes when block exits
 * ```
 *
 * @see <a href="https://upstox.com/developer/api-documentation/">Upstox API Documentation</a>
 */
class Upstox private constructor(internal val config: UpstoxConfig) : Closeable {

    // Per-instance infrastructure
    private val headerProvider = UpstoxHeaderProvider(config)
    private val clientProvider = UpstoxHttpClientProvider(config, headerProvider, UpstoxConstants.SHUTDOWN_TIMEOUT_SECONDS)
    internal val apiClient = ApiClient(config, clientProvider)

    // Track WebSocket clients for unified lifecycle management
    private val webSocketClients = CopyOnWriteArrayList<Closeable>()

    // Lazy per-instance API modules
    private val _loginApi by lazy { LoginApi(apiClient, config) }
    private val _userApi by lazy { UserApi(apiClient) }
    private val _ordersApi by lazy { OrdersApi(apiClient, config) }
    private val _gttOrdersApi by lazy { GttOrdersApi(apiClient) }
    private val _portfolioApi by lazy { PortfolioApi(apiClient) }
    private val _chargesApi by lazy { ChargesApi(apiClient) }
    private val _marginsApi by lazy { MarginsApi(apiClient) }
    private val _tradePnlApi by lazy { TradePnlApi(apiClient) }
    private val _historicalDataApi by lazy { HistoricalDataApi(apiClient) }
    private val _marketQuoteApi by lazy { MarketQuoteApi(apiClient) }
    private val _marketInfoApi by lazy { MarketInfoApi(apiClient) }
    private val _optionChainApi by lazy { OptionChainApi(apiClient) }
    private val _expiredInstrumentsApi by lazy { ExpiredInstrumentsApi(apiClient) }
    private val _instrumentsApi by lazy { InstrumentsApi(apiClient) }
    private val _webSocketApi by lazy { WebSocketApi(apiClient) }

    // ==================== Mutable State ====================

    /**
     * Sets the OAuth access token for API authentication.
     *
     * Can be called after initialization to update the token (e.g., for token refresh).
     *
     * Note: Access tokens expire at 3:30 AM IST daily. The SDK does not
     * automatically refresh tokens - handle 401 errors by obtaining a new token.
     *
     * @param token Access token from Upstox
     * @return This instance for chaining
     * @throws IllegalArgumentException if token is blank
     */
    fun setAccessToken(token: String): Upstox {
        require(token.isNotBlank()) { "Access token cannot be blank" }
        config.accessToken = token
        return this
    }

    /**
     * Gets the current access token.
     *
     * @return Current access token or empty string if not set
     */
    fun getAccessToken(): String = config.accessToken

    /**
     * Checks if sandbox mode is enabled.
     *
     * @return true if sandbox mode is enabled
     */
    fun isSandboxEnabled(): Boolean = config.sandboxEnabled

    /**
     * Checks if HTTP logging is enabled.
     *
     * @return true if logging is enabled
     */
    fun isLoggingEnabled(): Boolean = config.loggingEnabled

    /**
     * Gets the rate limit retry count.
     *
     * @return Number of retries configured
     */
    fun getRateLimitRetries(): Int = config.rateLimitRetries

    // ==================== REST API Modules ====================

    /**
     * Gets the Login API module.
     *
     * Provides OAuth authorization flow, token management, and logout.
     *
     * @return [LoginApi] instance
     * @see <a href="https://upstox.com/developer/api-documentation/authorize">Auth API</a>
     */
    fun getLoginApi(): LoginApi = _loginApi

    /**
     * Gets the User API module.
     *
     * Provides user profile and funds/margin information.
     *
     * @return [UserApi] instance
     * @see <a href="https://upstox.com/developer/api-documentation/get-profile">User API</a>
     */
    fun getUserApi(): UserApi = _userApi

    /**
     * Gets the Orders API module.
     *
     * Provides order placement, modification, cancellation,
     * order book, and trade history.
     *
     * Note: When sandbox mode is enabled, order operations use sandbox token.
     *
     * @return [OrdersApi] instance
     * @see <a href="https://upstox.com/developer/api-documentation/v3/place-order">Orders API</a>
     */
    fun getOrdersApi(): OrdersApi = _ordersApi

    /**
     * Gets the GTT Orders API module.
     *
     * Provides GTT (Good Till Triggered) order placement, modification,
     * cancellation, and retrieval.
     *
     * @return [GttOrdersApi] instance
     * @see <a href="https://upstox.com/developer/api-documentation/place-gtt-order">GTT API</a>
     */
    fun getGttOrdersApi(): GttOrdersApi = _gttOrdersApi

    /**
     * Gets the Portfolio API module.
     *
     * Provides positions, MTF positions, holdings, and position conversion.
     *
     * @return [PortfolioApi] instance
     * @see <a href="https://upstox.com/developer/api-documentation/get-positions">Portfolio API</a>
     */
    fun getPortfolioApi(): PortfolioApi = _portfolioApi

    /**
     * Gets the Charges API module.
     *
     * Provides brokerage calculation for potential trades.
     *
     * @return [ChargesApi] instance
     * @see <a href="https://upstox.com/developer/api-documentation/get-brokerage">Charges API</a>
     */
    fun getChargesApi(): ChargesApi = _chargesApi

    /**
     * Gets the Margins API module.
     *
     * Provides margin requirements calculation for orders.
     *
     * @return [MarginsApi] instance
     * @see <a href="https://upstox.com/developer/api-documentation/margin">Margin API</a>
     */
    fun getMarginsApi(): MarginsApi = _marginsApi

    /**
     * Gets the Trade P&L API module.
     *
     * Provides P&L reports, metadata, and charges breakdown.
     *
     * @return [TradePnlApi] instance
     * @see <a href="https://upstox.com/developer/api-documentation/get-profit-and-loss-report">Trade P&L API</a>
     */
    fun getTradePnlApi(): TradePnlApi = _tradePnlApi

    /**
     * Gets the Historical Data API module.
     *
     * Provides OHLC data for historical and intraday periods.
     *
     * @return [HistoricalDataApi] instance
     * @see <a href="https://upstox.com/developer/api-documentation/v3/get-historical-candle-data">Historical Data API</a>
     */
    fun getHistoricalDataApi(): HistoricalDataApi = _historicalDataApi

    /**
     * Gets the Market Quote API module.
     *
     * Provides full quotes, OHLC, LTP, and option Greeks.
     *
     * @return [MarketQuoteApi] instance
     * @see <a href="https://upstox.com/developer/api-documentation/get-full-market-quote">Market Quote API</a>
     */
    fun getMarketQuoteApi(): MarketQuoteApi = _marketQuoteApi

    /**
     * Gets the Market Info API module.
     *
     * Provides market holidays, timings, and status.
     *
     * @return [MarketInfoApi] instance
     * @see <a href="https://upstox.com/developer/api-documentation/get-market-holidays">Market Info API</a>
     */
    fun getMarketInfoApi(): MarketInfoApi = _marketInfoApi

    /**
     * Gets the Option Chain API module.
     *
     * Provides option contracts and put/call option chains.
     *
     * @return [OptionChainApi] instance
     * @see <a href="https://upstox.com/developer/api-documentation/get-option-contracts">Option Chain API</a>
     */
    fun getOptionChainApi(): OptionChainApi = _optionChainApi

    /**
     * Gets the Expired Instruments API module.
     *
     * Provides expired derivatives data and historical candles.
     * Requires Upstox Plus subscription.
     *
     * @return [ExpiredInstrumentsApi] instance
     * @see <a href="https://upstox.com/developer/api-documentation/get-expiries">Expired Instruments API</a>
     */
    fun getExpiredInstrumentsApi(): ExpiredInstrumentsApi = _expiredInstrumentsApi

    /**
     * Gets the Instruments API module.
     *
     * Provides access to the instruments master which contains details
     * of all tradeable instruments across NSE, BSE, and MCX exchanges.
     *
     * Note: This API does not require authentication.
     *
     * @return [InstrumentsApi] instance
     * @see <a href="https://upstox.com/developer/api-documentation/instruments">Instruments API</a>
     */
    fun getInstrumentsApi(): InstrumentsApi = _instrumentsApi

    /**
     * Gets the WebSocket API module.
     *
     * Provides methods to get authorized WebSocket URLs.
     * Internal use only - WebSocket clients handle authorization automatically.
     *
     * @return [WebSocketApi] instance
     */
    internal fun getWebSocketApi(): WebSocketApi = _webSocketApi

    // ==================== WebSocket Clients ====================

    /**
     * Creates a new Market Data Feed WebSocket client.
     *
     * The client is automatically tracked and will be closed when [close] is called.
     *
     * Example:
     * ```kotlin
     * val client = upstox.createMarketDataFeedClient()
     * client.addListener(myListener)
     * client.connect()
     * client.subscribe(listOf("NSE_EQ|INE669E01016"), FeedMode.FULL)
     *
     * // All clients are closed automatically when upstox.close() is called
     * ```
     *
     * @param maxReconnectAttempts Maximum reconnection attempts, 1-20 (default: 5)
     * @param autoReconnectEnabled Enable automatic reconnection (default: true)
     * @param autoResubscribeEnabled Enable auto-resubscription after reconnect (default: true)
     * @return New [MarketDataFeedClient] instance
     * @throws IllegalArgumentException if maxReconnectAttempts is not between 1 and 20
     */
    fun createMarketDataFeedClient(
        maxReconnectAttempts: Int = UpstoxConstants.WEBSOCKET_DEFAULT_MAX_RECONNECT_ATTEMPTS,
        autoReconnectEnabled: Boolean = true,
        autoResubscribeEnabled: Boolean = true
    ): MarketDataFeedClient {
        require(maxReconnectAttempts in 1..20) { "maxReconnectAttempts must be between 1 and 20" }
        val wsConfig = UpstoxWebSocketConfig(
            maxReconnectAttempts = maxReconnectAttempts,
            autoReconnectEnabled = autoReconnectEnabled,
            autoResubscribeEnabled = autoResubscribeEnabled
        )
        val client = MarketDataFeedClient(config, wsConfig, _webSocketApi, clientProvider.getWsHttpClient(UpstoxConstants.WEBSOCKET_PING_INTERVAL_MS))
        webSocketClients.add(client)
        return client
    }

    /**
     * Creates a new Portfolio Stream WebSocket client.
     *
     * The client is automatically tracked and will be closed when [close] is called.
     *
     * Example:
     * ```kotlin
     * val client = upstox.createPortfolioStreamClient()
     * client.addListener(myListener)
     * client.connect()
     *
     * // All clients are closed automatically when upstox.close() is called
     * ```
     *
     * @param maxReconnectAttempts Maximum reconnection attempts, 1-20 (default: 5)
     * @param autoReconnectEnabled Enable automatic reconnection (default: true)
     * @return New [PortfolioStreamClient] instance
     * @throws IllegalArgumentException if maxReconnectAttempts is not between 1 and 20
     */
    fun createPortfolioStreamClient(
        maxReconnectAttempts: Int = UpstoxConstants.WEBSOCKET_DEFAULT_MAX_RECONNECT_ATTEMPTS,
        autoReconnectEnabled: Boolean = true
    ): PortfolioStreamClient {
        require(maxReconnectAttempts in 1..20) { "maxReconnectAttempts must be between 1 and 20" }
        val wsConfig = UpstoxWebSocketConfig(
            maxReconnectAttempts = maxReconnectAttempts,
            autoReconnectEnabled = autoReconnectEnabled,
            autoResubscribeEnabled = false // Not applicable for portfolio stream
        )
        val client = PortfolioStreamClient(config, wsConfig, _webSocketApi, clientProvider.getWsHttpClient(UpstoxConstants.WEBSOCKET_PING_INTERVAL_MS))
        webSocketClients.add(client)
        return client
    }

    // ==================== Lifecycle ====================

    /**
     * Closes this SDK instance and releases all resources.
     *
     * This method:
     * 1. Closes all WebSocket clients created by this instance
     * 2. Shuts down the HTTP client and connection pool
     *
     * After calling this method, the SDK instance should not be used.
     *
     * Supports try-with-resources:
     * ```kotlin
     * Upstox.builder().accessToken("token").build().use { upstox ->
     *     // SDK auto-closes when block exits
     * }
     * ```
     */
    override fun close() {
        // Close all tracked WebSocket clients
        webSocketClients.forEach { it.close() }
        webSocketClients.clear()
        // Release HTTP resources
        clientProvider.shutdown()
    }

    // ==================== Builder ====================

    /**
     * Builder for creating [Upstox] instances.
     *
     * Example:
     * ```kotlin
     * val upstox = Upstox.builder()
     *     .accessToken("your-token")         // Optional at build
     *     .loggingEnabled(true)              // Optional
     *     .rateLimitRetries(3)               // Optional
     *     .sandboxMode(true, "sandbox-token") // Optional
     *     .build()
     * ```
     */
    class Builder {
        private var accessToken: String = ""
        private var sandboxEnabled: Boolean = false
        private var sandboxToken: String = ""
        private var loggingEnabled: Boolean = false
        private var rateLimitRetries: Int = 0

        /**
         * Sets the OAuth access token (optional at build time).
         *
         * Can be set later using [Upstox.setAccessToken].
         *
         * @param token Access token from Upstox
         * @return This builder for chaining
         */
        fun accessToken(token: String): Builder = apply {
            this.accessToken = token
        }

        /**
         * Enables or disables sandbox mode for order APIs (default: false).
         *
         * When enabled, order-related API calls (place, modify, cancel) will use
         * the sandbox token instead of the access token.
         *
         * @param enabled true to enable sandbox mode
         * @param token The sandbox token (required when enabling)
         * @return This builder for chaining
         * @throws IllegalArgumentException if enabled is true and token is blank
         */
        fun sandboxMode(enabled: Boolean, token: String? = null): Builder = apply {
            if (enabled) {
                require(!token.isNullOrBlank()) { "Sandbox token is required when enabling sandbox mode" }
                this.sandboxToken = token
            }
            this.sandboxEnabled = enabled
        }

        /**
         * Enables or disables HTTP request/response logging (default: false).
         *
         * Useful for debugging API calls. Logs at BODY level.
         *
         * @param enabled true to enable logging
         * @return This builder for chaining
         */
        fun loggingEnabled(enabled: Boolean): Builder = apply {
            this.loggingEnabled = enabled
        }

        /**
         * Sets the number of automatic retries for rate-limited requests (default: 0).
         *
         * When a rate limit (HTTP 429) is encountered, the SDK will wait
         * and retry up to this many times with exponential backoff.
         *
         * @param retries Number of retries (0-5)
         * @return This builder for chaining
         * @throws IllegalArgumentException if retries is not between 0 and 5
         */
        fun rateLimitRetries(retries: Int): Builder = apply {
            require(retries in 0..5) { "rateLimitRetries must be between 0 and 5" }
            this.rateLimitRetries = retries
        }

        /**
         * Builds and returns a new [Upstox] instance.
         *
         * @return New [Upstox] instance
         */
        fun build(): Upstox {
            val config = UpstoxConfig(
                accessToken = accessToken,
                sandboxToken = sandboxToken,
                sandboxEnabled = sandboxEnabled,
                loggingEnabled = loggingEnabled,
                rateLimitRetries = rateLimitRetries
            )

            return Upstox(config)
        }
    }

    /**
     * Factory methods for creating [Upstox] SDK instances.
     *
     * Use [builder] to configure and create a new SDK instance:
     *
     * ```kotlin
     * val upstox = Upstox.builder()
     *     .accessToken("your-access-token")
     *     .loggingEnabled(true)
     *     .build()
     * ```
     *
     * @see Upstox
     * @see Builder
     */
    companion object {
        /**
         * Creates a new builder for configuring an [Upstox] instance.
         *
         * Example:
         * ```kotlin
         * val upstox = Upstox.builder()
         *     .accessToken("your-token")       // Optional at build time
         *     .loggingEnabled(true)            // Enable HTTP logging
         *     .rateLimitRetries(3)             // Auto-retry on rate limit
         *     .sandboxMode(true, "sandbox")    // Enable sandbox mode
         *     .build()
         *
         * // Access APIs
         * val profile = upstox.getUserApi().getProfile()
         * val orders = upstox.getOrdersApi().getOrderBook()
         *
         * // Create WebSocket clients (config passed at creation time)
         * val marketFeed = upstox.createMarketDataFeedClient(maxReconnectAttempts = 10)
         * val portfolio = upstox.createPortfolioStreamClient()
         * ```
         *
         * @return New [Builder] instance
         * @see Builder
         */
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
