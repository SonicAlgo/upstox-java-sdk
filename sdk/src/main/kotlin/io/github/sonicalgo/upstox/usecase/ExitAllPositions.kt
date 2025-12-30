package io.github.sonicalgo.upstox.usecase

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.MultiOrderResponse
import io.github.sonicalgo.upstox.common.Segment
import java.net.URLEncoder

/** Exits all open positions. */
@JvmSynthetic
internal fun executeExitAllPositions(
    apiClient: ApiClient,
    segment: Segment? = null,
    tag: String? = null
): MultiOrderResponse {
    // Build query params for POST request (post() doesn't have queryParams param unlike delete())
    val queryParams = mutableListOf<String>()
    segment?.let { queryParams.add("segment=${URLEncoder.encode(it.name, Charsets.UTF_8)}") }
    tag?.let { queryParams.add("tag=${URLEncoder.encode(it, Charsets.UTF_8)}") }

    val endpoint = if (queryParams.isNotEmpty()) {
        "/order/positions/exit?${queryParams.joinToString("&")}"
    } else {
        "/order/positions/exit"
    }

    return apiClient.post(
        endpoint = endpoint,
        overrideBaseUrl = BASE_URL_V2
    )
}
