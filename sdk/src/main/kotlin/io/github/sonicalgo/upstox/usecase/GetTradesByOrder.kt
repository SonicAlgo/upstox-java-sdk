package io.github.sonicalgo.upstox.usecase

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.Trade

/** Gets trades for a specific order. */
@JvmSynthetic
internal fun executeGetTradesByOrder(apiClient: ApiClient, orderId: String): List<Trade> {
    val response: UpstoxResponse<List<Trade>> = apiClient.get(
        endpoint = "/order/trades",
        queryParams = mapOf("order_id" to orderId),
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}
