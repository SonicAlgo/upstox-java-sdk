package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.Exchange

/** Gets the current market status for an exchange. */
@JvmSynthetic
internal fun executeGetMarketStatus(apiClient: ApiClient, exchange: String): MarketStatusResponse {
    val response: UpstoxResponse<MarketStatusResponse> = apiClient.get(
        endpoint = "/market/status/$exchange",
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}

/** Market status values. */
enum class MarketStatus {
    NORMAL_OPEN,
    NORMAL_CLOSE,
    PRE_OPEN_START,
    PRE_OPEN_END,
    CLOSING_START,
    CLOSING_END
}

/**
 * Exchange market status.
 *
 * @property exchange Exchange identifier
 * @property status Current market status
 * @property lastUpdated Timestamp when status was last updated (milliseconds)
 * @see <a href="https://upstox.com/developer/api-documentation/get-market-status">Market Status API</a>
 */
data class MarketStatusResponse(
    @JsonProperty("exchange")
    val exchange: Exchange?,

    @JsonProperty("status")
    val status: MarketStatus?,

    @JsonProperty("last_updated")
    val lastUpdated: Long?
)