package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V3
import io.github.sonicalgo.upstox.common.UpstoxResponseWithMetadata
import io.github.sonicalgo.upstox.common.*

/** Places a GTT (Good Till Triggered) order. */
@JvmSynthetic
internal fun executePlaceGttOrder(
    apiClient: ApiClient,
    params: PlaceGttOrderParams
): GttOrderResponse {
    val response: UpstoxResponseWithMetadata<GttOrderData> = apiClient.post(
        endpoint = "/order/gtt/place",
        body = params,
        overrideBaseUrl = BASE_URL_V3
    )
    val data = response.dataOrThrow()
    return GttOrderResponse(
        gttOrderIds = data.gttOrderIds,
        latency = response.metadata?.latency
    )
}

/**
 * Parameters for placing a GTT order.
 *
 * @property type GTT type: SINGLE or MULTIPLE
 * @property quantity Order quantity
 * @property product Product type: INTRADAY or DELIVERY
 * @property instrumentToken Instrument key
 * @property transactionType Transaction type: BUY or SELL
 * @property rules List of GTT rules
 */
@GenerateBuilder
data class PlaceGttOrderParams(
    @JsonProperty("type")
    val type: GttType,

    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("product")
    val product: Product,

    @JsonProperty("instrument_token")
    val instrumentToken: String,

    @JsonProperty("transaction_type")
    val transactionType: TransactionType,

    @JsonProperty("rules")
    val rules: List<GttRuleParams>
)

/**
 * GTT rule definition.
 *
 * @property strategy Rule strategy: ENTRY, TARGET, or STOP_LOSS
 * @property triggerType Trigger condition: ABOVE, BELOW, or IMMEDIATE
 * @property triggerPrice Price at which to trigger
 * @property limitPrice Limit price for the triggered order
 * @property trailingGap Gap for trailing stop-loss
 */
@GenerateBuilder
data class GttRuleParams(
    @JsonProperty("strategy")
    val strategy: GttStrategy,

    @JsonProperty("trigger_type")
    val triggerType: GttTriggerType,

    @JsonProperty("trigger_price")
    val triggerPrice: Double,

    @JsonProperty("limit_price")
    val limitPrice: Double? = null,

    @JsonProperty("trailing_gap")
    val trailingGap: Double? = null
)

/**
 * Data portion of GTT order response.
 *
 * @property gttOrderIds List of GTT order IDs created or affected
 */
data class GttOrderData(
    @JsonProperty("gtt_order_ids")
    val gttOrderIds: List<String>?
)

/**
 * Response from Place/Modify/Cancel GTT Order API.
 *
 * @property gttOrderIds List of GTT order IDs created or affected
 * @property latency Processing time in milliseconds
 * @see <a href="https://upstox.com/developer/api-documentation/place-gtt-order">Place GTT Order API</a>
 */
data class GttOrderResponse(
    @JsonProperty("gtt_order_ids")
    val gttOrderIds: List<String>?,

    @JsonProperty("latency")
    val latency: Int?
)