package io.github.sonicalgo.upstox.usecase

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.Order

/** Gets the history/audit trail of an order. */
@JvmSynthetic
internal fun executeGetOrderHistory(
    apiClient: ApiClient,
    orderId: String? = null,
    tag: String? = null
): List<Order> {
    val queryParams = mutableMapOf<String, String?>()
    orderId?.let { queryParams["order_id"] = it }
    tag?.let { queryParams["tag"] = it }

    val response: UpstoxResponse<List<Order>> = apiClient.get(
        endpoint = "/order/history",
        queryParams = queryParams,
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}
