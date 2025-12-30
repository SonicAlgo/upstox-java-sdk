package io.github.sonicalgo.upstox.common

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.exception.UpstoxApiException
import io.github.sonicalgo.upstox.usecase.InstrumentType
import io.github.sonicalgo.upstox.usecase.UnderlyingType

// ============================================================================
// Internal Response Wrappers
// ============================================================================

/**
 * Standard Upstox API response wrapper.
 *
 * Used for parsing successful API responses. HTTP errors (4xx/5xx) throw
 * exceptions before reaching this wrapper.
 *
 * @param T The type of data contained in the response
 * @property status Status of the API call (always "success" for HTTP 200)
 * @property data The response data
 */
data class UpstoxResponse<T>(
    @JsonProperty("status")
    val status: String?,

    @JsonProperty("data")
    val data: T?
) {
    /**
     * Returns data or throws exception if null.
     *
     * @return The response data
     * @throws UpstoxApiException if data is null
     */
    fun dataOrThrow(): T = data ?: throw UpstoxApiException("Response data is null", null)
}

/**
 * Upstox API error details.
 *
 * Contains information about what went wrong with the API call.
 *
 * Common error codes:
 * - UDAPI10000: Request not supported by Upstox API
 * - UDAPI100016: Invalid Credentials
 * - UDAPI10005: Too Many Requests
 * - UDAPI100050: Invalid token
 * - UDAPI100500: Internal server error
 *
 * @property errorCode Specific error code identifier
 * @property message Human-readable error description
 * @property propertyPath Request component that triggered the error
 * @property invalidValue The problematic value that was submitted
 * @property orderId Associated order ID for order-related errors
 * @property instrumentKey Associated instrument key for instrument-related errors
 * @see <a href="https://upstox.com/developer/api-documentation/error-codes">Upstox Error Codes</a>
 */
data class UpstoxError(
    @JsonProperty("error_code")
    val errorCode: String?,

    @JsonProperty("message")
    val message: String?,

    @JsonProperty("property_path")
    val propertyPath: String?,

    @JsonProperty("invalid_value")
    val invalidValue: String?,

    @JsonProperty("order_id")
    val orderId: String?,

    @JsonProperty("instrument_key")
    val instrumentKey: String?
)

/**
 * Response wrapper with metadata including latency.
 *
 * Used by high-frequency trading (HFT) endpoints that provide
 * processing time information.
 *
 * @param T The type of data contained in the response
 * @property status Status of the API call
 * @property data The response data
 * @property metadata Metadata containing latency information
 */
data class UpstoxResponseWithMetadata<T>(
    @JsonProperty("status")
    val status: String?,

    @JsonProperty("data")
    val data: T?,

    @JsonProperty("metadata")
    val metadata: ResponseMetadata?
) {
    /**
     * Returns data or throws exception if null.
     *
     * @return The response data
     * @throws UpstoxApiException if data is null
     */
    fun dataOrThrow(): T = data ?: throw UpstoxApiException("Response data is null", null)
}

/**
 * Response metadata containing processing information.
 *
 * @property latency Time taken by API platform to process the request (milliseconds)
 */
data class ResponseMetadata(
    @JsonProperty("latency")
    val latency: Int?
)

/**
 * Summary statistics for multi-order operations.
 *
 * Used by PlaceMultiOrder, CancelMultiOrder, and ExitAllPositions APIs.
 *
 * @property total Total number of orders processed
 * @property payloadError Number of orders with payload errors (only for PlaceMultiOrder)
 * @property success Number of successfully processed orders
 * @property error Number of failed orders
 */
data class MultiOrderSummary(
    @JsonProperty("total")
    val total: Int?,

    @JsonProperty("payload_error")
    val payloadError: Int?,

    @JsonProperty("success")
    val success: Int?,

    @JsonProperty("error")
    val error: Int?
)

/**
 * Data containing order IDs for multi-order operations.
 *
 * Used by CancelMultiOrder and ExitAllPositions APIs.
 *
 * @property orderIds List of order IDs
 */
data class MultiOrderData(
    @JsonProperty("order_ids")
    val orderIds: List<String>?
)

/**
 * Error details for multi-order operations.
 *
 * @property correlationId Correlation ID for the failed order (PlaceMultiOrder only)
 * @property errorCode Specific error code identifier
 * @property message Human-readable error description
 * @property propertyPath Request component that triggered the error
 * @property invalidValue The problematic value that was submitted
 * @property orderId Associated order ID
 * @property instrumentKey Associated instrument key
 */
data class MultiOrderError(
    @JsonProperty("correlation_id")
    val correlationId: String?,

    @JsonProperty("error_code")
    val errorCode: String?,

    @JsonProperty("message")
    val message: String?,

    @JsonProperty("property_path")
    val propertyPath: String?,

    @JsonProperty("invalid_value")
    val invalidValue: String?,

    @JsonProperty("order_id")
    val orderId: String?,

    @JsonProperty("instrument_key")
    val instrumentKey: String?
)

/**
 * Response for multi-order operations (CancelMultiOrder, ExitAllPositions).
 *
 * @property status Response status: "success", "partial_success", or "error"
 * @property data Data containing order IDs
 * @property errors List of errors for failed operations
 * @property summary Summary statistics for the batch operation
 */
data class MultiOrderResponse(
    @JsonProperty("status")
    val status: String?,

    @JsonProperty("data")
    val data: MultiOrderData?,

    @JsonProperty("errors")
    val errors: List<MultiOrderError>?,

    @JsonProperty("summary")
    val summary: MultiOrderSummary?
) {
    val isSuccess: Boolean get() = status == "success"
    val isPartialSuccess: Boolean get() = status == "partial_success"
    val isError: Boolean get() = status != "success" && status != "partial_success"
    val orderIds: List<String> get() = data?.orderIds ?: emptyList()
}

/**
 * Pagination metadata for paginated responses.
 *
 * @property pageNumber Current page number (1-indexed)
 * @property pageSize Number of results per page
 * @property totalRecords Total number of matching records
 * @property totalPages Total number of available pages
 */
data class PageInfo(
    @JsonProperty("page_number")
    val pageNumber: Int?,

    @JsonProperty("page_size")
    val pageSize: Int?,

    @JsonProperty("total_records")
    val totalRecords: Int?,

    @JsonProperty("total_pages")
    val totalPages: Int?
)

/**
 * Response wrapper with pagination metadata.
 *
 * @param T The type of data contained in the response
 * @property status Status of the API call
 * @property data The response data
 * @property metaData Metadata containing pagination information
 */
data class PaginatedResponse<T>(
    @JsonProperty("status")
    val status: String?,

    @JsonProperty("data")
    val data: T?,

    @JsonProperty("meta_data")
    val metaData: PaginationMetadata?
) {
    /**
     * Returns data or throws exception if null.
     *
     * @return The response data
     * @throws UpstoxApiException if data is null
     */
    fun dataOrThrow(): T = data ?: throw UpstoxApiException("Response data is null", null)
}

/**
 * Pagination metadata wrapper.
 *
 * @property page Page information
 */
data class PaginationMetadata(
    @JsonProperty("page")
    val page: PageInfo?
)

// ============================================================================
// Shared Response Classes
// ============================================================================

/**
 * Complete order details.
 *
 * @property exchange Exchange identifier
 * @property product Product type
 * @property price Order placement price
 * @property quantity Order quantity
 * @property status Current order status
 * @property tag Custom order tag if provided
 * @property instrumentToken Instrument key identifier
 * @property placedBy User identifier who placed the order
 * @property tradingSymbol Trading symbol of the instrument
 * @property orderType Order type
 * @property validity Order validity
 * @property triggerPrice Trigger price for stop loss orders
 * @property disclosedQuantity Quantity disclosed in market depth
 * @property transactionType Transaction type
 * @property averagePrice Weighted average execution price
 * @property filledQuantity Quantity that has been executed
 * @property pendingQuantity Quantity yet to be executed
 * @property statusMessage Reason for rejection or cancellation
 * @property statusMessageRaw Raw status message from exchange
 * @property exchangeOrderId Exchange-assigned order ID
 * @property parentOrderId Parent order ID for CO orders
 * @property orderId Internal order ID
 * @property variety Order complexity classification
 * @property orderTimestamp Order placement timestamp
 * @property exchangeTimestamp Exchange timestamp for order events
 * @property isAmo Whether this is an After Market Order
 * @property orderRequestId Request sequence counter
 * @property orderRefId Internal order reference identifier
 * @see <a href="https://upstox.com/developer/api-documentation/get-order-details">Get Order Details API</a>
 */
data class Order(
    @JsonProperty("exchange")
    val exchange: Exchange?,

    @JsonProperty("product")
    val product: Product?,

    @JsonProperty("price")
    val price: Double?,

    @JsonProperty("quantity")
    val quantity: Int?,

    @JsonProperty("status")
    val status: OrderStatus?,

    @JsonProperty("tag")
    val tag: String?,

    @JsonProperty("instrument_token")
    val instrumentToken: String?,

    @JsonProperty("placed_by")
    val placedBy: String?,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String?,

    @JsonProperty("order_type")
    val orderType: OrderType?,

    @JsonProperty("validity")
    val validity: Validity?,

    @JsonProperty("trigger_price")
    val triggerPrice: Double?,

    @JsonProperty("disclosed_quantity")
    val disclosedQuantity: Int?,

    @JsonProperty("transaction_type")
    val transactionType: TransactionType?,

    @JsonProperty("average_price")
    val averagePrice: Double?,

    @JsonProperty("filled_quantity")
    val filledQuantity: Int?,

    @JsonProperty("pending_quantity")
    val pendingQuantity: Int?,

    @JsonProperty("status_message")
    val statusMessage: String?,

    @JsonProperty("status_message_raw")
    val statusMessageRaw: String?,

    @JsonProperty("exchange_order_id")
    val exchangeOrderId: String?,

    @JsonProperty("parent_order_id")
    val parentOrderId: String?,

    @JsonProperty("order_id")
    val orderId: String?,

    @JsonProperty("variety")
    val variety: OrderVariety?,

    @JsonProperty("order_timestamp")
    val orderTimestamp: String?,

    @JsonProperty("exchange_timestamp")
    val exchangeTimestamp: String?,

    @JsonProperty("is_amo")
    val isAmo: Boolean?,

    @JsonProperty("order_request_id")
    val orderRequestId: String?,

    @JsonProperty("order_ref_id")
    val orderRefId: String?
)

/**
 * Trade execution details.
 *
 * @property exchange Exchange identifier
 * @property product Product type
 * @property tradingSymbol Trading symbol
 * @property instrumentToken Instrument key identifier
 * @property orderType Order type
 * @property transactionType Transaction type
 * @property quantity Total quantity traded
 * @property exchangeOrderId Exchange-assigned order ID
 * @property orderId Internal order ID
 * @property exchangeTimestamp Trade execution timestamp
 * @property averagePrice Execution price per unit
 * @property tradeId Exchange-generated trade ID
 * @property orderRefId Internal order reference
 * @property orderTimestamp Order placement timestamp
 * @see <a href="https://upstox.com/developer/api-documentation/get-trade-history">Get Trades API</a>
 */
data class Trade(
    @JsonProperty("exchange")
    val exchange: Exchange?,

    @JsonProperty("product")
    val product: Product?,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String?,

    @JsonProperty("instrument_token")
    val instrumentToken: String?,

    @JsonProperty("order_type")
    val orderType: OrderType?,

    @JsonProperty("transaction_type")
    val transactionType: TransactionType?,

    @JsonProperty("quantity")
    val quantity: Int?,

    @JsonProperty("exchange_order_id")
    val exchangeOrderId: String?,

    @JsonProperty("order_id")
    val orderId: String?,

    @JsonProperty("exchange_timestamp")
    val exchangeTimestamp: String?,

    @JsonProperty("average_price")
    val averagePrice: Double?,

    @JsonProperty("trade_id")
    val tradeId: String?,

    @JsonProperty("order_ref_id")
    val orderRefId: String?,

    @JsonProperty("order_timestamp")
    val orderTimestamp: String?
)

/**
 * Position details.
 *
 * @property exchange Exchange identifier
 * @property multiplier Quantity/lot size multiplier for P&L calculations
 * @property value Net position value
 * @property pnl Profit/loss on the position
 * @property product Product type
 * @property instrumentToken Instrument key identifier
 * @property averagePrice Mean acquisition price for net quantity
 * @property buyValue Net bought quantities value
 * @property overnightQuantity Quantity carried forward from previous session
 * @property dayBuyValue Intraday purchase amount
 * @property dayBuyPrice Average intraday purchase price
 * @property overnightBuyAmount Previous session purchase amount
 * @property overnightBuyQuantity Previous session buy quantity
 * @property dayBuyQuantity Intraday buy quantity
 * @property daySellValue Intraday sale amount
 * @property daySellPrice Average intraday sale price
 * @property overnightSellAmount Previous session sale amount
 * @property overnightSellQuantity Previous session short quantity
 * @property daySellQuantity Intraday sale quantity
 * @property quantity Net remaining quantity
 * @property lastPrice Current market price
 * @property unrealised Unrealized P&L on open positions
 * @property realised Realized P&L on closed positions
 * @property sellValue Net sold quantities value
 * @property tradingSymbol Instrument trading symbol
 * @property closePrice Previous trading day closing price
 * @property buyPrice Mean purchase price
 * @property sellPrice Mean sale price
 * @see <a href="https://upstox.com/developer/api-documentation/get-positions">Get Positions API</a>
 */
data class Position(
    @JsonProperty("exchange")
    val exchange: Exchange?,

    @JsonProperty("multiplier")
    val multiplier: Double?,

    @JsonProperty("value")
    val value: Double?,

    @JsonProperty("pnl")
    val pnl: Double?,

    @JsonProperty("product")
    val product: Product?,

    @JsonProperty("instrument_token")
    val instrumentToken: String?,

    @JsonProperty("average_price")
    val averagePrice: Double?,

    @JsonProperty("buy_value")
    val buyValue: Double?,

    @JsonProperty("overnight_quantity")
    val overnightQuantity: Int?,

    @JsonProperty("day_buy_value")
    val dayBuyValue: Double?,

    @JsonProperty("day_buy_price")
    val dayBuyPrice: Double?,

    @JsonProperty("overnight_buy_amount")
    val overnightBuyAmount: Double?,

    @JsonProperty("overnight_buy_quantity")
    val overnightBuyQuantity: Int?,

    @JsonProperty("day_buy_quantity")
    val dayBuyQuantity: Int?,

    @JsonProperty("day_sell_value")
    val daySellValue: Double?,

    @JsonProperty("day_sell_price")
    val daySellPrice: Double?,

    @JsonProperty("overnight_sell_amount")
    val overnightSellAmount: Double?,

    @JsonProperty("overnight_sell_quantity")
    val overnightSellQuantity: Int?,

    @JsonProperty("day_sell_quantity")
    val daySellQuantity: Int?,

    @JsonProperty("quantity")
    val quantity: Int?,

    @JsonProperty("last_price")
    val lastPrice: Double?,

    @JsonProperty("unrealised")
    val unrealised: Double?,

    @JsonProperty("realised")
    val realised: Double?,

    @JsonProperty("sell_value")
    val sellValue: Double?,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String?,

    @JsonProperty("close_price")
    val closePrice: Double?,

    @JsonProperty("buy_price")
    val buyPrice: Double?,

    @JsonProperty("sell_price")
    val sellPrice: Double?
)

/**
 * Historical candle data response.
 *
 * Contains OHLC (Open, High, Low, Close) candle data.
 *
 * @property candles Array of candle data. Each candle is an array with elements:
 *   [0]: Timestamp (ISO 8601 format), [1]: Open price, [2]: High price,
 *   [3]: Low price, [4]: Close price, [5]: Volume traded, [6]: Open interest (for derivatives)
 * @see <a href="https://upstox.com/developer/api-documentation/v3/get-historical-candle-data">Historical Candle API</a>
 */
data class CandleData(
    val candles: List<List<Any>>?
) {
    /**
     * Parses candle data into strongly-typed Candle objects.
     *
     * @return List of Candle objects
     */
    fun toCandles(): List<Candle> {
        return candles?.mapNotNull { candleArray ->
            // Skip malformed candles with insufficient data
            if (candleArray.size < 6) return@mapNotNull null
            Candle(
                timestamp = candleArray[0] as? String,
                open = (candleArray[1] as? Number)?.toDouble(),
                high = (candleArray[2] as? Number)?.toDouble(),
                low = (candleArray[3] as? Number)?.toDouble(),
                close = (candleArray[4] as? Number)?.toDouble(),
                volume = (candleArray[5] as? Number)?.toLong(),
                openInterest = (candleArray.getOrNull(6) as? Number)?.toLong()
            )
        } ?: emptyList()
    }
}

/**
 * Parsed candle data.
 *
 * Strongly-typed representation of a single OHLC candle.
 *
 * @property timestamp Candle start timestamp in ISO 8601 format
 * @property open Opening price of the candle
 * @property high Highest price during the candle period
 * @property low Lowest price during the candle period
 * @property close Closing price of the candle
 * @property volume Total volume traded during the candle period
 * @property openInterest Open interest (outstanding derivative contracts), only available for derivatives instruments
 */
data class Candle(
    val timestamp: String?,
    val open: Double?,
    val high: Double?,
    val low: Double?,
    val close: Double?,
    val volume: Long?,
    val openInterest: Long?
)

/**
 * Expired instrument (option or future) contract.
 *
 * @property name Contract name
 * @property segment Market segment
 * @property exchange Exchange: NSE, BSE, or MCX
 * @property expiry Expiry date (yyyy-MM-dd)
 * @property instrumentKey Unique instrument identifier for the expired contract
 * @property exchangeToken Exchange-specific token
 * @property tradingSymbol Trading symbol
 * @property tickSize Minimum price movement
 * @property lotSize Size of one lot
 * @property instrumentType Instrument type
 * @property freezeQuantity Maximum quantity that can be frozen
 * @property underlyingKey Instrument key of the underlying
 * @property underlyingType Underlying type
 * @property underlyingSymbol Symbol of underlying
 * @property strikePrice Option strike price (not present for futures)
 * @property minimumLot Minimum lot size
 * @property weekly Whether this was a weekly expiry contract
 * @see <a href="https://upstox.com/developer/api-documentation/get-expired-option-contracts">Expired Option Contracts API</a>
 */
data class ExpiredContract(
    @JsonProperty("name")
    val name: String?,

    @JsonProperty("segment")
    val segment: Segment?,

    @JsonProperty("exchange")
    val exchange: Exchange?,

    @JsonProperty("expiry")
    val expiry: String?,

    @JsonProperty("instrument_key")
    val instrumentKey: String?,

    @JsonProperty("exchange_token")
    val exchangeToken: String?,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String?,

    @JsonProperty("tick_size")
    val tickSize: Double?,

    @JsonProperty("lot_size")
    val lotSize: Int?,

    @JsonProperty("instrument_type")
    val instrumentType: InstrumentType?,

    @JsonProperty("freeze_quantity")
    val freezeQuantity: Int?,

    @JsonProperty("underlying_key")
    val underlyingKey: String?,

    @JsonProperty("underlying_type")
    val underlyingType: UnderlyingType?,

    @JsonProperty("underlying_symbol")
    val underlyingSymbol: String?,

    @JsonProperty("strike_price")
    val strikePrice: Double?,

    @JsonProperty("minimum_lot")
    val minimumLot: Int?,

    @JsonProperty("weekly")
    val weekly: Boolean?
)
