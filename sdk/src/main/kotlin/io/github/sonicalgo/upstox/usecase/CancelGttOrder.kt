package io.github.sonicalgo.upstox.usecase

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V3
import io.github.sonicalgo.upstox.common.UpstoxResponseWithMetadata

/** Cancels a GTT order. */
@JvmSynthetic
internal fun executeCancelGttOrder(
    apiClient: ApiClient,
    gttOrderId: String
): GttOrderResponse {
    val response: UpstoxResponseWithMetadata<GttOrderData> = apiClient.delete(
        endpoint = "/order/gtt/cancel",
        body = mapOf("gtt_order_id" to gttOrderId),
        overrideBaseUrl = BASE_URL_V3
    )
    val data = response.dataOrThrow()
    return GttOrderResponse(
        gttOrderIds = data.gttOrderIds,
        latency = response.metadata?.latency
    )
}
