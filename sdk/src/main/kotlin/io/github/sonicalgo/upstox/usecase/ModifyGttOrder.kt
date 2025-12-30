package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V3
import io.github.sonicalgo.upstox.common.UpstoxResponseWithMetadata
import io.github.sonicalgo.upstox.common.GttType

/** Modifies an existing GTT order. */
@JvmSynthetic
internal fun executeModifyGttOrder(
    apiClient: ApiClient,
    params: ModifyGttOrderParams
): GttOrderResponse {
    val response: UpstoxResponseWithMetadata<GttOrderData> = apiClient.put(
        endpoint = "/order/gtt/modify",
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
 * Parameters for modifying a GTT order.
 *
 * @property gttOrderId The GTT order ID to modify
 * @property type GTT type: SINGLE or MULTIPLE
 * @property quantity New order quantity
 * @property rules Updated list of GTT rules
 */
@GenerateBuilder
data class ModifyGttOrderParams(
    @JsonProperty("gtt_order_id")
    val gttOrderId: String,

    @JsonProperty("type")
    val type: GttType,

    @JsonProperty("quantity")
    val quantity: Int? = null,

    @JsonProperty("rules")
    val rules: List<GttRuleParams>
)
