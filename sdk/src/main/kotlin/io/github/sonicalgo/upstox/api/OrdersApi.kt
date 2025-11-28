package io.github.sonicalgo.upstox.api

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConfig
import io.github.sonicalgo.upstox.config.UpstoxConfig.sandboxEnabled
import io.github.sonicalgo.upstox.config.UpstoxConstants
import io.github.sonicalgo.upstox.exception.UpstoxApiException
import io.github.sonicalgo.upstox.model.common.MultiOrderResponse
import io.github.sonicalgo.upstox.model.common.PaginatedResponse
import io.github.sonicalgo.upstox.model.common.UpstoxResponse
import io.github.sonicalgo.upstox.model.common.UpstoxResponseWithMetadata
import io.github.sonicalgo.upstox.model.request.*
import io.github.sonicalgo.upstox.model.response.*
import io.github.sonicalgo.upstox.validation.Validators

/**
 * API module for order management operations.
 *
 * Provides methods for placing, modifying, cancelling orders, and retrieving
 * order/trade information.
 *
 * Note: When sandbox mode is enabled via [sandboxEnabled],
 * order operations (place, modify, cancel) will use the sandbox token automatically.
 *
 * Example usage:
 * ```kotlin
 * val upstox = Upstox.getInstance()
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
 * // Get order details
 * val order = upstox.getOrdersApi().getOrderDetails(orderResponse.orderIds.first())
 * ```
 *
 * @see <a href="https://upstox.com/developer/api-documentation/v3/place-order">Place Order API</a>
 */
class OrdersApi private constructor() {

    /**
     * Returns the appropriate base URL for order operations.
     * Uses sandbox URL when sandbox mode is enabled, otherwise returns the regular URL.
     */
    private fun getOrderBaseUrl(regularUrl: String): String {
        return if (UpstoxConfig.sandboxEnabled) UpstoxConstants.BASE_URL_SANDBOX else regularUrl
    }

    /**
     * Places a single order.
     *
     * Uses the HFT (High Frequency Trading) endpoint for faster execution.
     * Returns order IDs which may be multiple if slicing is enabled.
     *
     * Note: Uses sandbox token and sandbox endpoint when [sandboxEnabled] is true.
     *
     * Example:
     * ```kotlin
     * val ordersApi = upstox.getOrdersApi()
     *
     * val response = ordersApi.placeOrder(PlaceOrderParams(
     *     instrumentToken = "NSE_EQ|INE669E01016",
     *     quantity = 1,
     *     product = Product.D,
     *     validity = Validity.DAY,
     *     price = 100.0,
     *     orderType = OrderType.LIMIT,
     *     transactionType = TransactionType.BUY,
     *     disclosedQuantity = 0,
     *     triggerPrice = 0.0,
     *     isAmo = false,
     *     tag = "my-order-tag"
     * ))
     * println("Order IDs: ${response.data?.orderIds}")
     * println("Latency: ${response.metadata?.latency}ms")
     * ```
     *
     * @param params Order placement parameters
     * @return [UpstoxResponseWithMetadata]<[PlaceOrderResponse]> Response with order IDs and latency metadata
     * @throws UpstoxApiException if order placement fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/v3/place-order">Place Order V3 API</a>
     */
    fun placeOrder(params: PlaceOrderParams): UpstoxResponseWithMetadata<PlaceOrderResponse> {
        val response: UpstoxResponse<PlaceOrderResponse> = ApiClient.post(
            endpoint = Endpoints.PLACE_ORDER,
            body = params,
            baseUrl = getOrderBaseUrl(UpstoxConstants.BASE_URL_HFT),
            unwrap = false
        )
        return UpstoxResponseWithMetadata(status = response.status, data = response.data)
    }

    /**
     * Places multiple orders in a single request.
     *
     * Allows placing up to 25 orders at once. BUY orders are executed
     * before SELL orders.
     *
     * Note: Uses sandbox token and sandbox endpoint when [sandboxEnabled] is true.
     *
     * Example:
     * ```kotlin
     * val ordersApi = upstox.getOrdersApi()
     *
     * val orders = listOf(
     *     MultiOrderParams(
     *         correlationId = "order-1",
     *         quantity = 1,
     *         product = Product.D,
     *         validity = Validity.DAY,
     *         price = 100.0,
     *         instrumentToken = "NSE_EQ|INE669E01016",
     *         orderType = OrderType.LIMIT,
     *         transactionType = TransactionType.BUY
     *     ),
     *     MultiOrderParams(
     *         correlationId = "order-2",
     *         quantity = 1,
     *         product = Product.D,
     *         validity = Validity.DAY,
     *         price = 200.0,
     *         instrumentToken = "NSE_EQ|INE002A01018",
     *         orderType = OrderType.LIMIT,
     *         transactionType = TransactionType.BUY
     *     )
     * )
     * val response = ordersApi.placeMultiOrder(orders)
     * ```
     *
     * @param orders List of order parameters (max 25)
     * @return [MultiOrderResponse]<List<[MultiOrderPlaceResponse]>> Multi-order response with success/error details
     * @throws UpstoxApiException if the request fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/place-multi-order">Place Multi Order API</a>
     */
    fun placeMultiOrder(orders: List<MultiOrderParams>): MultiOrderResponse<List<MultiOrderPlaceResponse>> {
        Validators.validateListSize(orders, MAX_MULTI_ORDER_COUNT, "placeMultiOrder")

        val response: UpstoxResponse<List<MultiOrderPlaceResponse>> = ApiClient.post(
            endpoint = Endpoints.PLACE_MULTI_ORDER,
            body = orders,
            baseUrl = getOrderBaseUrl(UpstoxConstants.BASE_URL_V2),
            unwrap = false
        )
        return MultiOrderResponse(
            status = response.status,
            data = response.data,
            errors = response.errors
        )
    }

    /**
     * Modifies an existing order.
     *
     * Note: Uses sandbox token and sandbox endpoint when [sandboxEnabled] is true.
     *
     * Example:
     * ```kotlin
     * val ordersApi = upstox.getOrdersApi()
     *
     * val response = ordersApi.modifyOrder(ModifyOrderParams(
     *     orderId = "240108010918222",
     *     quantity = 2,
     *     validity = Validity.DAY,
     *     price = 105.0,
     *     orderType = OrderType.LIMIT,
     *     disclosedQuantity = 0,
     *     triggerPrice = 0.0
     * ))
     * println("Modified Order ID: ${response.data?.orderId}")
     * ```
     *
     * @param params Order modification parameters
     * @return [UpstoxResponseWithMetadata]<[ModifyOrderResponse]> Response with modified order ID and latency
     * @throws UpstoxApiException if modification fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/v3/modify-order">Modify Order V3 API</a>
     */
    fun modifyOrder(params: ModifyOrderParams): UpstoxResponseWithMetadata<ModifyOrderResponse> {
        val response: UpstoxResponse<ModifyOrderResponse> = ApiClient.put(
            endpoint = Endpoints.MODIFY_ORDER,
            body = params,
            baseUrl = getOrderBaseUrl(UpstoxConstants.BASE_URL_HFT),
            unwrap = false
        )
        return UpstoxResponseWithMetadata(status = response.status, data = response.data)
    }

    /**
     * Cancels an existing order.
     *
     * Note: Uses sandbox token and sandbox endpoint when [sandboxEnabled] is true.
     *
     * Example:
     * ```kotlin
     * val ordersApi = upstox.getOrdersApi()
     *
     * val response = ordersApi.cancelOrder("240108010445130")
     * println("Cancelled Order ID: ${response.data?.orderId}")
     * ```
     *
     * @param orderId The order ID to cancel
     * @return [UpstoxResponseWithMetadata]<[CancelOrderResponse]> Response with cancelled order ID and latency
     * @throws UpstoxApiException if cancellation fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/v3/cancel-order">Cancel Order V3 API</a>
     */
    fun cancelOrder(orderId: String): UpstoxResponseWithMetadata<CancelOrderResponse> {
        val response: UpstoxResponse<CancelOrderResponse> = ApiClient.get(
            endpoint = Endpoints.CANCEL_ORDER,
            queryParams = mapOf("order_id" to orderId),
            baseUrl = getOrderBaseUrl(UpstoxConstants.BASE_URL_HFT),
            unwrap = false
        )
        return UpstoxResponseWithMetadata(status = response.status, data = response.data)
    }

    /**
     * Cancels multiple orders.
     *
     * Can filter by segment or tag. Maximum 50 orders per request.
     *
     * Note: Uses sandbox token when [sandboxEnabled] is true.
     *
     * Example:
     * ```kotlin
     * val ordersApi = upstox.getOrdersApi()
     *
     * // Cancel all orders
     * val response = ordersApi.cancelMultiOrder()
     *
     * // Cancel orders by segment
     * val segmentResponse = ordersApi.cancelMultiOrder(CancelMultiOrderParams(
     *     segment = Segment.NSE_EQ
     * ))
     * ```
     *
     * @param params Optional filter parameters
     * @return [MultiOrderResponse]<[MultiOrderCancelResponse]> Multi-order response with cancelled order IDs
     * @throws UpstoxApiException if cancellation fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/cancel-multi-order">Cancel Multi Order API</a>
     */
    @JvmOverloads
    fun cancelMultiOrder(params: CancelMultiOrderParams? = null): MultiOrderResponse<MultiOrderCancelResponse> {
        val queryParams = mutableMapOf<String, String?>()
        params?.segment?.let { queryParams["segment"] = it.name }
        params?.tag?.let { queryParams["tag"] = it }

        val response: UpstoxResponse<MultiOrderCancelResponse> = ApiClient.get(
            endpoint = Endpoints.CANCEL_MULTI_ORDER,
            queryParams = queryParams,
            baseUrl = UpstoxConstants.BASE_URL_V2,
            unwrap = false
        )
        return MultiOrderResponse(
            status = response.status,
            data = response.data,
            errors = response.errors
        )
    }

    /**
     * Exits all open positions.
     *
     * Creates market orders to square off all positions. BUY positions
     * are executed before SELL orders. Maximum 50 positions.
     *
     * Example:
     * ```kotlin
     * val ordersApi = upstox.getOrdersApi()
     *
     * // Exit all positions
     * val response = ordersApi.exitAllPositions()
     *
     * // Exit positions by segment
     * val segmentResponse = ordersApi.exitAllPositions(ExitAllPositionsParams(
     *     segment = Segment.NSE_FO
     * ))
     * ```
     *
     * @param params Optional filter parameters
     * @return [MultiOrderResponse]<[ExitPositionsResponse]> Response with created order IDs for exits
     * @throws UpstoxApiException if exit fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/exit-all-positions">Exit All Positions API</a>
     */
    @JvmOverloads
    fun exitAllPositions(params: ExitAllPositionsParams? = null): MultiOrderResponse<ExitPositionsResponse> {
        val response: UpstoxResponse<ExitPositionsResponse> = ApiClient.post(
            endpoint = Endpoints.EXIT_ALL_POSITIONS,
            body = params,
            baseUrl = UpstoxConstants.BASE_URL_V2,
            unwrap = false
        )
        return MultiOrderResponse(
            status = response.status,
            data = response.data,
            errors = response.errors
        )
    }

    /**
     * Gets details of a specific order.
     *
     * Example:
     * ```kotlin
     * val ordersApi = upstox.getOrdersApi()
     *
     * val order = ordersApi.getOrderDetails("240108010445130")
     * println("Status: ${order.status}")
     * println("Filled: ${order.filledQuantity}/${order.quantity}")
     * ```
     *
     * @param orderId The order ID to retrieve
     * @return [Order] Order details
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-order-details">Get Order Details API</a>
     */
    fun getOrderDetails(orderId: String): Order {
        return ApiClient.get(
            endpoint = Endpoints.GET_ORDER_DETAILS,
            queryParams = mapOf("order_id" to orderId),
            baseUrl = UpstoxConstants.BASE_URL_V2
        )
    }

    /**
     * Gets the history/audit trail of an order.
     *
     * Returns all status changes and modifications for an order.
     *
     * Example:
     * ```kotlin
     * val ordersApi = upstox.getOrdersApi()
     *
     * val history = ordersApi.getOrderHistory(orderId = "240108010445130")
     * history.forEach { entry ->
     *     println("${entry.orderTimestamp}: ${entry.status}")
     * }
     * ```
     *
     * @param orderId Order ID to get history for
     * @param tag Alternative: get history by tag
     * @return List of [Order] history entries
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-order-history">Get Order History API</a>
     */
    @JvmOverloads
    fun getOrderHistory(orderId: String? = null, tag: String? = null): List<Order> {
        val queryParams = mutableMapOf<String, String?>()
        orderId?.let { queryParams["order_id"] = it }
        tag?.let { queryParams["tag"] = it }

        return ApiClient.get(
            endpoint = Endpoints.GET_ORDER_HISTORY,
            queryParams = queryParams,
            baseUrl = UpstoxConstants.BASE_URL_V2
        )
    }

    /**
     * Gets all orders for the day (order book).
     *
     * Example:
     * ```kotlin
     * val ordersApi = upstox.getOrdersApi()
     *
     * val orders = ordersApi.getOrderBook()
     * orders.forEach { order ->
     *     println("${order.orderId}: ${order.tradingSymbol} - ${order.status}")
     * }
     * ```
     *
     * @return List of [Order] for the day
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-order-book">Get Order Book API</a>
     */
    fun getOrderBook(): List<Order> {
        return ApiClient.get(
            endpoint = Endpoints.GET_ORDER_BOOK,
            baseUrl = UpstoxConstants.BASE_URL_V2
        )
    }

    /**
     * Gets all trades for the day.
     *
     * Example:
     * ```kotlin
     * val ordersApi = upstox.getOrdersApi()
     *
     * val trades = ordersApi.getTrades()
     * trades.forEach { trade ->
     *     println("${trade.tradeId}: ${trade.tradingSymbol} @ ${trade.averagePrice}")
     * }
     * ```
     *
     * @return List of [Trade] for the day
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-trade-history">Get Trades API</a>
     */
    fun getTrades(): List<Trade> {
        return ApiClient.get(
            endpoint = Endpoints.GET_TRADES,
            baseUrl = UpstoxConstants.BASE_URL_V2
        )
    }

    /**
     * Gets trades for a specific order.
     *
     * Example:
     * ```kotlin
     * val ordersApi = upstox.getOrdersApi()
     *
     * val trades = ordersApi.getTradesByOrder("240108010445100")
     * trades.forEach { trade ->
     *     println("Trade ${trade.tradeId}: ${trade.quantity} @ ${trade.averagePrice}")
     * }
     * ```
     *
     * @param orderId Order ID to get trades for
     * @return List of [Trade] for the order
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-trades-by-order">Get Trades By Order API</a>
     */
    fun getTradesByOrder(orderId: String): List<Trade> {
        return ApiClient.get(
            endpoint = Endpoints.GET_TRADES_BY_ORDER,
            queryParams = mapOf("order_id" to orderId),
            baseUrl = UpstoxConstants.BASE_URL_V2
        )
    }

    /**
     * Gets historical trades with pagination.
     *
     * Data available for the last 3 financial years.
     *
     * Example:
     * ```kotlin
     * val ordersApi = upstox.getOrdersApi()
     *
     * val response = ordersApi.getHistoricalTrades(HistoricalTradesParams(
     *     startDate = "2023-04-01",
     *     endDate = "2024-03-31",
     *     pageNumber = 1,
     *     pageSize = 100,
     *     segment = TradeSegment.EQ
     * ))
     * response.data?.forEach { trade ->
     *     println("${trade.tradeDate}: ${trade.scripName} - ${trade.transactionType}")
     * }
     * ```
     *
     * @param params Historical trades query parameters
     * @return [PaginatedResponse]<List<[HistoricalTrade]>> Paginated list of historical trades
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-historical-trades">Get Historical Trades API</a>
     */
    fun getHistoricalTrades(params: HistoricalTradesParams): PaginatedResponse<List<HistoricalTrade>> {
        val response: UpstoxResponse<List<HistoricalTrade>> = ApiClient.get(
            endpoint = Endpoints.GET_HISTORICAL_TRADES,
            params = params,
            baseUrl = UpstoxConstants.BASE_URL_V2,
            unwrap = false
        )
        return PaginatedResponse(
            status = response.status,
            data = response.data,
            errors = response.errors
        )
    }

    internal object Endpoints {
        const val PLACE_ORDER = "/order/place"
        const val PLACE_MULTI_ORDER = "/order/multi/place"
        const val MODIFY_ORDER = "/order/modify"
        const val CANCEL_ORDER = "/order/cancel"
        const val CANCEL_MULTI_ORDER = "/order/multi/cancel"
        const val EXIT_ALL_POSITIONS = "/order/positions/exit"
        const val GET_ORDER_DETAILS = "/order/details"
        const val GET_ORDER_HISTORY = "/order/history"
        const val GET_ORDER_BOOK = "/order/retrieve-all"
        const val GET_TRADES = "/order/trades/get-trades-for-day"
        const val GET_TRADES_BY_ORDER = "/order/trades"
        const val GET_HISTORICAL_TRADES = "/charges/historical-trades"
    }

    companion object {
        /** Maximum number of orders that can be placed in a single placeMultiOrder call */
        private const val MAX_MULTI_ORDER_COUNT = 25

        internal val instance by lazy { OrdersApi() }
    }
}
