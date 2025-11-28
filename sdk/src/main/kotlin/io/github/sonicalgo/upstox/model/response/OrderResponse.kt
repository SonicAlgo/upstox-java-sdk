package io.github.sonicalgo.upstox.model.response

import com.google.gson.annotations.SerializedName

/**
 * Response from Place Order API.
 *
 * @property orderIds List of order IDs (multiple when slice is enabled)
 * @see <a href="https://upstox.com/developer/api-documentation/v3/place-order">Place Order V3 API</a>
 */
data class PlaceOrderResponse(
    @SerializedName("order_ids")
    val orderIds: List<String>
)

/**
 * Response from Modify Order API.
 *
 * @property orderId The modified order ID
 * @see <a href="https://upstox.com/developer/api-documentation/v3/modify-order">Modify Order V3 API</a>
 */
data class ModifyOrderResponse(
    @SerializedName("order_id")
    val orderId: String
)

/**
 * Response from Cancel Order API.
 *
 * @property orderId The cancelled order ID
 * @see <a href="https://upstox.com/developer/api-documentation/v3/cancel-order">Cancel Order V3 API</a>
 */
data class CancelOrderResponse(
    @SerializedName("order_id")
    val orderId: String
)

/**
 * Response from Place Multi Order API.
 *
 * @property correlationId Correlation ID provided in the request
 * @property orderId Generated order ID for successful orders
 */
data class MultiOrderPlaceResponse(
    @SerializedName("correlation_id")
    val correlationId: String,
    @SerializedName("order_id")
    val orderId: String? = null
)

/**
 * Response from Cancel Multi Order API.
 *
 * @property orderIds List of successfully cancelled order IDs
 */
data class MultiOrderCancelResponse(
    @SerializedName("order_ids")
    val orderIds: List<String>
)

/**
 * Response from Exit All Positions API.
 *
 * @property orderIds List of order IDs created for exiting positions
 */
data class ExitPositionsResponse(
    @SerializedName("order_ids")
    val orderIds: List<String>
)

/**
 * Complete order details.
 *
 * Contains all information about an order including status and execution details.
 *
 * @property exchange Exchange identifier (NSE, BSE, etc.)
 * @property product Product type: I, D, CO, MTF
 * @property price Order placement price
 * @property quantity Order quantity
 * @property status Current order status
 * @property tag Custom order tag if provided
 * @property instrumentToken Instrument key identifier
 * @property placedBy User identifier who placed the order
 * @property tradingSymbol Trading symbol of the instrument
 * @property orderType Order type: MARKET, LIMIT, SL, SL-M
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
    val exchange: String,
    val product: String,
    val price: Double,
    val quantity: Int,
    val status: String,
    val tag: String? = null,
    @SerializedName("instrument_token")
    val instrumentToken: String,
    @SerializedName("placed_by")
    val placedBy: String? = null,
    @SerializedName("trading_symbol")
    val tradingSymbol: String,
    @SerializedName("order_type")
    val orderType: String,
    val validity: String,
    @SerializedName("trigger_price")
    val triggerPrice: Double,
    @SerializedName("disclosed_quantity")
    val disclosedQuantity: Int,
    @SerializedName("transaction_type")
    val transactionType: String,
    @SerializedName("average_price")
    val averagePrice: Double,
    @SerializedName("filled_quantity")
    val filledQuantity: Int,
    @SerializedName("pending_quantity")
    val pendingQuantity: Int,
    @SerializedName("status_message")
    val statusMessage: String? = null,
    @SerializedName("status_message_raw")
    val statusMessageRaw: String? = null,
    @SerializedName("exchange_order_id")
    val exchangeOrderId: String? = null,
    @SerializedName("parent_order_id")
    val parentOrderId: String? = null,
    @SerializedName("order_id")
    val orderId: String,
    val variety: String,
    @SerializedName("order_timestamp")
    val orderTimestamp: String? = null,
    @SerializedName("exchange_timestamp")
    val exchangeTimestamp: String? = null,
    @SerializedName("is_amo")
    val isAmo: Boolean,
    @SerializedName("order_request_id")
    val orderRequestId: String? = null,
    @SerializedName("order_ref_id")
    val orderRefId: String? = null
)

/**
 * Trade execution details.
 *
 * Contains information about executed trades.
 *
 * @property exchange Exchange identifier
 * @property product Product type: I, D, CO, MTF
 * @property tradingSymbol Trading symbol
 * @property instrumentToken Instrument key identifier
 * @property orderType Order type: MARKET, LIMIT, SL, SL-M
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
    val exchange: String,
    val product: String,
    @SerializedName("trading_symbol")
    val tradingSymbol: String,
    @SerializedName("instrument_token")
    val instrumentToken: String,
    @SerializedName("order_type")
    val orderType: String,
    @SerializedName("transaction_type")
    val transactionType: String,
    val quantity: Int,
    @SerializedName("exchange_order_id")
    val exchangeOrderId: String,
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("exchange_timestamp")
    val exchangeTimestamp: String,
    @SerializedName("average_price")
    val averagePrice: Double,
    @SerializedName("trade_id")
    val tradeId: String,
    @SerializedName("order_ref_id")
    val orderRefId: String? = null,
    @SerializedName("order_timestamp")
    val orderTimestamp: String? = null
)

/**
 * Historical trade record.
 *
 * Contains historical trade information for reporting.
 *
 * @property exchange Exchange identifier
 * @property segment Market segment: EQ, FO, CD, COM, MF
 * @property optionType Option type: CE or PE (FO/CD segments only)
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
    val exchange: String,
    val segment: String,
    @SerializedName("option_type")
    val optionType: String? = null,
    val quantity: Int,
    val amount: Double,
    @SerializedName("trade_id")
    val tradeId: String,
    @SerializedName("trade_date")
    val tradeDate: String,
    @SerializedName("transaction_type")
    val transactionType: String,
    @SerializedName("scrip_name")
    val scripName: String,
    @SerializedName("strike_price")
    val strikePrice: String? = null,
    val expiry: String? = null,
    val price: Double,
    val isin: String? = null,
    val symbol: String? = null,
    @SerializedName("instrument_token")
    val instrumentToken: String? = null
)
