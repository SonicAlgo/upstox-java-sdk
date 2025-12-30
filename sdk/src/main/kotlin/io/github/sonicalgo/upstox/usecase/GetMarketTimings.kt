package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.Exchange

/** Gets market timings for a specific date. */
@JvmSynthetic
internal fun executeGetMarketTimings(apiClient: ApiClient, date: String): List<MarketTiming> {
    val response: UpstoxResponse<List<MarketTiming>> = apiClient.get(
        endpoint = "/market/timings/$date",
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}

/**
 * Market timing for a specific date.
 *
 * @property exchange Exchange identifier
 * @property startTime Market open timestamp in milliseconds
 * @property endTime Market close timestamp in milliseconds
 * @see <a href="https://upstox.com/developer/api-documentation/get-market-timings">Market Timings API</a>
 */
data class MarketTiming(
    @JsonProperty("exchange")
    val exchange: Exchange?,

    @JsonProperty("start_time")
    val startTime: Long?,

    @JsonProperty("end_time")
    val endTime: Long?
)