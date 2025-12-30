package io.github.sonicalgo.upstox.usecase

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.Order

/** Gets all orders for the day (order book). */
@JvmSynthetic
internal fun executeGetOrders(apiClient: ApiClient): List<Order> {
    val response: UpstoxResponse<List<Order>> = apiClient.get(
        endpoint = "/order/retrieve-all",
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}
