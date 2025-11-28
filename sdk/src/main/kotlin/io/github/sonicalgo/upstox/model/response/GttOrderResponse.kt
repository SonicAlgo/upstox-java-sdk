package io.github.sonicalgo.upstox.model.response

import com.google.gson.annotations.SerializedName

/**
 * Response from Place/Modify/Cancel GTT Order API.
 *
 * @property gttOrderIds List of GTT order IDs created or affected
 * @see <a href="https://upstox.com/developer/api-documentation/place-gtt-order">Place GTT Order API</a>
 */
data class GttOrderResponse(
    @SerializedName("gtt_order_ids")
    val gttOrderIds: List<String>
)

/**
 * GTT order details.
 *
 * Contains complete information about a GTT order.
 *
 * @property type GTT order type: SINGLE or MULTIPLE
 * @property exchange Exchange code (NSE_EQ, BSE_EQ, etc.)
 * @property quantity Order quantity
 * @property product Product type: I, D, MTF
 * @property instrumentToken Unique instrument identifier
 * @property tradingSymbol Instrument trading symbol
 * @property gttOrderId Unique GTT order identifier (starts with "GTT-")
 * @property expiresAt Expiration timestamp in microseconds
 * @property createdAt Creation timestamp in microseconds
 * @property rules List of order execution rules
 * @see <a href="https://upstox.com/developer/api-documentation/get-gtt-order-details">Get GTT Order Details API</a>
 */
data class GttOrder(
    val type: String,
    val exchange: String,
    val quantity: Int,
    val product: String,
    @SerializedName("instrument_token")
    val instrumentToken: String,
    @SerializedName("trading_symbol")
    val tradingSymbol: String,
    @SerializedName("gtt_order_id")
    val gttOrderId: String,
    @SerializedName("expires_at")
    val expiresAt: Long,
    @SerializedName("created_at")
    val createdAt: Long,
    val rules: List<GttRuleDetails>
)

/**
 * GTT order rule details.
 *
 * Contains information about a specific rule within a GTT order.
 *
 * @property strategy Rule strategy: ENTRY, TARGET, or STOPLOSS
 * @property status Rule status: SCHEDULED, TRIGGERED, FAILED, CANCELLED, EXPIRED
 * @property triggerType Trigger type: BELOW, ABOVE, or IMMEDIATE
 * @property triggerPrice Price at which the order triggers
 * @property transactionType Transaction type: BUY or SELL
 * @property message Error reason if rule execution failed
 * @property orderId Generated order ID after rule execution
 * @property trailingGap Gap for trailing stop-loss (STOPLOSS strategy only)
 */
data class GttRuleDetails(
    val strategy: String,
    val status: String,
    @SerializedName("trigger_type")
    val triggerType: String,
    @SerializedName("trigger_price")
    val triggerPrice: Double,
    @SerializedName("transaction_type")
    val transactionType: String,
    val message: String? = null,
    @SerializedName("order_id")
    val orderId: String? = null,
    @SerializedName("trailing_gap")
    val trailingGap: Double? = null
)
