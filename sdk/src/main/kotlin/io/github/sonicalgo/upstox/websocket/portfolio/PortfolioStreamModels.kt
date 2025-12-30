package io.github.sonicalgo.upstox.websocket.portfolio

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.common.*
import io.github.sonicalgo.upstox.usecase.GttRuleStatus

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
    val authorizedRedirectUri: String?
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
    val isin: String?,

    @JsonProperty("cnc_used_quantity")
    val cncUsedQuantity: Int?,

    @JsonProperty("collateral_type")
    val collateralType: String?,

    @JsonProperty("company_name")
    val companyName: String?,

    @JsonProperty("haircut")
    val haircut: Double?,

    @JsonProperty("product")
    val product: Product?,

    @JsonProperty("quantity")
    val quantity: Int?,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String?,

    @JsonProperty("last_price")
    val lastPrice: Double?,

    @JsonProperty("close_price")
    val closePrice: Double?,

    @JsonProperty("pnl")
    val pnl: Double?,

    @JsonProperty("day_change")
    val dayChange: Double?,

    @JsonProperty("day_change_percentage")
    val dayChangePercentage: Double?,

    @JsonProperty("instrument_token")
    val instrumentToken: String?,

    @JsonProperty("average_price")
    val averagePrice: Double?,

    @JsonProperty("collateral_quantity")
    val collateralQuantity: Int?,

    @JsonProperty("collateral_update_quantity")
    val collateralUpdateQuantity: Int?,

    @JsonProperty("t1_quantity")
    val t1Quantity: Int?,

    @JsonProperty("exchange")
    val exchange: Exchange?
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
    val type: GttType?,

    @JsonProperty("exchange")
    val exchange: Segment?,

    @JsonProperty("quantity")
    val quantity: Int?,

    @JsonProperty("product")
    val product: Product?,

    @JsonProperty("instrument_token")
    val instrumentToken: String?,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String?,

    @JsonProperty("gtt_order_id")
    val gttOrderId: String?,

    @JsonProperty("expires_at")
    val expiresAt: Long?,

    @JsonProperty("created_at")
    val createdAt: Long?,

    @JsonProperty("rules")
    val rules: List<GttRuleUpdate>?
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
    val strategy: GttStrategy?,

    @JsonProperty("status")
    val status: GttRuleStatus?,

    @JsonProperty("trigger_type")
    val triggerType: GttTriggerType?,

    @JsonProperty("trigger_price")
    val triggerPrice: Double?,

    @JsonProperty("transaction_type")
    val transactionType: TransactionType?,

    @JsonProperty("message")
    val message: String?,

    @JsonProperty("order_id")
    val orderId: String?,

    @JsonProperty("trailing_gap")
    val trailingGap: Double?
)

/**
 * Internal class to detect the update type from WebSocket messages.
 *
 * The Portfolio Stream sends flat JSON messages with an "update_type" field
 * at the root level (e.g., "order", "position", "holding", "gtt_order").
 * This class is used to first detect the type, then deserialize the full
 * message into the appropriate update class.
 */
internal data class PortfolioStreamTypeDetector(
    @JsonProperty("update_type")
    val updateType: String?
)
