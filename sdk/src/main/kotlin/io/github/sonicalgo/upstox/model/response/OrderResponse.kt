package io.github.sonicalgo.upstox.model.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.model.enums.*

/**
 * Response from Place Order API.
 *
 * @property orderIds List of order IDs (multiple when slice is enabled)
 * @see <a href="https://upstox.com/developer/api-documentation/v3/place-order">Place Order V3 API</a>
 */
data class PlaceOrderResponse(
    @JsonProperty("order_ids")
    val orderIds: List<String>
)

/**
 * Response from Modify Order API.
 *
 * @property orderId The modified order ID
 * @see <a href="https://upstox.com/developer/api-documentation/v3/modify-order">Modify Order V3 API</a>
 */
data class ModifyOrderResponse(
    @JsonProperty("order_id")
    val orderId: String
)

/**
 * Response from Cancel Order API.
 *
 * @property orderId The cancelled order ID
 * @see <a href="https://upstox.com/developer/api-documentation/v3/cancel-order">Cancel Order V3 API</a>
 */
data class CancelOrderResponse(
    @JsonProperty("order_id")
    val orderId: String
)

/**
 * Response from Place Multi Order API.
 *
 * @property correlationId Correlation ID provided in the request
 * @property orderId Generated order ID for successful orders
 */
data class MultiOrderPlaceResponse(
    @JsonProperty("correlation_id")
    val correlationId: String,

    @JsonProperty("order_id")
    val orderId: String? = null
)

/**
 * Response from Cancel Multi Order API.
 *
 * @property orderIds List of successfully cancelled order IDs
 */
data class MultiOrderCancelResponse(
    @JsonProperty("order_ids")
    val orderIds: List<String>
)

/**
 * Response from Exit All Positions API.
 *
 * @property orderIds List of order IDs created for exiting positions
 */
data class ExitPositionsResponse(
    @JsonProperty("order_ids")
    val orderIds: List<String>
)

/**
 * Complete order details.
 *
 * Contains all information about an order including status and execution details.
 *
 * @property exchange Exchange identifier (NSE, BSE, etc.)
 * @property product Product type: INTRADAY, DELIVERY, COVER_ORDER, MTF
 * @property price Order placement price
 * @property quantity Order quantity
 * @property status Current order status
 * @property tag Custom order tag if provided
 * @property instrumentToken Instrument key identifier
 * @property placedBy User identifier who placed the order
 * @property tradingSymbol Trading symbol of the instrument
 * @property orderType Order type: MARKET, LIMIT, SL, SL_M
 * @property validity Order validity: DAY or IOC
 * @property triggerPrice Trigger price for stop loss orders
 * @property disclosedQuantity Quantity disclosed in market depth
 * @property transactionType Transaction type: BUY or SELL
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
    val exchange: Exchange,

    @JsonProperty("product")
    val product: Product,

    @JsonProperty("price")
    val price: Double,

    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("status")
    val status: OrderStatus,

    @JsonProperty("tag")
    val tag: String? = null,

    @JsonProperty("instrument_token")
    val instrumentToken: String,

    @JsonProperty("placed_by")
    val placedBy: String? = null,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String,

    @JsonProperty("order_type")
    val orderType: OrderType,

    @JsonProperty("validity")
    val validity: Validity,

    @JsonProperty("trigger_price")
    val triggerPrice: Double,

    @JsonProperty("disclosed_quantity")
    val disclosedQuantity: Int,

    @JsonProperty("transaction_type")
    val transactionType: TransactionType,

    @JsonProperty("average_price")
    val averagePrice: Double,

    @JsonProperty("filled_quantity")
    val filledQuantity: Int,

    @JsonProperty("pending_quantity")
    val pendingQuantity: Int,

    @JsonProperty("status_message")
    val statusMessage: String? = null,

    @JsonProperty("status_message_raw")
    val statusMessageRaw: String? = null,

    @JsonProperty("exchange_order_id")
    val exchangeOrderId: String? = null,

    @JsonProperty("parent_order_id")
    val parentOrderId: String? = null,

    @JsonProperty("order_id")
    val orderId: String,

    @JsonProperty("variety")
    val variety: OrderVariety,

    @JsonProperty("order_timestamp")
    val orderTimestamp: String? = null,

    @JsonProperty("exchange_timestamp")
    val exchangeTimestamp: String? = null,

    @JsonProperty("is_amo")
    val isAmo: Boolean,

    @JsonProperty("order_request_id")
    val orderRequestId: String? = null,

    @JsonProperty("order_ref_id")
    val orderRefId: String? = null
)

/**
 * Trade execution details.
 *
 * Contains information about executed trades.
 *
 * @property exchange Exchange identifier
 * @property product Product type: INTRADAY, DELIVERY, COVER_ORDER, MTF
 * @property tradingSymbol Trading symbol
 * @property instrumentToken Instrument key identifier
 * @property orderType Order type: MARKET, LIMIT, SL, SL_M
 * @property transactionType Transaction type: BUY or SELL
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
    val exchange: Exchange,

    @JsonProperty("product")
    val product: Product,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String,

    @JsonProperty("instrument_token")
    val instrumentToken: String,

    @JsonProperty("order_type")
    val orderType: OrderType,

    @JsonProperty("transaction_type")
    val transactionType: TransactionType,

    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("exchange_order_id")
    val exchangeOrderId: String,

    @JsonProperty("order_id")
    val orderId: String,

    @JsonProperty("exchange_timestamp")
    val exchangeTimestamp: String,

    @JsonProperty("average_price")
    val averagePrice: Double,

    @JsonProperty("trade_id")
    val tradeId: String,

    @JsonProperty("order_ref_id")
    val orderRefId: String? = null,

    @JsonProperty("order_timestamp")
    val orderTimestamp: String? = null
)

/**
 * Historical trade record.
 *
 * Contains historical trade information for reporting.
 *
 * @property exchange Exchange identifier
 * @property segment Market segment: EQUITY, FUTURES_OPTIONS, CURRENCY_DERIVATIVES, COMMODITY, MUTUAL_FUNDS
 * @property optionType Option type: CALL_OPTION or PUT_OPTION (FO/CD segments only)
 * @property quantity Order quantity
 * @property amount Total transaction amount
 * @property tradeId Exchange-generated trade identifier
 * @property tradeDate Transaction date in YYYY-mm-dd format
 * @property transactionType Transaction type: BUY or SELL
 * @property scripName Security name
 * @property strikePrice Option strike price
 * @property expiry Derivative expiry date in YYYY-mm-dd format
 * @property price Per-unit execution price
 * @property isin Standard ISIN (EQ/MF segments)
 * @property symbol Trading symbol (EQ/FO segments)
 * @property instrumentToken Instrument key (EQ/MF segments)
 * @see <a href="https://upstox.com/developer/api-documentation/get-historical-trades">Get Historical Trades API</a>
 */
data class HistoricalTrade(
    @JsonProperty("exchange")
    val exchange: Exchange,

    @JsonProperty("segment")
    val segment: TradeSegment,

    @JsonProperty("option_type")
    val optionType: OptionType? = null,

    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("amount")
    val amount: Double,

    @JsonProperty("trade_id")
    val tradeId: String,

    @JsonProperty("trade_date")
    val tradeDate: String,

    @JsonProperty("transaction_type")
    val transactionType: TransactionType,

    @JsonProperty("scrip_name")
    val scripName: String,

    @JsonProperty("strike_price")
    val strikePrice: String? = null,

    @JsonProperty("expiry")
    val expiry: String? = null,

    @JsonProperty("price")
    val price: Double,

    @JsonProperty("isin")
    val isin: String? = null,

    @JsonProperty("symbol")
    val symbol: String? = null,

    @JsonProperty("instrument_token")
    val instrumentToken: String? = null
)
