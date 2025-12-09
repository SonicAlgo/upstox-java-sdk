package io.github.sonicalgo.upstox.model.websocket

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.model.enums.*

/**
 * Response from WebSocket authorization endpoints.
 *
 * Contains the one-time use WebSocket URL for establishing a secure connection.
 * The URL includes an embedded authentication code that can only be used once.
 *
 * @property authorizedRedirectUri The WSS URL for WebSocket connection. Contains a single-use
 *                                  authentication code in the query parameters.
 * @see <a href="https://upstox.com/developer/api-documentation/get-market-data-feed-authorize-v3">Market Data Feed Authorize</a>
 * @see <a href="https://upstox.com/developer/api-documentation/get-portfolio-stream-feed-authorize">Portfolio Stream Authorize</a>
 */
data class AuthorizedRedirectResponse(
    @JsonProperty("authorized_redirect_uri")
    val authorizedRedirectUri: String
)

/**
 * Order update received via Portfolio Stream WebSocket.
 *
 * Contains real-time updates for order status changes, executions, and modifications.
 * Subscribe to order updates by including "order" in the update_types query parameter.
 *
 * @property exchange Exchange where the order is placed (e.g., "NSE", "BSE", "MCX")
 * @property product Product type: INTRADAY, DELIVERY, MTF (Margin Trading Facility)
 * @property price Order price for limit orders
 * @property quantity Total order quantity
 * @property status Current order status (e.g., "open", "complete", "rejected", "cancelled")
 * @property tag Optional user-defined tag for order identification
 * @property instrumentToken Unique identifier for the instrument (e.g., "NSE_EQ|INE002A01018")
 * @property placedBy User ID who placed the order
 * @property tradingSymbol Trading symbol of the instrument
 * @property orderType Type of order: MARKET, LIMIT, SL (Stop Loss), SL-M (Stop Loss Market)
 * @property validity Order validity: DAY or IOC (Immediate or Cancel)
 * @property triggerPrice Trigger price for stop loss orders
 * @property disclosedQuantity Quantity disclosed in the market (for iceberg orders)
 * @property transactionType Trade direction: BUY or SELL
 * @property averagePrice Average execution price for filled quantity
 * @property filledQuantity Quantity that has been executed
 * @property pendingQuantity Quantity pending execution
 * @property statusMessage Human-readable status message
 * @property exchangeOrderId Order ID assigned by the exchange
 * @property parentOrderId Parent order ID for child orders (e.g., in bracket orders)
 * @property orderId Unique order identifier assigned by Upstox
 * @property variety Order variety (e.g., "SIMPLE", "AFTER_MARKET_ORDER", "COVER_ORDER")
 * @property orderTimestamp Timestamp when the order was placed
 * @property exchangeTimestamp Timestamp from the exchange
 * @property isAmo Whether this is an After Market Order
 * @property orderRequestId Unique request identifier for tracking
 * @property orderRefId Reference ID for the order
 * @see <a href="https://upstox.com/developer/api-documentation/get-portfolio-stream-feed">Portfolio Stream Feed</a>
 */
data class OrderUpdate(
    @JsonProperty("exchange")
    val exchange: Exchange? = null,

    @JsonProperty("product")
    val product: Product? = null,

    @JsonProperty("price")
    val price: Double? = null,

    @JsonProperty("quantity")
    val quantity: Int? = null,

    @JsonProperty("status")
    val status: OrderStatus? = null,

    @JsonProperty("tag")
    val tag: String? = null,

    @JsonProperty("instrument_token")
    val instrumentToken: String? = null,

    @JsonProperty("placed_by")
    val placedBy: String? = null,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String? = null,

    @JsonProperty("order_type")
    val orderType: OrderType? = null,

    @JsonProperty("validity")
    val validity: Validity? = null,

    @JsonProperty("trigger_price")
    val triggerPrice: Double? = null,

    @JsonProperty("disclosed_quantity")
    val disclosedQuantity: Int? = null,

    @JsonProperty("transaction_type")
    val transactionType: TransactionType? = null,

    @JsonProperty("average_price")
    val averagePrice: Double? = null,

    @JsonProperty("filled_quantity")
    val filledQuantity: Int? = null,

    @JsonProperty("pending_quantity")
    val pendingQuantity: Int? = null,

    @JsonProperty("status_message")
    val statusMessage: String? = null,

    @JsonProperty("exchange_order_id")
    val exchangeOrderId: String? = null,

    @JsonProperty("parent_order_id")
    val parentOrderId: String? = null,

    @JsonProperty("order_id")
    val orderId: String? = null,

    @JsonProperty("variety")
    val variety: OrderVariety? = null,

    @JsonProperty("order_timestamp")
    val orderTimestamp: String? = null,

    @JsonProperty("exchange_timestamp")
    val exchangeTimestamp: String? = null,

    @JsonProperty("is_amo")
    val isAmo: Boolean? = null,

    @JsonProperty("order_request_id")
    val orderRequestId: String? = null,

    @JsonProperty("order_ref_id")
    val orderRefId: String? = null
)

/**
 * Position update received via Portfolio Stream WebSocket.
 *
 * Contains real-time updates for position changes including day trades and overnight positions.
 * Subscribe to position updates by including "position" in the update_types query parameter.
 *
 * @property exchange Exchange where the position is held (e.g., "NSE", "BSE", "MCX")
 * @property multiplier Contract multiplier for derivatives
 * @property value Current position value
 * @property pnl Total profit and loss
 * @property product Product type: INTRADAY, DELIVERY
 * @property instrumentToken Unique identifier for the instrument
 * @property averagePrice Average price of the position
 * @property buyValue Total buy value
 * @property overnightQuantity Quantity held overnight (carried forward from previous day)
 * @property dayBuyValue Total value of day's buy trades
 * @property dayBuyPrice Average buy price for the day
 * @property overnightBuyAmount Overnight buy amount carried forward
 * @property overnightBuyQuantity Overnight buy quantity carried forward
 * @property dayBuyQuantity Total buy quantity for the day
 * @property daySellValue Total value of day's sell trades
 * @property daySellPrice Average sell price for the day
 * @property overnightSellAmount Overnight sell amount carried forward
 * @property overnightSellQuantity Overnight sell quantity carried forward
 * @property daySellQuantity Total sell quantity for the day
 * @property quantity Net position quantity (positive for long, negative for short)
 * @property lastPrice Last traded price of the instrument
 * @property unrealised Unrealized profit/loss on open position
 * @property realised Realized profit/loss from closed trades
 * @property sellValue Total sell value
 * @property tradingSymbol Trading symbol of the instrument
 * @property closePrice Previous day's closing price
 * @property buyPrice Average buy price
 * @property sellPrice Average sell price
 * @see <a href="https://upstox.com/developer/api-documentation/get-portfolio-stream-feed">Portfolio Stream Feed</a>
 */
data class PositionUpdate(
    @JsonProperty("exchange")
    val exchange: Exchange? = null,

    @JsonProperty("multiplier")
    val multiplier: Double? = null,

    @JsonProperty("value")
    val value: Double? = null,

    @JsonProperty("pnl")
    val pnl: Double? = null,

    @JsonProperty("product")
    val product: Product? = null,

    @JsonProperty("instrument_token")
    val instrumentToken: String? = null,

    @JsonProperty("average_price")
    val averagePrice: Double? = null,

    @JsonProperty("buy_value")
    val buyValue: Double? = null,

    @JsonProperty("overnight_quantity")
    val overnightQuantity: Int? = null,

    @JsonProperty("day_buy_value")
    val dayBuyValue: Double? = null,

    @JsonProperty("day_buy_price")
    val dayBuyPrice: Double? = null,

    @JsonProperty("overnight_buy_amount")
    val overnightBuyAmount: Double? = null,

    @JsonProperty("overnight_buy_quantity")
    val overnightBuyQuantity: Int? = null,

    @JsonProperty("day_buy_quantity")
    val dayBuyQuantity: Int? = null,

    @JsonProperty("day_sell_value")
    val daySellValue: Double? = null,

    @JsonProperty("day_sell_price")
    val daySellPrice: Double? = null,

    @JsonProperty("overnight_sell_amount")
    val overnightSellAmount: Double? = null,

    @JsonProperty("overnight_sell_quantity")
    val overnightSellQuantity: Int? = null,

    @JsonProperty("day_sell_quantity")
    val daySellQuantity: Int? = null,

    @JsonProperty("quantity")
    val quantity: Int? = null,

    @JsonProperty("last_price")
    val lastPrice: Double? = null,

    @JsonProperty("unrealised")
    val unrealised: Double? = null,

    @JsonProperty("realised")
    val realised: Double? = null,

    @JsonProperty("sell_value")
    val sellValue: Double? = null,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String? = null,

    @JsonProperty("close_price")
    val closePrice: Double? = null,

    @JsonProperty("buy_price")
    val buyPrice: Double? = null,

    @JsonProperty("sell_price")
    val sellPrice: Double? = null
)

/**
 * Holding update received via Portfolio Stream WebSocket.
 *
 * Contains real-time updates for holdings including quantity changes and collateral updates.
 * Subscribe to holding updates by including "holding" in the update_types query parameter.
 *
 * @property isin International Securities Identification Number
 * @property cncUsedQuantity Quantity used in CNC (Cash and Carry) orders
 * @property collateralType Type of collateral (e.g., "haircut")
 * @property companyName Full name of the company
 * @property haircut Haircut percentage applied for margin calculation
 * @property product Product type for the holding
 * @property quantity Total quantity held
 * @property tradingSymbol Trading symbol of the instrument
 * @property lastPrice Current market price of the instrument
 * @property closePrice Previous day's closing price
 * @property pnl Profit/loss on the holding
 * @property dayChange Absolute price change for the day
 * @property dayChangePercentage Percentage price change for the day
 * @property instrumentToken Unique identifier for the instrument
 * @property averagePrice Average purchase price of the holding
 * @property collateralQuantity Quantity pledged as collateral
 * @property collateralUpdateQuantity Change in collateral quantity
 * @property t1Quantity T+1 quantity (shares bought today, available tomorrow)
 * @property exchange Exchange where the holding is listed
 * @see <a href="https://upstox.com/developer/api-documentation/get-portfolio-stream-feed">Portfolio Stream Feed</a>
 */
data class HoldingUpdate(
    @JsonProperty("isin")
    val isin: String? = null,

    @JsonProperty("cnc_used_quantity")
    val cncUsedQuantity: Int? = null,

    @JsonProperty("collateral_type")
    val collateralType: String? = null,

    @JsonProperty("company_name")
    val companyName: String? = null,

    @JsonProperty("haircut")
    val haircut: Double? = null,

    @JsonProperty("product")
    val product: Product? = null,

    @JsonProperty("quantity")
    val quantity: Int? = null,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String? = null,

    @JsonProperty("last_price")
    val lastPrice: Double? = null,

    @JsonProperty("close_price")
    val closePrice: Double? = null,

    @JsonProperty("pnl")
    val pnl: Double? = null,

    @JsonProperty("day_change")
    val dayChange: Double? = null,

    @JsonProperty("day_change_percentage")
    val dayChangePercentage: Double? = null,

    @JsonProperty("instrument_token")
    val instrumentToken: String? = null,

    @JsonProperty("average_price")
    val averagePrice: Double? = null,

    @JsonProperty("collateral_quantity")
    val collateralQuantity: Int? = null,

    @JsonProperty("collateral_update_quantity")
    val collateralUpdateQuantity: Int? = null,

    @JsonProperty("t1_quantity")
    val t1Quantity: Int? = null,

    @JsonProperty("exchange")
    val exchange: Exchange? = null
)

/**
 * GTT (Good Till Triggered) Order update received via Portfolio Stream WebSocket.
 *
 * Contains real-time updates for GTT order status changes.
 * Subscribe to GTT updates by including "gtt_order" in the update_types query parameter.
 *
 * @property type GTT order type: SINGLE or MULTIPLE
 * @property exchange Exchange where the GTT order will be placed
 * @property quantity Order quantity
 * @property product Product type: INTRADAY or DELIVERY
 * @property instrumentToken Unique identifier for the instrument
 * @property tradingSymbol Trading symbol of the instrument
 * @property gttOrderId Unique GTT order identifier
 * @property expiresAt Timestamp when the GTT order expires (Unix timestamp in milliseconds)
 * @property createdAt Timestamp when the GTT order was created (Unix timestamp in milliseconds)
 * @property rules List of rules/legs in this GTT order
 * @see <a href="https://upstox.com/developer/api-documentation/get-portfolio-stream-feed">Portfolio Stream Feed</a>
 */
data class GttOrderUpdate(
    @JsonProperty("type")
    val type: GttType? = null,

    @JsonProperty("exchange")
    val exchange: Segment? = null,

    @JsonProperty("quantity")
    val quantity: Int? = null,

    @JsonProperty("product")
    val product: Product? = null,

    @JsonProperty("instrument_token")
    val instrumentToken: String? = null,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String? = null,

    @JsonProperty("gtt_order_id")
    val gttOrderId: String? = null,

    @JsonProperty("expires_at")
    val expiresAt: Long? = null,

    @JsonProperty("created_at")
    val createdAt: Long? = null,

    @JsonProperty("rules")
    val rules: List<GttRuleUpdate>? = null
)

/**
 * GTT Rule update within a GTT Order update.
 *
 * Represents a single rule/leg within a GTT order. Each GTT order can have
 * multiple rules (e.g., target and stop-loss in a bracket GTT).
 *
 * @property strategy Rule strategy: ENTRY, TARGET, or STOP_LOSS
 * @property status Rule status: PENDING, COMPLETED, FAILED, or CANCELLED
 * @property triggerType Trigger condition: BELOW, ABOVE, or IMMEDIATE
 * @property triggerPrice Price at which this rule triggers
 * @property transactionType Trade direction when triggered: BUY or SELL
 * @property message Status message or error description
 * @property orderId Order ID if the rule has been triggered and order placed
 * @property trailingGap Trailing stop gap for trailing stop-loss orders
 * @see <a href="https://upstox.com/developer/api-documentation/get-portfolio-stream-feed">Portfolio Stream Feed</a>
 */
data class GttRuleUpdate(
    @JsonProperty("strategy")
    val strategy: GttStrategy? = null,

    @JsonProperty("status")
    val status: GttRuleStatus? = null,

    @JsonProperty("trigger_type")
    val triggerType: GttTriggerType? = null,

    @JsonProperty("trigger_price")
    val triggerPrice: Double? = null,

    @JsonProperty("transaction_type")
    val transactionType: TransactionType? = null,

    @JsonProperty("message")
    val message: String? = null,

    @JsonProperty("order_id")
    val orderId: String? = null,

    @JsonProperty("trailing_gap")
    val trailingGap: Double? = null
)

/**
 * Wrapper for portfolio stream messages.
 *
 * Internal class used to deserialize the raw JSON messages from the Portfolio Stream WebSocket.
 *
 * @property type The type of update: "order", "position", "holding", or "gtt_order"
 * @property data The actual update payload
 */
internal data class PortfolioStreamMessage(
    @JsonProperty("type")
    val type: String? = null,

    @JsonProperty("data")
    val data: PortfolioStreamData? = null
)

/**
 * Data payload in portfolio stream messages.
 *
 * Internal class containing the typed update data. Only one field will be populated
 * based on the message type.
 *
 * @property order Order update data (when type is "order")
 * @property position Position update data (when type is "position")
 * @property holding Holding update data (when type is "holding")
 * @property gttOrder GTT order update data (when type is "gtt_order")
 */
internal data class PortfolioStreamData(
    @JsonProperty("order")
    val order: OrderUpdate? = null,

    @JsonProperty("position")
    val position: PositionUpdate? = null,

    @JsonProperty("holding")
    val holding: HoldingUpdate? = null,

    @JsonProperty("gtt_order")
    val gttOrder: GttOrderUpdate? = null
)
