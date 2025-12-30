package io.github.sonicalgo.upstox.usecase

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.Order

/** Gets details of a specific order. */
@JvmSynthetic
internal fun executeGetOrderDetails(apiClient: ApiClient, orderId: String): Order {
    val response: UpstoxResponse<Order> = apiClient.get(
        endpoint = "/order/details",
        queryParams = mapOf("order_id" to orderId),
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}
