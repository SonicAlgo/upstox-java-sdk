package io.github.sonicalgo.upstox

import io.github.sonicalgo.upstox.api.*
import io.github.sonicalgo.upstox.config.OkHttpClientFactory
import io.github.sonicalgo.upstox.config.UpstoxConfig
import io.github.sonicalgo.upstox.websocket.MarketDataFeedClient
import io.github.sonicalgo.upstox.websocket.PortfolioStreamClient
import java.io.Closeable

/**
 * Main entry point for interacting with the Upstox trading platform.
 *
 * Provides access to all Upstox APIs including authentication, trading,
 * market data, and portfolio management.
 *
 * ## Quick Start
 *
 * ### Authentication Flow
 * ```kotlin
 * val upstox = Upstox.getInstance()
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
 * ### Using the SDK
 * ```kotlin
 * val upstox = Upstox.getInstance()
 * upstox.setAccessToken("your-access-token")
 *
 * // Get user profile
 * val profile = upstox.getUserApi().getProfile()
 * println("Welcome, ${profile.userName}!")
 *
 * // Get positions
 * val positions = upstox.getPortfolioApi().getPositions()
 *
 * // Place an order
 * val orderResponse = upstox.getOrdersApi().placeOrder(PlaceOrderParams(
 *     instrumentToken = "NSE_EQ|INE669E01016",
 *     quantity = 1,
 *     product = Product.D,
 *     validity = Validity.DAY,
 *     price = 0.0,
 *     orderType = OrderType.MARKET,
 *     transactionType = TransactionType.BUY,
 *     disclosedQuantity = 0,
 *     triggerPrice = 0.0,
 *     isAmo = false
 * ))
 *
 * // Get market quotes
 * val quotes = upstox.getMarketQuoteApi().getFullQuote(listOf("NSE_EQ|INE669E01016"))
 * ```
 *
 * ## Sandbox Mode
 *
 * For testing order APIs in sandbox environment:
 * ```kotlin
 * val upstox = Upstox.getInstance()
 * upstox.setAccessToken("production-token")
 * upstox.setSandboxMode(true, "sandbox-token")
 *
 * // Order APIs now use sandbox token
 * upstox.getOrdersApi().placeOrder(params)
 *
 * // Disable sandbox mode
 * upstox.setSandboxMode(false, null)
 * ```
 *
 * ## WebSocket Streaming
 *
 * ### Market Data Feed
 * ```kotlin
 * val upstox = Upstox.getInstance()
 * val feedClient = upstox.createMarketDataFeedClient()
 *
 * feedClient.connect(object : MarketDataListener {
 *     override fun onConnected() {
 *         feedClient.subscribe(listOf("NSE_EQ|INE669E01016"), FeedMode.FULL)
 *     }
 *     override fun onFullFeedUpdate(instrumentKey: String, feed: FullFeedTick) {
 *         println("$instrumentKey: LTP=${feed.ltp}, Volume=${feed.volume}")
 *     }
 *     override fun onDisconnected(code: Int, reason: String) {}
 *     override fun onError(error: Throwable) { error.printStackTrace() }
 * })
 * ```
 *
 * ## API Modules
 *
 * | Module | Description |
 * |--------|-------------|
 * | [getLoginApi] | OAuth authentication (authorize, token, logout) |
 * | [getUserApi] | User profile and funds/margin |
 * | [getOrdersApi] | Order placement, modification, cancellation, and retrieval |
 * | [getGttOrdersApi] | GTT (Good Till Triggered) orders |
 * | [getPortfolioApi] | Positions and holdings |
 * | [getChargesApi] | Brokerage calculations |
 * | [getMarginsApi] | Margin requirements calculations |
 * | [getTradePnlApi] | Profit & Loss reports |
 * | [getHistoricalDataApi] | Historical OHLC candle data |
 * | [getMarketQuoteApi] | Real-time market quotes (LTP, OHLC, full quote, Greeks) |
 * | [getMarketInfoApi] | Market holidays, timings, and status |
 * | [getOptionChainApi] | Option contracts and chains |
 * | [getExpiredInstrumentsApi] | Expired derivatives data |
 * | [getInstrumentsApi] | Instrument master data (no auth required) |
 *
 * @see <a href="https://upstox.com/developer/api-documentation/">Upstox API Documentation</a>
 */
class Upstox private constructor() : Closeable {

    // ==================== API Modules ====================

    /**
     * Gets the API module for authentication operations.
     *
     * Provides OAuth authorization flow, token management, and logout.
     *
     * @see <a href="https://upstox.com/developer/api-documentation/authorize">Auth API</a>
     */
    fun getLoginApi() = LoginApi.instance

    /**
     * Gets the API module for user operations.
     *
     * Provides user profile and funds/margin information.
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-profile">User API</a>
     */
    fun getUserApi() = UserApi.instance

    /**
     * Gets the API module for order operations.
     *
     * Provides order placement, modification, cancellation,
     * order book, and trade history.
     *
     * Note: When sandbox mode is enabled, order operations use sandbox token.
     *
     * @see <a href="https://upstox.com/developer/api-documentation/v3/place-order">Orders API</a>
     */
    fun getOrdersApi() = OrdersApi.instance

    /**
     * Gets the API module for GTT (Good Till Triggered) orders.
     *
     * Provides GTT order placement, modification, cancellation,
     * and retrieval.
     *
     * @see <a href="https://upstox.com/developer/api-documentation/place-gtt-order">GTT API</a>
     */
    fun getGttOrdersApi() = GttOrdersApi.instance

    /**
     * Gets the API module for portfolio operations.
     *
     * Provides positions, MTF positions, holdings, and position conversion.
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-positions">Portfolio API</a>
     */
    fun getPortfolioApi() = PortfolioApi.instance

    /**
     * Gets the API module for brokerage calculations.
     *
     * Provides brokerage calculation for potential trades.
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-brokerage">Charges API</a>
     */
    fun getChargesApi() = ChargesApi.instance

    /**
     * Gets the API module for margin calculations.
     *
     * Provides margin requirements calculation for orders.
     *
     * @see <a href="https://upstox.com/developer/api-documentation/margin">Margin API</a>
     */
    fun getMarginsApi() = MarginsApi.instance

    /**
     * Gets the API module for trade profit and loss reports.
     *
     * Provides P&L reports, metadata, and charges breakdown.
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-profit-and-loss-report">Trade P&L API</a>
     */
    fun getTradePnlApi() = TradePnlApi.instance

    /**
     * Gets the API module for historical candle data.
     *
     * Provides OHLC data for historical and intraday periods.
     *
     * @see <a href="https://upstox.com/developer/api-documentation/v3/get-historical-candle-data">Historical Data API</a>
     */
    fun getHistoricalDataApi() = HistoricalDataApi.instance

    /**
     * Gets the API module for market quotes.
     *
     * Provides full quotes, OHLC, LTP, and option Greeks.
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-full-market-quote">Market Quote API</a>
     */
    fun getMarketQuoteApi() = MarketQuoteApi.instance

    /**
     * Gets the API module for market information.
     *
     * Provides market holidays, timings, and status.
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-market-holidays">Market Info API</a>
     */
    fun getMarketInfoApi() = MarketInfoApi.instance

    /**
     * Gets the API module for option chain data.
     *
     * Provides option contracts and put/call option chains.
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-option-contracts">Option Chain API</a>
     */
    fun getOptionChainApi() = OptionChainApi.instance

    /**
     * Gets the API module for expired instruments.
     *
     * Provides expired derivatives data and historical candles.
     * Requires Upstox Plus subscription.
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-expiries">Expired Instruments API</a>
     */
    fun getExpiredInstrumentsApi() = ExpiredInstrumentsApi.instance

    /**
     * Gets the API module for instrument master data.
     *
     * Provides access to the instruments master which contains details
     * of all tradeable instruments across NSE, BSE, and MCX exchanges.
     *
     * Note: This API does not require authentication.
     *
     * @see <a href="https://upstox.com/developer/api-documentation/instruments">Instruments API</a>
     */
    fun getInstrumentsApi() = InstrumentsApi.instance

    /**
     * Gets the API module for WebSocket authorization.
     *
     * Provides methods to get authorized WebSocket URLs.
     */
    fun getWebSocketApi() = WebSocketApi.instance

    // ==================== Configuration ====================

    /**
     * Sets the OAuth access token for authenticating API requests.
     *
     * The token is obtained through the OAuth flow:
     * 1. Get authorization URL using getLoginApi().getAuthorizationUrl()
     * 2. Redirect user to authorize
     * 3. Exchange authorization code for token using getLoginApi().getToken()
     *
     * Note: Access tokens expire at 3:30 AM IST daily. The SDK does not
     * automatically refresh tokens - handle 401 errors by obtaining a new token.
     *
     * @param token The access token
     */
    fun setAccessToken(token: String) {
        UpstoxConfig.accessToken = token
    }

    /**
     * Gets the current access token.
     *
     * @return The current access token
     */
    fun getAccessToken(): String = UpstoxConfig.accessToken

    /**
     * Enables or disables sandbox mode for order APIs.
     *
     * When enabled, order-related API calls (place, modify, cancel) will use
     * the sandbox token instead of the access token. Other APIs always use access token.
     *
     * @param enabled Whether to enable sandbox mode
     * @param token The sandbox token (required when enabling, can be null when disabling)
     */
    fun setSandboxMode(enabled: Boolean, token: String?) {
        UpstoxConfig.sandboxEnabled = enabled
        if (token != null) {
            UpstoxConfig.sandboxToken = token
        }
    }

    /**
     * Checks if sandbox mode is enabled.
     *
     * @return true if sandbox mode is enabled
     */
    fun isSandboxEnabled(): Boolean = UpstoxConfig.sandboxEnabled

    /**
     * Enables or disables HTTP request/response logging.
     *
     * When enabled, logs HTTP requests and responses for debugging.
     * Should only be enabled during development.
     *
     * Default: false (disabled)
     *
     * @param enabled Whether to enable logging
     */
    fun setLoggingEnabled(enabled: Boolean) {
        UpstoxConfig.loggingEnabled = enabled
    }

    /**
     * Checks if HTTP logging is enabled.
     *
     * @return true if logging is enabled
     */
    fun isLoggingEnabled(): Boolean = UpstoxConfig.loggingEnabled

    /**
     * Sets the number of retry attempts for rate-limited requests (HTTP 429).
     *
     * When set to 0 (default), rate limit errors are thrown immediately.
     * When set to 1-5, the SDK will retry with exponential backoff (1s, 2s, 4s, ...).
     *
     * Default: 0 (disabled)
     *
     * @param retries Number of retry attempts (0-5)
     */
    fun setRateLimitRetries(retries: Int) {
        UpstoxConfig.rateLimitRetries = retries
    }

    /**
     * Gets the current rate limit retry count.
     *
     * @return Number of retry attempts configured
     */
    fun getRateLimitRetries(): Int = UpstoxConfig.rateLimitRetries

    /**
     * Sets the maximum number of WebSocket reconnection attempts.
     *
     * Uses exponential backoff between attempts.
     *
     * Default: 5 (approximately 31 seconds total)
     *
     * @param attempts Number of reconnection attempts (1-20)
     */
    fun setMaxWebSocketReconnectAttempts(attempts: Int) {
        UpstoxConfig.maxWebSocketReconnectAttempts = attempts
    }

    /**
     * Gets the maximum WebSocket reconnection attempts.
     *
     * @return Maximum reconnection attempts configured
     */
    fun getMaxWebSocketReconnectAttempts(): Int = UpstoxConfig.maxWebSocketReconnectAttempts

    /**
     * Enables or disables automatic WebSocket reconnection globally.
     *
     * When enabled (default), WebSocket clients will automatically attempt
     * to reconnect on disconnection with exponential backoff.
     * This can be overridden per-connection in the connect() method.
     *
     * Default: true (enabled)
     *
     * @param enabled Whether to enable auto-reconnection
     */
    fun setWebSocketAutoReconnectEnabled(enabled: Boolean) {
        UpstoxConfig.webSocketAutoReconnectEnabled = enabled
    }

    /**
     * Checks if WebSocket auto-reconnection is enabled.
     *
     * @return true if auto-reconnection is enabled
     */
    fun isWebSocketAutoReconnectEnabled(): Boolean = UpstoxConfig.webSocketAutoReconnectEnabled

    /**
     * Resets all configuration to default values.
     *
     * This clears:
     * - Access token and sandbox token
     * - Sandbox mode (disabled)
     * - Rate limit retries (0)
     * - HTTP logging (disabled)
     * - WebSocket reconnect attempts (default)
     * - WebSocket auto-reconnect (enabled)
     *
     * Note: You will need to call [setAccessToken] again after this.
     */
    fun resetConfiguration() {
        UpstoxConfig.resetToDefaults()
    }

    // ==================== WebSocket Factory Methods ====================

    /**
     * Creates a new Market Data Feed client for real-time tick data.
     *
     * Each call creates a new client instance. Remember to close it when done.
     *
     * @return A new MarketDataFeedClient instance
     */
    fun createMarketDataFeedClient(): MarketDataFeedClient {
        return MarketDataFeedClient()
    }

    /**
     * Creates a new Portfolio Stream client for real-time portfolio updates.
     *
     * Each call creates a new client instance. Remember to close it when done.
     *
     * @return A new PortfolioStreamClient instance
     */
    fun createPortfolioStreamClient(): PortfolioStreamClient {
        return PortfolioStreamClient()
    }

    // ==================== Lifecycle ====================

    /**
     * Closes the SDK and releases all resources.
     *
     * Should be called when the SDK is no longer needed to properly
     * clean up thread pools and connections.
     */
    override fun close() {
        OkHttpClientFactory.shutdown()
    }

    companion object {
        @Volatile
        private var instance: Upstox? = null

        /**
         * Gets the singleton Upstox instance.
         *
         * Thread-safe double-checked locking singleton pattern.
         *
         * @return The Upstox singleton instance
         */
        @JvmStatic
        fun getInstance(): Upstox {
            return instance ?: synchronized(this) {
                instance ?: Upstox().also { instance = it }
            }
        }
    }
}
