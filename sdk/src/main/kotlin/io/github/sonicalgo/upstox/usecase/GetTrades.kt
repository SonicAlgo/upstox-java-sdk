package io.github.sonicalgo.upstox.usecase

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.Trade

/** Gets all trades for the day. */
@JvmSynthetic
internal fun executeGetTrades(apiClient: ApiClient): List<Trade> {
    val response: UpstoxResponse<List<Trade>> = apiClient.get(
        endpoint = "/order/trades/get-trades-for-day",
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}
