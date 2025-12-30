package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V3
import io.github.sonicalgo.upstox.common.UpstoxResponseWithMetadata
import io.github.sonicalgo.upstox.common.GttStrategy
import io.github.sonicalgo.upstox.common.GttTriggerType
import io.github.sonicalgo.upstox.common.GttType
import io.github.sonicalgo.upstox.common.Product
import io.github.sonicalgo.upstox.common.Segment
import io.github.sonicalgo.upstox.common.TransactionType

/** Gets details of GTT orders. */
@JvmSynthetic
internal fun executeGetGttOrders(
    apiClient: ApiClient,
    gttOrderId: String? = null
): GetGttOrdersResponse {
    val queryParams = mutableMapOf<String, String?>()
    gttOrderId?.let { queryParams["gtt_order_id"] = it }

    val response: UpstoxResponseWithMetadata<List<GttOrder>> = apiClient.get(
        endpoint = "/order/gtt",
        queryParams = queryParams,
        overrideBaseUrl = BASE_URL_V3
    )
    return GetGttOrdersResponse(
        orders = response.data ?: emptyList(),
        latency = response.metadata?.latency
    )
}

/** GTT rule status values. */
enum class GttRuleStatus {
    /** Rule is scheduled and waiting for trigger */
    SCHEDULED,

    /** Rule has been triggered */
    TRIGGERED,

    /** Rule execution failed */
    FAILED,

    /** Rule was cancelled */
    CANCELLED,

    /** Rule has expired */
    EXPIRED,

    /** Rule is open */
    OPEN,

    /** Rule has been completed */
    COMPLETED,

    /** Rule is pending */
    PENDING,

    /** Rule is inactive */
    INACTIVE
}

/**
 * Response from Get GTT Order Details API.
 *
 * @property orders List of GTT orders
 * @property latency Processing time in milliseconds
 * @see <a href="https://upstox.com/developer/api-documentation/get-gtt-order-details">Get GTT Order Details API</a>
 */
data class GetGttOrdersResponse(
    @JsonProperty("orders")
    val orders: List<GttOrder>?,

    @JsonProperty("latency")
    val latency: Int?
)

/**
 * GTT order details.
 *
 * @property type GTT order type: SINGLE or MULTIPLE
 * @property exchange Exchange code (NSE_EQ, BSE_EQ, etc.)
 * @property quantity Order quantity
 * @property product Product type
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
    val rules: List<GttRuleDetails>?
)

/**
 * GTT order rule details.
 *
 * @property strategy Rule strategy: ENTRY, TARGET, or STOP_LOSS
 * @property status Rule status
 * @property triggerType Trigger type: BELOW, ABOVE, or IMMEDIATE
 * @property triggerPrice Price at which the order triggers
 * @property transactionType Transaction type: BUY or SELL
 * @property message Error reason if rule execution failed
 * @property orderId Generated order ID after rule execution
 * @property trailingGap Gap for trailing stop-loss
 */
data class GttRuleDetails(
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