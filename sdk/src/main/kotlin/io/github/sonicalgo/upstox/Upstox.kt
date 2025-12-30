package io.github.sonicalgo.upstox

import io.github.sonicalgo.upstox.config.*
import io.github.sonicalgo.upstox.common.*
import io.github.sonicalgo.upstox.usecase.*
import io.github.sonicalgo.upstox.websocket.marketData.MarketDataFeedClient
import io.github.sonicalgo.upstox.websocket.portfolio.PortfolioStreamClient
import java.io.Closeable
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Main entry point for the Upstox SDK.
 *
 * Provides direct access to all Upstox trading operations including orders, portfolio,
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
 * val profile = upstox.getProfile()
 * println("Welcome, ${profile.userName}!")
 * ```
 *
 * ## Authentication Flow
 *
 * For initial authentication, create an instance without access token, then use login methods:
 * ```kotlin
 * val upstox = Upstox.builder().build()
 *
 * // Step 1: Get authorization URL
 * val authUrl = upstox.getAuthorizationUrl(
 *     clientId = "your-api-key",
 *     redirectUri = "https://yourapp.com/callback"
 * )
 * // Redirect user to authUrl
 *
 * // Step 2: Exchange code for token (in callback handler)
 * val tokenResponse = upstox.getToken(
 *     code = "authorization-code",
 *     clientId = "your-api-key",
 *     clientSecret = "your-api-secret",
 *     redirectUri = "https://yourapp.com/callback"
 * )
 *
 * // Step 3: Set the access token
 * upstox.setAccessToken(tokenResponse.accessToken)
 * ```
 *
 * ## Placing Orders
 *
 * ```kotlin
 * val response = upstox.placeOrder(PlaceOrderParams {
 *     instrumentToken = "NSE_EQ|INE669E01016"
 *     quantity = 1
 *     product = Product.DELIVERY
 *     validity = Validity.DAY
 *     price = 0.0
 *     orderType = OrderType.MARKET
 *     transactionType = TransactionType.BUY
 *     disclosedQuantity = 0
 *     triggerPrice = 0.0
 *     isAmo = false
 * })
 * println("Order ID: ${response.orderIds?.firstOrNull()}")
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
 * @see <a href="https://upstox.com/developer/api-documentation/">Upstox API Documentation</a>
 */
class Upstox private constructor(internal val config: UpstoxConfig) : Closeable {

    // Per-instance infrastructure
    private val headerProvider = UpstoxHeaderProvider()
    private val clientProvider = UpstoxHttpClientProvider(config, headerProvider, UpstoxConstants.SHUTDOWN_TIMEOUT_SECONDS)
    internal val apiClient = ApiClient(config, clientProvider)

    // Track WebSocket clients for unified lifecycle management
    private val webSocketClients = CopyOnWriteArrayList<Closeable>()

    // ==================== Mutable State ====================

    /**
     * Sets the OAuth access token for API authentication.
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
     */
    fun getAccessToken(): String = config.accessToken

    /**
     * Checks if sandbox mode is enabled.
     */
    fun isSandboxEnabled(): Boolean = config.sandboxEnabled

    /**
     * Checks if HTTP logging is enabled.
     */
    fun isLoggingEnabled(): Boolean = config.loggingEnabled

    /**
     * Gets the rate limit retry count.
     */
    fun getRateLimitRetries(): Int = config.rateLimitRetries

    // ==================== Authentication ====================

    /**
     * Constructs the authorization URL for initiating OAuth flow.
     *
     * **Kotlin:**
     * ```kotlin
     * val authUrl = upstox.getAuthorizationUrl(
     *     clientId = "your-api-key",
     *     redirectUri = "https://yourapp.com/callback",
     *     state = "optional-state"
     * )
     * // Redirect user to authUrl
     * ```
     *
     * **Java:**
     * ```java
     * String authUrl = upstox.getAuthorizationUrl(
     *     "your-api-key",
     *     "https://yourapp.com/callback",
     *     "optional-state"  // Can be null
     * );
     * ```
     *
     * @param clientId API key from app generation process
     * @param redirectUri URL to which the user will be redirected post authentication
     * @param state Optional parameter returned after authentication for state continuity
     * @see <a href="https://upstox.com/developer/api-documentation/authorize">Authorize API</a>
     */
    @JvmOverloads
    fun getAuthorizationUrl(
        clientId: String,
        redirectUri: String,
        state: String? = null
    ): String = executeGetAuthorizationUrl(clientId, redirectUri, state)

    /**
     * Exchanges an authorization code for access token.
     *
     * **Kotlin:**
     * ```kotlin
     * val tokenResponse = upstox.getToken(
     *     code = "authorization-code",
     *     clientId = "your-api-key",
     *     clientSecret = "your-api-secret",
     *     redirectUri = "https://yourapp.com/callback"
     * )
     * upstox.setAccessToken(tokenResponse.accessToken)
     * ```
     *
     * **Java:**
     * ```java
     * TokenResponse tokenResponse = upstox.getToken(
     *     "authorization-code",
     *     "your-api-key",
     *     "your-api-secret",
     *     "https://yourapp.com/callback"
     * );
     * upstox.setAccessToken(tokenResponse.getAccessToken());
     * ```
     *
     * @param code Authorization code from successful Authorize API authentication
     * @param clientId API key from app generation
     * @param clientSecret API secret from app generation
     * @param redirectUri URL provided during app generation
     * @see <a href="https://upstox.com/developer/api-documentation/get-token">Get Token API</a>
     */
    fun getToken(
        code: String,
        clientId: String,
        clientSecret: String,
        redirectUri: String
    ): TokenResponse = executeGetAccessToken(apiClient, code, clientId, clientSecret, redirectUri)

    /**
     * Requests access token generation via webhook flow.
     *
     * **Kotlin:**
     * ```kotlin
     * val response = upstox.requestAccessToken(
     *     clientId = "your-api-key",
     *     clientSecret = "your-api-secret"
     * )
     * println("Request ID: ${response.requestId}")
     * ```
     *
     * **Java:**
     * ```java
     * AccessTokenRequestResponse response = upstox.requestAccessToken(
     *     "your-api-key",
     *     "your-api-secret"
     * );
     * ```
     *
     * @param clientId API key obtained during app generation
     * @param clientSecret API secret obtained during app generation
     * @see <a href="https://upstox.com/developer/api-documentation/access-token-request">Access Token Request API</a>
     */
    fun requestAccessToken(clientId: String, clientSecret: String): AccessTokenRequestResponse =
        executeRequestAccessToken(apiClient, clientId, clientSecret)

    /**
     * Logs out the user and invalidates the current session.
     *
     * **Kotlin:**
     * ```kotlin
     * val success = upstox.logout()
     * if (success) println("Logged out successfully")
     * ```
     *
     * **Java:**
     * ```java
     * boolean success = upstox.logout();
     * ```
     *
     * @return true if logout was successful
     * @see <a href="https://upstox.com/developer/api-documentation/logout">Logout API</a>
     */
    fun logout(): Boolean = executeLogout(apiClient, config)

    // ==================== User ====================

    /**
     * Gets the user profile information.
     *
     * **Kotlin:**
     * ```kotlin
     * val profile = upstox.getProfile()
     * println("Name: ${profile.userName}")
     * println("Email: ${profile.email}")
     * ```
     *
     * **Java:**
     * ```java
     * UserProfile profile = upstox.getProfile();
     * System.out.println("Name: " + profile.getUserName());
     * ```
     *
     * @return [UserProfile] containing user information
     * @see <a href="https://upstox.com/developer/api-documentation/get-profile">Get Profile API</a>
     */
    fun getProfile(): UserProfile = executeGetProfile(apiClient)

    /**
     * Gets the user's fund and margin information.
     *
     * **Kotlin:**
     * ```kotlin
     * val funds = upstox.getFundsAndMargin()
     * println("Available: ${funds.equity?.availableMargin}")
     * println("Used: ${funds.equity?.usedMargin}")
     * ```
     *
     * **Java:**
     * ```java
     * FundsAndMargin funds = upstox.getFundsAndMargin();
     * // Or filter by segment
     * FundsAndMargin equityFunds = upstox.getFundsAndMargin(FundSegment.SECURITIES);
     * ```
     *
     * @param segment Optional segment filter: SECURITIES (equity) or COMMODITY. Omitting returns both.
     * @return [FundsAndMargin] containing fund details
     * @see <a href="https://upstox.com/developer/api-documentation/get-user-fund-margin">Get Funds API</a>
     */
    @JvmOverloads
    fun getFundsAndMargin(segment: FundSegment? = null): FundsAndMargin = executeGetFundsAndMargin(apiClient, segment)

    // ==================== Orders ====================

    /**
     * Places a new order.
     *
     * **Kotlin:**
     * ```kotlin
     * val response = upstox.placeOrder(PlaceOrderParams {
     *     instrumentToken = "NSE_EQ|INE669E01016"
     *     quantity = 1
     *     product = Product.DELIVERY
     *     validity = Validity.DAY
     *     price = 0.0
     *     orderType = OrderType.MARKET
     *     transactionType = TransactionType.BUY
     * })
     * println("Order IDs: ${response.orderIds}")
     * ```
     *
     * **Java:**
     * ```java
     * PlaceOrderParams params = new PlaceOrderParamsBuilder()
     *     .instrumentToken("NSE_EQ|INE669E01016")
     *     .quantity(1)
     *     .product(Product.DELIVERY)
     *     .validity(Validity.DAY)
     *     .price(0.0)
     *     .orderType(OrderType.MARKET)
     *     .transactionType(TransactionType.BUY)
     *     .build();
     *
     * PlaceOrderResponse response = upstox.placeOrder(params);
     * ```
     *
     * @param params Order parameters
     * @return [PlaceOrderResponse] containing order IDs and latency
     * @see <a href="https://upstox.com/developer/api-documentation/v3/place-order">Place Order API</a>
     */
    fun placeOrder(params: PlaceOrderParams): PlaceOrderResponse =
        executePlaceOrder(apiClient, config, params)

    /**
     * Places multiple orders in a single request.
     *
     * Returns complete response including errors and summary for partial success (207) scenarios.
     *
     * **Kotlin:**
     * ```kotlin
     * val orders = listOf(
     *     MultiOrderParams { correlationId = "ord1"; instrumentToken = "NSE_EQ|INE669E01016"; quantity = 1; product = Product.DELIVERY; validity = Validity.DAY; price = 0.0; orderType = OrderType.MARKET; transactionType = TransactionType.BUY },
     *     MultiOrderParams { correlationId = "ord2"; instrumentToken = "NSE_EQ|INE002A01018"; quantity = 1; product = Product.DELIVERY; validity = Validity.DAY; price = 0.0; orderType = OrderType.MARKET; transactionType = TransactionType.BUY }
     * )
     * val response = upstox.placeMultiOrder(orders)
     * println("Success: ${response.isSuccess}, Placed: ${response.data?.size}")
     * ```
     *
     * **Java:**
     * ```java
     * List<MultiOrderParams> orders = Arrays.asList(
     *     new MultiOrderParamsBuilder().correlationId("ord1").instrumentToken("NSE_EQ|INE669E01016").quantity(1).product(Product.DELIVERY).validity(Validity.DAY).price(0.0).orderType(OrderType.MARKET).transactionType(TransactionType.BUY).build(),
     *     new MultiOrderParamsBuilder().correlationId("ord2").instrumentToken("NSE_EQ|INE002A01018").quantity(1).product(Product.DELIVERY).validity(Validity.DAY).price(0.0).orderType(OrderType.MARKET).transactionType(TransactionType.BUY).build()
     * );
     *
     * PlaceMultiOrderResponse response = upstox.placeMultiOrder(orders);
     * ```
     *
     * @param orders List of order parameters (max 25)
     * @return [PlaceMultiOrderResponse] with data, errors, and summary
     * @see <a href="https://upstox.com/developer/api-documentation/place-multi-order">Place Multi Order API</a>
     */
    fun placeMultiOrder(orders: List<MultiOrderParams>): PlaceMultiOrderResponse =
        executePlaceMultiOrder(apiClient, config, orders)

    /**
     * Modifies an existing order.
     *
     * **Kotlin:**
     * ```kotlin
     * val response = upstox.modifyOrder(ModifyOrderParams {
     *     orderId = "order-123"
     *     validity = Validity.DAY
     *     orderType = OrderType.LIMIT
     *     price = 500.0
     *     quantity = 2
     * })
     * println("Modified: ${response.orderId}")
     * ```
     *
     * **Java:**
     * ```java
     * ModifyOrderParams params = new ModifyOrderParamsBuilder()
     *     .orderId("order-123")
     *     .validity(Validity.DAY)
     *     .orderType(OrderType.LIMIT)
     *     .price(500.0)
     *     .quantity(2)
     *     .build();
     *
     * ModifyOrderResponse response = upstox.modifyOrder(params);
     * ```
     *
     * @param params Modification parameters
     * @return [ModifyOrderResponse] containing modified order ID and latency
     * @see <a href="https://upstox.com/developer/api-documentation/v3/modify-order">Modify Order API</a>
     */
    fun modifyOrder(params: ModifyOrderParams): ModifyOrderResponse =
        executeModifyOrder(apiClient, config, params)

    /**
     * Cancels an existing order.
     *
     * **Kotlin:**
     * ```kotlin
     * val response = upstox.cancelOrder("order-123")
     * println("Cancelled: ${response.orderId}")
     * ```
     *
     * **Java:**
     * ```java
     * CancelOrderResponse response = upstox.cancelOrder("order-123");
     * System.out.println("Cancelled: " + response.getOrderId());
     * ```
     *
     * @param orderId The order ID to cancel
     * @return [CancelOrderResponse] containing cancelled order ID and latency
     * @see <a href="https://upstox.com/developer/api-documentation/v3/cancel-order">Cancel Order API</a>
     */
    fun cancelOrder(orderId: String): CancelOrderResponse =
        executeCancelOrder(apiClient, config, orderId)

    /**
     * Cancels multiple orders by segment or tag filter.
     *
     * Returns complete response including errors and summary for partial success (207) scenarios.
     *
     * **Kotlin:**
     * ```kotlin
     * // Cancel all orders in NSE F&O segment
     * val response = upstox.cancelMultiOrder(segment = Segment.NSE_FO)
     * println("Cancelled: ${response.orderIds.size}")
     *
     * // Cancel by tag
     * val response2 = upstox.cancelMultiOrder(tag = "my-strategy")
     * ```
     *
     * **Java:**
     * ```java
     * // Cancel by segment
     * MultiOrderResponse response = upstox.cancelMultiOrder(Segment.NSE_FO, null);
     *
     * // Cancel by tag
     * MultiOrderResponse response2 = upstox.cancelMultiOrder(null, "my-strategy");
     * ```
     *
     * @param segment Market segment filter (only orders in this segment)
     * @param tag Order tag filter (only orders with this tag). Java users can pass null for segment to filter by tag only.
     * @return [MultiOrderResponse] with cancelled order IDs and summary
     * @see <a href="https://upstox.com/developer/api-documentation/cancel-multi-order">Cancel Multi Order API</a>
     */
    @JvmOverloads
    fun cancelMultiOrder(segment: Segment? = null, tag: String? = null): MultiOrderResponse =
        executeCancelMultiOrder(apiClient, segment, tag)

    /**
     * Exits all open positions.
     *
     * Returns complete response including errors and summary for partial success (207) scenarios.
     *
     * **Kotlin:**
     * ```kotlin
     * // Exit all positions
     * val response = upstox.exitAllPositions()
     * println("Exited ${response.orderIds.size} positions")
     *
     * // Exit only NSE F&O positions
     * val response2 = upstox.exitAllPositions(segment = Segment.NSE_FO)
     * ```
     *
     * **Java:**
     * ```java
     * // Exit all positions
     * MultiOrderResponse response = upstox.exitAllPositions();
     *
     * // Exit by segment
     * MultiOrderResponse response2 = upstox.exitAllPositions(Segment.NSE_FO, null);
     * ```
     *
     * @param segment Market segment filter (only positions in this segment will be exited)
     * @param tag Order tag filter (valid only for intraday positions). Java users can pass null for segment to filter by tag only.
     * @return [MultiOrderResponse] with order IDs and summary
     * @see <a href="https://upstox.com/developer/api-documentation/exit-all-positions">Exit All Positions API</a>
     */
    @JvmOverloads
    fun exitAllPositions(segment: Segment? = null, tag: String? = null): MultiOrderResponse =
        executeExitAllPositions(apiClient, segment, tag)

    /**
     * Gets details of a specific order.
     *
     * **Kotlin:**
     * ```kotlin
     * val order = upstox.getOrderDetails("order-123")
     * println("Status: ${order.status}")
     * println("Filled: ${order.filledQuantity}/${order.quantity}")
     * ```
     *
     * **Java:**
     * ```java
     * Order order = upstox.getOrderDetails("order-123");
     * System.out.println("Status: " + order.getStatus());
     * ```
     *
     * @param orderId The order ID to fetch
     * @return [Order] containing full order details
     * @see <a href="https://upstox.com/developer/api-documentation/get-order-details">Get Order Details API</a>
     */
    fun getOrderDetails(orderId: String): Order =
        executeGetOrderDetails(apiClient, orderId)

    /**
     * Gets the history/audit trail of an order.
     *
     * At least one of orderId or tag must be provided.
     *
     * **Kotlin:**
     * ```kotlin
     * val history = upstox.getOrderHistory(orderId = "order-123")
     * history.forEach { order ->
     *     println("${order.orderTimestamp}: ${order.status}")
     * }
     * ```
     *
     * **Java:**
     * ```java
     * List<Order> history = upstox.getOrderHistory("order-123", null);
     * for (Order order : history) {
     *     System.out.println(order.getOrderTimestamp() + ": " + order.getStatus());
     * }
     * ```
     *
     * @param orderId Order reference ID (alphanumeric and hyphens only)
     * @param tag Unique tag identifier for filtering orders
     * @see <a href="https://upstox.com/developer/api-documentation/get-order-history">Get Order History API</a>
     */
    fun getOrderHistory(orderId: String? = null, tag: String? = null): List<Order> =
        executeGetOrderHistory(apiClient, orderId, tag)

    /**
     * Gets all orders (order book).
     *
     * **Kotlin:**
     * ```kotlin
     * val orders = upstox.getOrders()
     * orders.forEach { order ->
     *     println("${order.orderId}: ${order.tradingSymbol} - ${order.status}")
     * }
     * ```
     *
     * **Java:**
     * ```java
     * List<Order> orders = upstox.getOrders();
     * for (Order order : orders) {
     *     System.out.println(order.getOrderId() + ": " + order.getStatus());
     * }
     * ```
     *
     * @return List of [Order] for the day
     * @see <a href="https://upstox.com/developer/api-documentation/get-order-book">Get Orders API</a>
     */
    fun getOrders(): List<Order> = executeGetOrders(apiClient)

    /**
     * Gets all trades (trade book).
     *
     * **Kotlin:**
     * ```kotlin
     * val trades = upstox.getTrades()
     * trades.forEach { trade ->
     *     println("${trade.tradingSymbol}: ${trade.quantity} @ ${trade.averagePrice}")
     * }
     * ```
     *
     * **Java:**
     * ```java
     * List<Trade> trades = upstox.getTrades();
     * for (Trade trade : trades) {
     *     System.out.println(trade.getTradingSymbol() + ": " + trade.getQuantity());
     * }
     * ```
     *
     * @return List of [Trade] for the day
     * @see <a href="https://upstox.com/developer/api-documentation/get-trade-history">Get Trades API</a>
     */
    fun getTrades(): List<Trade> = executeGetTrades(apiClient)

    /**
     * Gets trades for a specific order.
     *
     * **Kotlin:**
     * ```kotlin
     * val trades = upstox.getTradesByOrder("order-123")
     * trades.forEach { println("Trade: ${it.tradeId} @ ${it.averagePrice}") }
     * ```
     *
     * **Java:**
     * ```java
     * List<Trade> trades = upstox.getTradesByOrder("order-123");
     * ```
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-trades-by-order">Get Trades By Order API</a>
     */
    fun getTradesByOrder(orderId: String): List<Trade> =
        executeGetTradesByOrder(apiClient, orderId)

    /**
     * Gets historical trades with pagination.
     *
     * Returns paginated response including metadata with total_records and total_pages.
     *
     * **Kotlin:**
     * ```kotlin
     * val response = upstox.getHistoricalTrades(HistoricalTradesParams {
     *     startDate = "2024-01-01"
     *     endDate = "2024-12-31"
     *     pageNumber = 1
     *     pageSize = 100
     * })
     * println("Total: ${response.metadata?.totalRecords}")
     * response.data.forEach { println("${it.tradingSymbol}: ${it.quantity}") }
     * ```
     *
     * **Java:**
     * ```java
     * HistoricalTradesParams params = new HistoricalTradesParamsBuilder()
     *     .startDate("2024-01-01")
     *     .endDate("2024-12-31")
     *     .pageNumber(1)
     *     .pageSize(100)
     *     .build();
     * PaginatedResponse<List<HistoricalTrade>> response = upstox.getHistoricalTrades(params);
     * ```
     *
     * @param params Query parameters including date range and pagination
     * @see <a href="https://upstox.com/developer/api-documentation/get-historical-trades">Get Historical Trades API</a>
     */
    fun getHistoricalTrades(params: HistoricalTradesParams): PaginatedResponse<List<HistoricalTrade>> =
        executeGetHistoricalTrades(apiClient, params)

    // ==================== GTT Orders ====================

    /**
     * Places a GTT (Good Till Triggered) order.
     *
     * **Kotlin:**
     * ```kotlin
     * val response = upstox.placeGttOrder(PlaceGttOrderParams {
     *     type = GttType.SINGLE
     *     instrumentToken = "NSE_EQ|INE669E01016"
     *     quantity = 1
     *     product = Product.DELIVERY
     *     transactionType = TransactionType.BUY
     *     rules = listOf(GttRuleParams {
     *         strategy = GttStrategy.ENTRY
     *         triggerType = GttTriggerType.BELOW
     *         triggerPrice = 450.0
     *         limitPrice = 450.0
     *     })
     * })
     * println("GTT Order IDs: ${response.gttOrderIds}")
     * ```
     *
     * **Java:**
     * ```java
     * PlaceGttOrderParams params = new PlaceGttOrderParamsBuilder()
     *     .type(GttType.SINGLE)
     *     .instrumentToken("NSE_EQ|INE669E01016")
     *     .quantity(1)
     *     .product(Product.DELIVERY)
     *     .transactionType(TransactionType.BUY)
     *     .rules(List.of(new GttRuleParamsBuilder()
     *         .strategy(GttStrategy.ENTRY)
     *         .triggerType(GttTriggerType.BELOW)
     *         .triggerPrice(450.0)
     *         .limitPrice(450.0)
     *         .build()))
     *     .build();
     *
     * GttOrderResponse response = upstox.placeGttOrder(params);
     * ```
     *
     * @param params GTT order parameters
     * @return [GttOrderResponse] containing GTT order IDs and latency
     * @see <a href="https://upstox.com/developer/api-documentation/place-gtt-order">Place GTT Order API</a>
     */
    fun placeGttOrder(params: PlaceGttOrderParams): GttOrderResponse =
        executePlaceGttOrder(apiClient, params)

    /**
     * Modifies an existing GTT order.
     *
     * **Kotlin:**
     * ```kotlin
     * val response = upstox.modifyGttOrder(ModifyGttOrderParams {
     *     gttOrderId = "gtt-123"
     *     quantity = 2
     *     rules = listOf(GttRuleParams {
     *         strategy = GttStrategy.ENTRY
     *         triggerType = GttTriggerType.BELOW
     *         triggerPrice = 440.0
     *         limitPrice = 440.0
     *     })
     * })
     * ```
     *
     * **Java:**
     * ```java
     * ModifyGttOrderParams params = new ModifyGttOrderParamsBuilder()
     *     .gttOrderId("gtt-123")
     *     .quantity(2)
     *     .rules(List.of(new GttRuleParamsBuilder()
     *         .strategy(GttStrategy.ENTRY)
     *         .triggerType(GttTriggerType.BELOW)
     *         .triggerPrice(440.0)
     *         .limitPrice(440.0)
     *         .build()))
     *     .build();
     * GttOrderResponse response = upstox.modifyGttOrder(params);
     * ```
     *
     * @see <a href="https://upstox.com/developer/api-documentation/modify-gtt-order">Modify GTT Order API</a>
     */
    fun modifyGttOrder(params: ModifyGttOrderParams): GttOrderResponse =
        executeModifyGttOrder(apiClient, params)

    /**
     * Cancels a GTT order.
     *
     * **Kotlin:**
     * ```kotlin
     * val response = upstox.cancelGttOrder("gtt-123")
     * println("Cancelled: ${response.gttOrderIds}")
     * ```
     *
     * **Java:**
     * ```java
     * GttOrderResponse response = upstox.cancelGttOrder("gtt-123");
     * ```
     *
     * @see <a href="https://upstox.com/developer/api-documentation/cancel-gtt-order">Cancel GTT Order API</a>
     */
    fun cancelGttOrder(gttOrderId: String): GttOrderResponse =
        executeCancelGttOrder(apiClient, gttOrderId)

    /**
     * Gets GTT orders. If gttOrderId is provided, returns details for that specific order.
     *
     * **Kotlin:**
     * ```kotlin
     * // Get all GTT orders
     * val allOrders = upstox.getGttOrders()
     * allOrders.orders.forEach { println("${it.gttOrderId}: ${it.status}") }
     *
     * // Get specific GTT order
     * val order = upstox.getGttOrders("gtt-123")
     * ```
     *
     * **Java:**
     * ```java
     * // Get all GTT orders
     * GetGttOrdersResponse allOrders = upstox.getGttOrders();
     *
     * // Get specific GTT order
     * GetGttOrdersResponse order = upstox.getGttOrders("gtt-123");
     * ```
     *
     * @param gttOrderId Optional GTT order ID to get specific order details
     * @see <a href="https://upstox.com/developer/api-documentation/get-gtt-order-details">Get GTT Orders API</a>
     */
    @JvmOverloads
    fun getGttOrders(gttOrderId: String? = null): GetGttOrdersResponse =
        executeGetGttOrders(apiClient, gttOrderId)

    // ==================== Portfolio ====================

    /**
     * Gets all current positions.
     *
     * **Kotlin:**
     * ```kotlin
     * val positions = upstox.getPositions()
     * positions.forEach { position ->
     *     println("${position.tradingSymbol}: PnL=${position.pnl}")
     * }
     * ```
     *
     * **Java:**
     * ```java
     * List<Position> positions = upstox.getPositions();
     * for (Position position : positions) {
     *     System.out.println(position.getTradingSymbol() + ": " + position.getPnl());
     * }
     * ```
     *
     * @return List of [Position] for the day
     * @see <a href="https://upstox.com/developer/api-documentation/get-positions">Get Positions API</a>
     */
    fun getPositions(): List<Position> = executeGetPositions(apiClient)

    /**
     * Gets MTF (Margin Trading Facility) positions.
     *
     * **Kotlin:**
     * ```kotlin
     * val mtfPositions = upstox.getMtfPositions()
     * mtfPositions.forEach { println("${it.tradingSymbol}: ${it.quantity}") }
     * ```
     *
     * **Java:**
     * ```java
     * List<Position> mtfPositions = upstox.getMtfPositions();
     * ```
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-mtf-positions">Get MTF Positions API</a>
     */
    fun getMtfPositions(): List<Position> = executeGetMtfPositions(apiClient)

    /**
     * Converts a position from one product type to another.
     *
     * **Kotlin:**
     * ```kotlin
     * val response = upstox.convertPosition(ConvertPositionParams {
     *     instrumentToken = "NSE_EQ|INE669E01016"
     *     quantity = 1
     *     newProduct = Product.DELIVERY
     *     oldProduct = Product.INTRADAY
     *     transactionType = TransactionType.BUY
     * })
     * println("Converted: ${response.status}")
     * ```
     *
     * **Java:**
     * ```java
     * ConvertPositionParams params = new ConvertPositionParamsBuilder()
     *     .instrumentToken("NSE_EQ|INE669E01016")
     *     .quantity(1)
     *     .newProduct(Product.DELIVERY)
     *     .oldProduct(Product.INTRADAY)
     *     .transactionType(TransactionType.BUY)
     *     .build();
     * ConvertPositionResponse response = upstox.convertPosition(params);
     * ```
     *
     * @see <a href="https://upstox.com/developer/api-documentation/convert-positions">Convert Position API</a>
     */
    fun convertPosition(params: ConvertPositionParams): ConvertPositionResponse =
        executeConvertPosition(apiClient, params)

    /**
     * Gets all holdings in the demat account.
     *
     * **Kotlin:**
     * ```kotlin
     * val holdings = upstox.getHoldings()
     * holdings.forEach { holding ->
     *     println("${holding.tradingSymbol}: ${holding.quantity} @ ${holding.averagePrice}")
     * }
     * ```
     *
     * **Java:**
     * ```java
     * List<Holding> holdings = upstox.getHoldings();
     * for (Holding holding : holdings) {
     *     System.out.println(holding.getTradingSymbol() + ": " + holding.getQuantity());
     * }
     * ```
     *
     * @return List of [Holding] in demat account
     * @see <a href="https://upstox.com/developer/api-documentation/get-holdings">Get Holdings API</a>
     */
    fun getHoldings(): List<Holding> = executeGetHoldings(apiClient)

    // ==================== Charges ====================

    /**
     * Gets brokerage charges for a potential trade.
     *
     * **Kotlin:**
     * ```kotlin
     * val response = upstox.getBrokerage(BrokerageParams {
     *     instrumentToken = "NSE_EQ|INE669E01016"
     *     quantity = 10
     *     product = Product.DELIVERY
     *     transactionType = TransactionType.BUY
     *     price = 500.0
     * })
     * println("Total charges: ${response.charges?.total}")
     * ```
     *
     * **Java:**
     * ```java
     * BrokerageParams params = new BrokerageParamsBuilder()
     *     .instrumentToken("NSE_EQ|INE669E01016")
     *     .quantity(10)
     *     .product(Product.DELIVERY)
     *     .transactionType(TransactionType.BUY)
     *     .price(500.0)
     *     .build();
     * BrokerageResponse response = upstox.getBrokerage(params);
     * ```
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-brokerage">Get Brokerage API</a>
     */
    fun getBrokerage(params: BrokerageParams): BrokerageResponse =
        executeGetBrokerage(apiClient, params)

    // ==================== Margins ====================

    /**
     * Gets margin requirements for placing orders.
     *
     * **Kotlin:**
     * ```kotlin
     * val response = upstox.getMargin(listOf(
     *     MarginInstrumentParams {
     *         instrumentKey = "NSE_FO|NIFTY25DEC24000CE"
     *         quantity = 50
     *         transactionType = TransactionType.BUY
     *         product = Product.INTRADAY
     *     }
     * ))
     * println("Required margin: ${response.required?.totalMargin}")
     * ```
     *
     * **Java:**
     * ```java
     * List<MarginInstrumentParams> instruments = List.of(
     *     new MarginInstrumentParamsBuilder()
     *         .instrumentKey("NSE_FO|NIFTY25DEC24000CE")
     *         .quantity(50)
     *         .transactionType(TransactionType.BUY)
     *         .product(Product.INTRADAY)
     *         .build()
     * );
     * MarginResponse response = upstox.getMargin(instruments);
     * ```
     *
     * @param instruments List of instruments for margin calculation (max 20, no duplicates)
     * @see <a href="https://upstox.com/developer/api-documentation/margin">Margin API</a>
     */
    fun getMargin(instruments: List<MarginInstrumentParams>): MarginResponse =
        executeGetMargin(apiClient, instruments)

    // ==================== Trade P&L ====================

    /**
     * Gets metadata about the P&L report.
     *
     * **Kotlin:**
     * ```kotlin
     * val metadata = upstox.getTradePnlMetadata(TradePnlMetadataParams {
     *     financialYear = "2425"
     *     segment = Segment.NSE_EQ
     * })
     * println("Trades from: ${metadata.startDate} to ${metadata.endDate}")
     * ```
     *
     * **Java:**
     * ```java
     * TradePnlMetadataParams params = new TradePnlMetadataParamsBuilder()
     *     .financialYear("2425")
     *     .segment(Segment.NSE_EQ)
     *     .build();
     * TradePnlMetadata metadata = upstox.getTradePnlMetadata(params);
     * ```
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-report-meta-data">Get Report Metadata API</a>
     */
    fun getTradePnlMetadata(params: TradePnlMetadataParams): TradePnlMetadata =
        executeGetTradePnlMetadata(apiClient, params)

    /**
     * Gets the profit and loss report.
     *
     * **Kotlin:**
     * ```kotlin
     * val response = upstox.getTradePnlReport(TradePnlReportParams {
     *     financialYear = "2425"
     *     segment = Segment.NSE_EQ
     *     pageNumber = 1
     *     pageSize = 100
     * })
     * response.data.forEach { println("${it.tradingSymbol}: PnL=${it.pnl}") }
     * ```
     *
     * **Java:**
     * ```java
     * TradePnlReportParams params = new TradePnlReportParamsBuilder()
     *     .financialYear("2425")
     *     .segment(Segment.NSE_EQ)
     *     .pageNumber(1)
     *     .pageSize(100)
     *     .build();
     * PaginatedResponse<List<TradePnlEntry>> response = upstox.getTradePnlReport(params);
     * ```
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-profit-and-loss-report">Get P&L Report API</a>
     */
    fun getTradePnlReport(params: TradePnlReportParams): PaginatedResponse<List<TradePnlEntry>> =
        executeGetTradePnlReport(apiClient, params)

    /**
     * Gets trade charges breakdown for a period.
     *
     * **Kotlin:**
     * ```kotlin
     * val response = upstox.getTradeCharges(TradeChargesParams {
     *     financialYear = "2425"
     *     segment = Segment.NSE_EQ
     * })
     * println("Total charges: ${response.totalCharges}")
     * ```
     *
     * **Java:**
     * ```java
     * TradeChargesParams params = new TradeChargesParamsBuilder()
     *     .financialYear("2425")
     *     .segment(Segment.NSE_EQ)
     *     .build();
     * TradeChargesResponse response = upstox.getTradeCharges(params);
     * ```
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-trade-charges">Get Trade Charges API</a>
     */
    fun getTradeCharges(params: TradeChargesParams): TradeChargesResponse =
        executeGetTradeCharges(apiClient, params)

    // ==================== Historical Data ====================

    /**
     * Gets historical candle data.
     *
     * Interval ranges by unit:
     * - minutes: 1-300
     * - hours: 1-5
     * - days: 1 only
     * - weeks: 1 only
     * - months: 1 only
     *
     * **Kotlin:**
     * ```kotlin
     * // Get daily candles for last month
     * val candles = upstox.getHistoricalCandles(
     *     instrumentKey = "NSE_EQ|INE669E01016",
     *     unit = CandleUnit.DAYS,
     *     interval = 1,
     *     toDate = "2024-12-20",
     *     fromDate = "2024-11-20"
     * )
     * candles.toCandles().forEach { candle ->
     *     println("${candle.timestamp}: O=${candle.open} H=${candle.high} L=${candle.low} C=${candle.close}")
     * }
     * ```
     *
     * **Java:**
     * ```java
     * CandleData candles = upstox.getHistoricalCandles(
     *     "NSE_EQ|INE669E01016",
     *     CandleUnit.DAYS,
     *     1,
     *     "2024-12-20",
     *     "2024-11-20"
     * );
     * ```
     *
     * @param instrumentKey The unique identifier for the financial instrument
     * @param unit Timeframe unit: minutes, hours, days, weeks, months
     * @param interval Numeric interval value (valid ranges depend on unit)
     * @param toDate End date (inclusive) in YYYY-MM-DD format
     * @param fromDate Start date in YYYY-MM-DD format (optional)
     * @return [CandleData] containing OHLCV data
     * @see <a href="https://upstox.com/developer/api-documentation/v3/get-historical-candle-data">Historical Candle API</a>
     */
    @JvmOverloads
    fun getHistoricalCandles(
        instrumentKey: String,
        unit: CandleUnit,
        interval: Int,
        toDate: String,
        fromDate: String? = null
    ): CandleData = executeGetHistoricalCandles(apiClient, instrumentKey, unit, interval, toDate, fromDate)

    /**
     * Gets intraday candle data for the current trading day.
     *
     * **Kotlin:**
     * ```kotlin
     * val candles = upstox.getIntradayCandles(
     *     instrumentKey = "NSE_EQ|INE669E01016",
     *     unit = CandleUnit.MINUTES,
     *     interval = 5
     * )
     * candles.toCandles().forEach { println("${it.timestamp}: ${it.close}") }
     * ```
     *
     * **Java:**
     * ```java
     * CandleData candles = upstox.getIntradayCandles(
     *     "NSE_EQ|INE669E01016",
     *     CandleUnit.MINUTES,
     *     5
     * );
     * ```
     *
     * @param instrumentKey The unique identifier for the financial instrument
     * @param unit Timeframe unit: minutes, hours, or days
     * @param interval Numeric interval value (minutes: 1-300, hours: 1-5, days: 1)
     * @see <a href="https://upstox.com/developer/api-documentation/v3/get-intra-day-candle-data">Intraday Candle API</a>
     */
    fun getIntradayCandles(instrumentKey: String, unit: CandleUnit, interval: Int): CandleData =
        executeGetIntradayCandles(apiClient, instrumentKey, unit, interval)

    // ==================== Market Quote ====================

    /**
     * Gets full market quotes for instruments.
     *
     * **Kotlin:**
     * ```kotlin
     * val quotes = upstox.getFullQuote(listOf("NSE_EQ|INE669E01016", "NSE_EQ|INE002A01018"))
     * quotes.forEach { (key, quote) ->
     *     println("$key: LTP=${quote.ltp}, Change=${quote.netChange}")
     * }
     * ```
     *
     * **Java:**
     * ```java
     * Map<String, FullMarketQuote> quotes = upstox.getFullQuote(
     *     List.of("NSE_EQ|INE669E01016", "NSE_EQ|INE002A01018")
     * );
     * ```
     *
     * @param instrumentKeys List of instrument keys (max 500)
     * @return Map of instrument key to [FullMarketQuote]
     * @see <a href="https://upstox.com/developer/api-documentation/get-full-market-quote">Full Market Quote API</a>
     */
    fun getFullQuote(instrumentKeys: List<String>): Map<String, FullMarketQuote> =
        executeGetFullQuote(apiClient, instrumentKeys)

    /**
     * Gets OHLC quotes for instruments.
     *
     * **Kotlin:**
     * ```kotlin
     * val ohlc = upstox.getOhlc(
     *     listOf("NSE_EQ|INE669E01016"),
     *     OhlcInterval.DAY_1
     * )
     * ohlc.forEach { (key, quote) ->
     *     println("$key: O=${quote.ohlc?.open} H=${quote.ohlc?.high} L=${quote.ohlc?.low} C=${quote.ohlc?.close}")
     * }
     * ```
     *
     * **Java:**
     * ```java
     * Map<String, OhlcQuote> ohlc = upstox.getOhlc(
     *     List.of("NSE_EQ|INE669E01016"),
     *     OhlcInterval.DAY_1
     * );
     * ```
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-market-quote-ohlc-v3">OHLC Quote API</a>
     */
    fun getOhlc(instrumentKeys: List<String>, interval: OhlcInterval): Map<String, OhlcQuote> =
        executeGetOhlc(apiClient, instrumentKeys, interval)

    /**
     * Gets LTP (Last Traded Price) quotes for instruments.
     *
     * **Kotlin:**
     * ```kotlin
     * val ltps = upstox.getLtp(listOf("NSE_EQ|INE669E01016"))
     * ltps.forEach { (key, quote) ->
     *     println("$key: LTP=${quote.ltp}")
     * }
     * ```
     *
     * **Java:**
     * ```java
     * Map<String, LtpQuote> ltps = upstox.getLtp(List.of("NSE_EQ|INE669E01016"));
     * ```
     *
     * @param instrumentKeys List of instrument keys (max 500)
     * @return Map of instrument key to [LtpQuote]
     * @see <a href="https://upstox.com/developer/api-documentation/ltp-v3">LTP API</a>
     */
    fun getLtp(instrumentKeys: List<String>): Map<String, LtpQuote> =
        executeGetLtp(apiClient, instrumentKeys)

    /**
     * Gets option Greeks for instruments.
     *
     * **Kotlin:**
     * ```kotlin
     * val greeks = upstox.getOptionGreeks(listOf("NSE_FO|NIFTY25DEC24000CE"))
     * greeks.forEach { (key, quote) ->
     *     println("$key: Delta=${quote.greeks?.delta} IV=${quote.greeks?.iv}")
     * }
     * ```
     *
     * **Java:**
     * ```java
     * Map<String, OptionGreeksQuote> greeks = upstox.getOptionGreeks(
     *     List.of("NSE_FO|NIFTY25DEC24000CE")
     * );
     * ```
     *
     * @see <a href="https://upstox.com/developer/api-documentation/option-greek">Option Greek API</a>
     */
    fun getOptionGreeks(instrumentKeys: List<String>): Map<String, OptionGreeksQuote> =
        executeGetOptionGreeks(apiClient, instrumentKeys)

    // ==================== Market Info ====================

    /**
     * Gets all market holidays for the year.
     *
     * **Kotlin:**
     * ```kotlin
     * val holidays = upstox.getMarketHolidays()
     * holidays.forEach { println("${it.date}: ${it.description}") }
     * ```
     *
     * **Java:**
     * ```java
     * List<MarketHoliday> holidays = upstox.getMarketHolidays();
     * ```
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-market-holidays">Market Holidays API</a>
     */
    fun getMarketHolidays(): List<MarketHoliday> =
        executeGetMarketHolidays(apiClient)

    /**
     * Gets market holiday information for a specific date.
     *
     * **Kotlin:**
     * ```kotlin
     * val holiday = upstox.getMarketHoliday("2024-12-25")
     * holiday?.let { println("${it.date}: ${it.description}") }
     * ```
     *
     * **Java:**
     * ```java
     * MarketHoliday holiday = upstox.getMarketHoliday("2024-12-25");
     * ```
     *
     * @param date Date in YYYY-MM-DD format
     * @see <a href="https://upstox.com/developer/api-documentation/get-market-holidays">Market Holidays API</a>
     */
    fun getMarketHoliday(date: String): MarketHoliday? =
        executeGetMarketHoliday(apiClient, date)

    /**
     * Gets market timings for a specific date.
     *
     * **Kotlin:**
     * ```kotlin
     * val timings = upstox.getMarketTimings("2024-12-20")
     * timings.forEach { println("${it.segment}: ${it.openTime} - ${it.closeTime}") }
     * ```
     *
     * **Java:**
     * ```java
     * List<MarketTiming> timings = upstox.getMarketTimings("2024-12-20");
     * ```
     *
     * @param date Date in YYYY-MM-DD format
     * @see <a href="https://upstox.com/developer/api-documentation/get-market-timings">Market Timings API</a>
     */
    fun getMarketTimings(date: String): List<MarketTiming> =
        executeGetMarketTimings(apiClient, date)

    /**
     * Gets the current market status for an exchange.
     *
     * **Kotlin:**
     * ```kotlin
     * val status = upstox.getMarketStatus("NSE")
     * println("NSE status: ${status.status}")
     * ```
     *
     * **Java:**
     * ```java
     * MarketStatusResponse status = upstox.getMarketStatus("NSE");
     * ```
     *
     * @param exchange Exchange identifier (NSE, BSE, etc.)
     * @see <a href="https://upstox.com/developer/api-documentation/get-market-status">Market Status API</a>
     */
    fun getMarketStatus(exchange: String): MarketStatusResponse =
        executeGetMarketStatus(apiClient, exchange)

    // ==================== Option Chain ====================

    /**
     * Gets option contracts for an underlying instrument.
     *
     * **Kotlin:**
     * ```kotlin
     * // Get all option contracts for Nifty
     * val contracts = upstox.getOptionContracts("NSE_INDEX|Nifty 50")
     *
     * // Get contracts for specific expiry
     * val decContracts = upstox.getOptionContracts("NSE_INDEX|Nifty 50", "2024-12-26")
     * decContracts.forEach { println("${it.instrumentKey}: ${it.strikePrice}") }
     * ```
     *
     * **Java:**
     * ```java
     * // Get all option contracts
     * List<OptionContract> contracts = upstox.getOptionContracts("NSE_INDEX|Nifty 50");
     *
     * // Get contracts for specific expiry
     * List<OptionContract> decContracts = upstox.getOptionContracts("NSE_INDEX|Nifty 50", "2024-12-26");
     * ```
     *
     * @param instrumentKey Key of the underlying instrument
     * @param expiryDate Optional expiry date filter in YYYY-MM-DD format
     * @see <a href="https://upstox.com/developer/api-documentation/get-option-contracts">Option Contracts API</a>
     */
    @JvmOverloads
    fun getOptionContracts(instrumentKey: String, expiryDate: String? = null): List<OptionContract> =
        executeGetOptionContracts(apiClient, instrumentKey, expiryDate)

    /**
     * Gets the put/call option chain for an underlying.
     *
     * **Kotlin:**
     * ```kotlin
     * val chain = upstox.getOptionChain("NSE_INDEX|Nifty 50", "2024-12-26")
     * chain.forEach { entry ->
     *     println("Strike: ${entry.strikePrice}")
     *     println("  CE: LTP=${entry.callOptions?.marketData?.ltp}")
     *     println("  PE: LTP=${entry.putOptions?.marketData?.ltp}")
     * }
     * ```
     *
     * **Java:**
     * ```java
     * List<OptionChainEntry> chain = upstox.getOptionChain("NSE_INDEX|Nifty 50", "2024-12-26");
     * for (OptionChainEntry entry : chain) {
     *     System.out.println("Strike: " + entry.getStrikePrice());
     * }
     * ```
     *
     * @param instrumentKey Key of the underlying instrument
     * @param expiryDate Expiry date in YYYY-MM-DD format
     * @return List of [OptionChainEntry] with call and put options
     * @see <a href="https://upstox.com/developer/api-documentation/get-pc-option-chain">Option Chain API</a>
     */
    fun getOptionChain(instrumentKey: String, expiryDate: String): List<OptionChainEntry> =
        executeGetOptionChain(apiClient, instrumentKey, expiryDate)

    // ==================== Expired Instruments ====================

    /**
     * Gets available expiry dates for expired instruments.
     *
     * **Kotlin:**
     * ```kotlin
     * val expiries = upstox.getExpiries("NSE_INDEX|Nifty 50")
     * expiries.forEach { println("Expiry: $it") }
     * ```
     *
     * **Java:**
     * ```java
     * List<String> expiries = upstox.getExpiries("NSE_INDEX|Nifty 50");
     * ```
     *
     * @param instrumentKey Key of underlying symbol
     * @see <a href="https://upstox.com/developer/api-documentation/get-expiries">Get Expiries API</a>
     */
    fun getExpiries(instrumentKey: String): List<String> =
        executeGetExpiries(apiClient, instrumentKey)

    /**
     * Gets expired option contracts for an underlying and expiry date.
     *
     * **Kotlin:**
     * ```kotlin
     * val contracts = upstox.getExpiredOptionContracts("NSE_INDEX|Nifty 50", "2024-11-28")
     * contracts.forEach { println("${it.instrumentKey}: ${it.strikePrice}") }
     * ```
     *
     * **Java:**
     * ```java
     * List<ExpiredContract> contracts = upstox.getExpiredOptionContracts(
     *     "NSE_INDEX|Nifty 50", "2024-11-28"
     * );
     * ```
     *
     * @param instrumentKey Key of underlying instrument
     * @param expiryDate Expiry date in YYYY-MM-DD format
     * @see <a href="https://upstox.com/developer/api-documentation/get-expired-option-contracts">Expired Option Contracts API</a>
     */
    fun getExpiredOptionContracts(instrumentKey: String, expiryDate: String): List<ExpiredContract> =
        executeGetExpiredOptionContracts(apiClient, instrumentKey, expiryDate)

    /**
     * Gets expired future contracts for an underlying and expiry date.
     *
     * **Kotlin:**
     * ```kotlin
     * val contracts = upstox.getExpiredFutureContracts("NSE_INDEX|Nifty 50", "2024-11-28")
     * contracts.forEach { println("${it.instrumentKey}") }
     * ```
     *
     * **Java:**
     * ```java
     * List<ExpiredContract> contracts = upstox.getExpiredFutureContracts(
     *     "NSE_INDEX|Nifty 50", "2024-11-28"
     * );
     * ```
     *
     * @param instrumentKey Key of underlying instrument
     * @param expiryDate Expiry date in YYYY-MM-DD format
     * @see <a href="https://upstox.com/developer/api-documentation/get-expired-future-contracts">Expired Future Contracts API</a>
     */
    fun getExpiredFutureContracts(instrumentKey: String, expiryDate: String): List<ExpiredContract> =
        executeGetExpiredFutureContracts(apiClient, instrumentKey, expiryDate)

    /**
     * Gets historical candle data for an expired instrument.
     *
     * **Kotlin:**
     * ```kotlin
     * val candles = upstox.getExpiredHistoricalCandles(
     *     expiredInstrumentKey = "NSE_FO|NIFTY24NOV24000CE",
     *     interval = "day",
     *     toDate = "2024-11-28",
     *     fromDate = "2024-11-01"
     * )
     * candles.forEach { println("${it.timestamp}: O=${it.open} C=${it.close}") }
     * ```
     *
     * **Java:**
     * ```java
     * List<Candle> candles = upstox.getExpiredHistoricalCandles(
     *     "NSE_FO|NIFTY24NOV24000CE",
     *     "day",
     *     "2024-11-28",
     *     "2024-11-01"
     * );
     * ```
     *
     * @param expiredInstrumentKey Unique identifier for the expired instrument with expiry date
     * @param interval Timeframe: 1minute, 3minute, 5minute, 15minute, 30minute, or day
     * @param toDate End date (inclusive) in YYYY-MM-DD format
     * @param fromDate Start date in YYYY-MM-DD format
     * @return List of parsed Candle objects with timestamp, OHLC, volume, and open interest
     * @see <a href="https://upstox.com/developer/api-documentation/get-expired-historical-candle-data">Expired Historical Candle API</a>
     */
    fun getExpiredHistoricalCandles(
        expiredInstrumentKey: String,
        interval: String,
        toDate: String,
        fromDate: String
    ): List<Candle> = executeGetExpiredHistoricalCandles(apiClient, expiredInstrumentKey, interval, toDate, fromDate)

    // ==================== Instruments ====================

    /**
     * Gets instruments for the specified type.
     *
     * **Kotlin:**
     * ```kotlin
     * val nseInstruments = upstox.getInstruments(InstrumentDownloadType.NSE)
     * nseInstruments.forEach { println("${it.tradingSymbol}: ${it.instrumentKey}") }
     * ```
     *
     * **Java:**
     * ```java
     * List<Instrument> nseInstruments = upstox.getInstruments(InstrumentDownloadType.NSE);
     * for (Instrument inst : nseInstruments) {
     *     System.out.println(inst.getTradingSymbol() + ": " + inst.getInstrumentKey());
     * }
     * ```
     *
     * @see <a href="https://upstox.com/developer/api-documentation/instruments">Instruments API</a>
     */
    fun getInstruments(type: InstrumentDownloadType): List<Instrument> =
        executeGetInstruments(apiClient, type)

    /**
     * Gets the download URL for the specified instrument type.
     *
     * **Kotlin:**
     * ```kotlin
     * val url = upstox.getInstrumentsUrl(InstrumentDownloadType.NSE)
     * println("Download URL: $url")
     * ```
     *
     * **Java:**
     * ```java
     * String url = upstox.getInstrumentsUrl(InstrumentDownloadType.NSE);
     * System.out.println("Download URL: " + url);
     * ```
     */
    fun getInstrumentsUrl(type: InstrumentDownloadType): String =
        executeGetInstrumentsUrl(type)

    /**
     * Gets all instruments across all exchanges.
     *
     * **Kotlin:**
     * ```kotlin
     * val allInstruments = upstox.getAllInstruments()
     * println("Total instruments: ${allInstruments.size}")
     * ```
     *
     * **Java:**
     * ```java
     * List<Instrument> allInstruments = upstox.getAllInstruments();
     * System.out.println("Total instruments: " + allInstruments.size());
     * ```
     */
    fun getAllInstruments(): List<Instrument> = getInstruments(InstrumentDownloadType.COMPLETE)

    /**
     * Gets NSE instruments.
     *
     * **Kotlin:**
     * ```kotlin
     * val nseInstruments = upstox.getNseInstruments()
     * ```
     *
     * **Java:**
     * ```java
     * List<Instrument> nseInstruments = upstox.getNseInstruments();
     * ```
     */
    fun getNseInstruments(): List<Instrument> = getInstruments(InstrumentDownloadType.NSE)

    /**
     * Gets BSE instruments.
     *
     * **Kotlin:**
     * ```kotlin
     * val bseInstruments = upstox.getBseInstruments()
     * ```
     *
     * **Java:**
     * ```java
     * List<Instrument> bseInstruments = upstox.getBseInstruments();
     * ```
     */
    fun getBseInstruments(): List<Instrument> = getInstruments(InstrumentDownloadType.BSE)

    /**
     * Gets MCX instruments.
     *
     * **Kotlin:**
     * ```kotlin
     * val mcxInstruments = upstox.getMcxInstruments()
     * ```
     *
     * **Java:**
     * ```java
     * List<Instrument> mcxInstruments = upstox.getMcxInstruments();
     * ```
     */
    fun getMcxInstruments(): List<Instrument> = getInstruments(InstrumentDownloadType.MCX)

    // ==================== WebSocket Clients ====================

    /**
     * Creates a new Market Data Feed WebSocket client.
     *
     * **Kotlin:**
     * ```kotlin
     * val feedClient = upstox.createMarketDataFeedClient()
     * feedClient.addListener(object : MarketDataListener {
     *     override fun onConnected() {
     *         feedClient.subscribe(listOf("NSE_EQ|INE669E01016"), FeedMode.FULL)
     *     }
     *     override fun onDisconnected(code: Int, reason: String) {}
     *     override fun onError(error: Throwable) {}
     *
     *     // Optional callbacks
     *     override fun onReconnected() {
     *         println("Reconnected successfully!")
     *     }
     *     override fun onFullFeedUpdate(instrumentKey: String, feed: FullFeedTick) {
     *         println("$instrumentKey: LTP=${feed.ltp}")
     *     }
     * })
     * feedClient.connect()
     * ```
     *
     * **Java:**
     * ```java
     * MarketDataFeedClient feedClient = upstox.createMarketDataFeedClient();
     * feedClient.addListener(new MarketDataListener() {
     *     @Override
     *     public void onConnected() {
     *         feedClient.subscribe(List.of("NSE_EQ|INE669E01016"), FeedMode.FULL);
     *     }
     *     @Override
     *     public void onDisconnected(int code, String reason) {}
     *     @Override
     *     public void onError(Throwable error) {}
     *
     *     // Optional callbacks
     *     @Override
     *     public void onReconnected() {
     *         System.out.println("Reconnected successfully!");
     *     }
     *     @Override
     *     public void onFullFeedUpdate(String instrumentKey, FullFeedTick feed) {
     *         System.out.println(instrumentKey + ": LTP=" + feed.getLtp());
     *     }
     * });
     * feedClient.connect();
     * ```
     *
     * @param maxReconnectAttempts Maximum reconnection attempts, 1-20 (default: 5)
     * @param autoReconnectEnabled Enable automatic reconnection (default: true)
     * @param autoResubscribeEnabled Enable auto-resubscription after reconnect (default: true)
     * @return New [MarketDataFeedClient] instance
     */
    @JvmOverloads
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
        val client = MarketDataFeedClient(config, wsConfig, apiClient, clientProvider.getWsHttpClient(UpstoxConstants.WEBSOCKET_PING_INTERVAL_MS))
        webSocketClients.add(client)
        return client
    }

    /**
     * Creates a new Portfolio Stream WebSocket client.
     *
     * **Kotlin:**
     * ```kotlin
     * val portfolioClient = upstox.createPortfolioStreamClient()
     * portfolioClient.addListener(object : PortfolioStreamListener {
     *     override fun onConnected() {
     *         println("Connected to portfolio stream")
     *     }
     *     override fun onDisconnected(code: Int, reason: String) {}
     *     override fun onError(error: Throwable) {}
     *
     *     // Optional callbacks
     *     override fun onReconnected() {
     *         println("Reconnected successfully!")
     *     }
     *     override fun onOrderUpdate(orderUpdate: OrderUpdate) {
     *         println("Order ${orderUpdate.orderId}: ${orderUpdate.status}")
     *     }
     *     override fun onPositionUpdate(position: PositionUpdate) {
     *         println("Position: ${position.instrumentToken}")
     *     }
     * })
     * portfolioClient.connect()
     * ```
     *
     * **Java:**
     * ```java
     * PortfolioStreamClient portfolioClient = upstox.createPortfolioStreamClient();
     * portfolioClient.addListener(new PortfolioStreamListener() {
     *     @Override
     *     public void onConnected() {
     *         System.out.println("Connected to portfolio stream");
     *     }
     *     @Override
     *     public void onDisconnected(int code, String reason) {}
     *     @Override
     *     public void onError(Throwable error) {}
     *
     *     // Optional callbacks
     *     @Override
     *     public void onReconnected() {
     *         System.out.println("Reconnected successfully!");
     *     }
     *     @Override
     *     public void onOrderUpdate(OrderUpdate orderUpdate) {
     *         System.out.println("Order " + orderUpdate.getOrderId() + ": " + orderUpdate.getStatus());
     *     }
     *     @Override
     *     public void onPositionUpdate(PositionUpdate position) {
     *         System.out.println("Position: " + position.getInstrumentToken());
     *     }
     * });
     * portfolioClient.connect();
     * ```
     *
     * @param maxReconnectAttempts Maximum reconnection attempts, 1-20 (default: 5)
     * @param autoReconnectEnabled Enable automatic reconnection (default: true)
     * @return New [PortfolioStreamClient] instance
     */
    @JvmOverloads
    fun createPortfolioStreamClient(
        maxReconnectAttempts: Int = UpstoxConstants.WEBSOCKET_DEFAULT_MAX_RECONNECT_ATTEMPTS,
        autoReconnectEnabled: Boolean = true
    ): PortfolioStreamClient {
        require(maxReconnectAttempts in 1..20) { "maxReconnectAttempts must be between 1 and 20" }
        val wsConfig = UpstoxWebSocketConfig(
            maxReconnectAttempts = maxReconnectAttempts,
            autoReconnectEnabled = autoReconnectEnabled,
            autoResubscribeEnabled = false
        )
        val client = PortfolioStreamClient(config, wsConfig, apiClient, clientProvider.getWsHttpClient(UpstoxConstants.WEBSOCKET_PING_INTERVAL_MS))
        webSocketClients.add(client)
        return client
    }

    // ==================== Lifecycle ====================

    /**
     * Closes this SDK instance and releases all resources.
     */
    override fun close() {
        webSocketClients.forEach { it.close() }
        webSocketClients.clear()
        clientProvider.shutdown()
    }

    // ==================== Builder ====================

    /**
     * Builder for creating [Upstox] instances.
     */
    class Builder {
        private var accessToken: String = ""
        private var sandboxEnabled: Boolean = false
        private var sandboxToken: String = ""
        private var loggingEnabled: Boolean = false
        private var rateLimitRetries: Int = 0

        /**
         * Sets the OAuth access token (optional at build time).
         */
        fun accessToken(token: String): Builder = apply {
            this.accessToken = token
        }

        /**
         * Enables or disables sandbox mode for order APIs.
         *
         * @param enabled true to enable sandbox mode
         * @param token The sandbox token (required when enabling)
         */
        fun sandboxMode(enabled: Boolean, token: String? = null): Builder = apply {
            if (enabled) {
                require(!token.isNullOrBlank()) { "Sandbox token is required when enabling sandbox mode" }
                this.sandboxToken = token
            }
            this.sandboxEnabled = enabled
        }

        /**
         * Enables or disables HTTP request/response logging.
         */
        fun loggingEnabled(enabled: Boolean): Builder = apply {
            this.loggingEnabled = enabled
        }

        /**
         * Sets the number of automatic retries for rate-limited requests.
         *
         * @param retries Number of retries (0-5)
         */
        fun rateLimitRetries(retries: Int): Builder = apply {
            require(retries in 0..5) { "rateLimitRetries must be between 0 and 5" }
            this.rateLimitRetries = retries
        }

        /**
         * Builds and returns a new [Upstox] instance.
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

    companion object {
        /**
         * Creates a new builder for configuring an [Upstox] instance.
         *
         * Example:
         * ```kotlin
         * val upstox = Upstox.builder()
         *     .accessToken("your-token")
         *     .loggingEnabled(true)
         *     .build()
         *
         * val profile = upstox.getProfile()
         * val orders = upstox.getOrders()
         * ```
         */
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
