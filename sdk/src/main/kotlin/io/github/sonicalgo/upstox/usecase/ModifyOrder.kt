package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConfig
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_HFT
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_SANDBOX
import io.github.sonicalgo.upstox.common.UpstoxResponseWithMetadata
import io.github.sonicalgo.upstox.common.OrderType
import io.github.sonicalgo.upstox.common.Validity

/** Modifies an existing order. */
@JvmSynthetic
internal fun executeModifyOrder(
    apiClient: ApiClient,
    config: UpstoxConfig,
    params: ModifyOrderParams
): ModifyOrderResponse {
    val baseUrl = if (config.sandboxEnabled) BASE_URL_SANDBOX else BASE_URL_HFT
    val response: UpstoxResponseWithMetadata<ModifyOrderData> = apiClient.put(
        endpoint = "/order/modify",
        body = params,
        overrideBaseUrl = baseUrl
    )
    val data = response.dataOrThrow()
    return ModifyOrderResponse(
        orderId = data.orderId,
        latency = response.metadata?.latency
    )
}

/**
 * Parameters for modifying an existing order (V3 API).
 *
 * @property orderId The order ID to modify
 * @property validity Order validity: DAY or IOC
 * @property orderType Order type: MARKET, LIMIT, SL, or SL_M
 * @property price New order price
 * @property triggerPrice New trigger price for stop loss orders
 * @property quantity New order quantity
 * @property disclosedQuantity New disclosed quantity (min 10% of quantity)
 */
@GenerateBuilder
data class ModifyOrderParams(
    @JsonProperty("order_id")
    val orderId: String,

    @JsonProperty("validity")
    val validity: Validity,

    @JsonProperty("order_type")
    val orderType: OrderType,

    @JsonProperty("price")
    val price: Double,

    @JsonProperty("trigger_price")
    val triggerPrice: Double = 0.0,

    @JsonProperty("quantity")
    val quantity: Int? = null,

    @JsonProperty("disclosed_quantity")
    val disclosedQuantity: Int? = null
)

/** Data portion of Modify Order API response. */
data class ModifyOrderData(
    @JsonProperty("order_id")
    val orderId: String?
)

/**
 * Response from Modify Order V3 API.
 *
 * @property orderId The modified order ID
 * @property latency Processing time in milliseconds
 * @see <a href="https://upstox.com/developer/api-documentation/v3/modify-order">Modify Order V3 API</a>
 */
data class ModifyOrderResponse(
    @JsonProperty("order_id")
    val orderId: String?,

    @JsonProperty("latency")
    val latency: Int?
)
