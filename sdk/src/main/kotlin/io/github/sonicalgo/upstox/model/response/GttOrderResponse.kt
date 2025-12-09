package io.github.sonicalgo.upstox.model.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.model.enums.*

/**
 * Response from Place/Modify/Cancel GTT Order API.
 *
 * @property gttOrderIds List of GTT order IDs created or affected
 * @see <a href="https://upstox.com/developer/api-documentation/place-gtt-order">Place GTT Order API</a>
 */
data class GttOrderResponse(
    @JsonProperty("gtt_order_ids")
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
 * @property product Product type: INTRADAY, DELIVERY, MTF
 * @property instrumentToken Unique instrument identifier
 * @property tradingSymbol Instrument trading symbol
 * @property gttOrderId Unique GTT order identifier (starts with "GTT-")
 * @property expiresAt Expiration timestamp in microseconds
 * @property createdAt Creation timestamp in microseconds
 * @property rules List of order execution rules
 * @see <a href="https://upstox.com/developer/api-documentation/get-gtt-order-details">Get GTT Order Details API</a>
 */
data class GttOrder(
    @JsonProperty("type")
    val type: GttType,

    @JsonProperty("exchange")
    val exchange: Segment,

    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("product")
    val product: Product,

    @JsonProperty("instrument_token")
    val instrumentToken: String,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String,

    @JsonProperty("gtt_order_id")
    val gttOrderId: String,

    @JsonProperty("expires_at")
    val expiresAt: Long,

    @JsonProperty("created_at")
    val createdAt: Long,

    @JsonProperty("rules")
    val rules: List<GttRuleDetails>
)

/**
 * GTT order rule details.
 *
 * Contains information about a specific rule within a GTT order.
 *
 * @property strategy Rule strategy: ENTRY, TARGET, or STOP_LOSS
 * @property status Rule status: SCHEDULED, TRIGGERED, FAILED, CANCELLED, EXPIRED
 * @property triggerType Trigger type: BELOW, ABOVE, or IMMEDIATE
 * @property triggerPrice Price at which the order triggers
 * @property transactionType Transaction type: BUY or SELL
 * @property message Error reason if rule execution failed
 * @property orderId Generated order ID after rule execution
 * @property trailingGap Gap for trailing stop-loss (STOP_LOSS strategy only)
 */
data class GttRuleDetails(
    @JsonProperty("strategy")
    val strategy: GttStrategy,

    @JsonProperty("status")
    val status: GttRuleStatus,

    @JsonProperty("trigger_type")
    val triggerType: GttTriggerType,

    @JsonProperty("trigger_price")
    val triggerPrice: Double,

    @JsonProperty("transaction_type")
    val transactionType: TransactionType,

    @JsonProperty("message")
    val message: String? = null,

    @JsonProperty("order_id")
    val orderId: String? = null,

    @JsonProperty("trailing_gap")
    val trailingGap: Double? = null
)
