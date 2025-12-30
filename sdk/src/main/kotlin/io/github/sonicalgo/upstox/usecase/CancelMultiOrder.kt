package io.github.sonicalgo.upstox.usecase

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.MultiOrderResponse
import io.github.sonicalgo.upstox.common.Segment

/** Cancels multiple orders. */
@JvmSynthetic
internal fun executeCancelMultiOrder(
    apiClient: ApiClient,
    segment: Segment? = null,
    tag: String? = null
): MultiOrderResponse {
    val queryParams = mutableMapOf<String, String?>()
    segment?.let { queryParams["segment"] = it.name }
    tag?.let { queryParams["tag"] = it }

    return apiClient.delete(
        endpoint = "/order/multi/cancel",
        queryParams = queryParams,
        overrideBaseUrl = BASE_URL_V2
    )
}
