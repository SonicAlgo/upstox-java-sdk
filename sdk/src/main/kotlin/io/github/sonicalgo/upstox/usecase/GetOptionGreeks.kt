package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V3
import io.github.sonicalgo.upstox.util.parseMapResponse
import io.github.sonicalgo.upstox.validation.Validators

private const val MAX_OPTION_GREEKS_INSTRUMENTS = 50

/** Gets option Greeks for instruments. */
@JvmSynthetic
internal fun executeGetOptionGreeks(
    apiClient: ApiClient,
    instrumentKeys: List<String>
): Map<String, OptionGreeksQuote> {
    Validators.validateListSize(instrumentKeys, MAX_OPTION_GREEKS_INSTRUMENTS, "getOptionGreeks")

    val queryParams = mapOf("instrument_key" to instrumentKeys.joinToString(","))
    val rawResponse = apiClient.getRaw(
        endpoint = "/market-quote/option-greek",
        queryParams = queryParams,
        overrideBaseUrl = BASE_URL_V3
    )
    return parseMapResponse(rawResponse)
}

/**
 * Option Greeks quote.
 *
 * @property lastPrice Last traded price
 * @property instrumentToken Instrument key identifier
 * @property ltq Last traded quantity
 * @property volume Volume traded today
 * @property cp Previous day's closing price
 * @property iv Implied Volatility - market's expectation of future volatility
 * @property delta Delta - sensitivity of option price to underlying price changes (range: -1 to 1)
 * @property gamma Gamma - rate of change of delta
 * @property theta Theta - time decay rate of decline in option value
 * @property vega Vega - sensitivity of option price to changes in volatility
 * @property oi Open interest for the instrument
 * @see <a href="https://upstox.com/developer/api-documentation/option-greek">Option Greek API</a>
 */
data class OptionGreeksQuote(
    @JsonProperty("last_price")
    val lastPrice: Double?,

    @JsonProperty("instrument_token")
    val instrumentToken: String?,

    @JsonProperty("ltq")
    val ltq: Int?,

    @JsonProperty("volume")
    val volume: Long?,

    @JsonProperty("cp")
    val cp: Double?,

    @JsonProperty("iv")
    val iv: Double?,

    @JsonProperty("delta")
    val delta: Double?,

    @JsonProperty("gamma")
    val gamma: Double?,

    @JsonProperty("theta")
    val theta: Double?,

    @JsonProperty("vega")
    val vega: Double?,

    @JsonProperty("oi")
    val oi: Long?
)