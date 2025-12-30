package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V3
import io.github.sonicalgo.upstox.util.parseMapResponse
import io.github.sonicalgo.upstox.validation.Validators

private const val MAX_QUOTE_INSTRUMENTS = 500

/** Gets LTP (Last Traded Price) quotes for instruments. */
@JvmSynthetic
internal fun executeGetLtp(
    apiClient: ApiClient,
    instrumentKeys: List<String>
): Map<String, LtpQuote> {
    Validators.validateListSize(instrumentKeys, MAX_QUOTE_INSTRUMENTS, "getLtp")

    val queryParams = mapOf("instrument_key" to instrumentKeys.joinToString(","))
    val rawResponse = apiClient.getRaw(
        endpoint = "/market-quote/ltp",
        queryParams = queryParams,
        overrideBaseUrl = BASE_URL_V3
    )
    return parseMapResponse(rawResponse)
}

/**
 * LTP (Last Traded Price) quote.
 *
 * @property lastPrice Last traded price
 * @property instrumentToken Instrument key identifier
 * @property ltq Last traded quantity (V3 only)
 * @property volume Volume traded today (V3 only)
 * @property cp Previous day's closing price (V3 only)
 * @see <a href="https://upstox.com/developer/api-documentation/ltp">LTP API</a>
 */
data class LtpQuote(
    @JsonProperty("last_price")
    val lastPrice: Double?,

    @JsonProperty("instrument_token")
    val instrumentToken: String?,

    @JsonProperty("ltq")
    val ltq: Int?,

    @JsonProperty("volume")
    val volume: Long?,

    @JsonProperty("cp")
    val cp: Double?
)