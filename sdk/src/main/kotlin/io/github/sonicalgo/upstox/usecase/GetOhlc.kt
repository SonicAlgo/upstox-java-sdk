package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V3
import io.github.sonicalgo.upstox.util.parseMapResponse
import io.github.sonicalgo.upstox.validation.Validators

private const val MAX_QUOTE_INSTRUMENTS = 500

/** Gets OHLC quotes for instruments. */
@JvmSynthetic
internal fun executeGetOhlc(
    apiClient: ApiClient,
    instrumentKeys: List<String>,
    interval: OhlcInterval
): Map<String, OhlcQuote> {
    Validators.validateListSize(instrumentKeys, MAX_QUOTE_INSTRUMENTS, "getOhlc")

    val queryParams = mapOf(
        "instrument_key" to instrumentKeys.joinToString(","),
        "interval" to interval.toString()
    )
    val rawResponse = apiClient.getRaw(
        endpoint = "/market-quote/ohlc",
        queryParams = queryParams,
        overrideBaseUrl = BASE_URL_V3
    )
    return parseMapResponse(rawResponse)
}

/**
 * OHLC quote response (V2/V3).
 *
 * @property lastPrice Last traded price
 * @property instrumentToken Instrument key identifier
 * @property ohlc OHLC data (V2 API)
 * @property prevOhlc Previous candle OHLC data (V3 API)
 * @property liveOhlc Current candle OHLC data (V3 API)
 * @see <a href="https://upstox.com/developer/api-documentation/get-market-quote-ohlc">OHLC Quote API</a>
 */
data class OhlcQuote(
    @JsonProperty("last_price")
    val lastPrice: Double?,

    @JsonProperty("instrument_token")
    val instrumentToken: String?,

    @JsonProperty("ohlc")
    val ohlc: OhlcData?,

    @JsonProperty("prev_ohlc")
    val prevOhlc: OhlcV3Data?,

    @JsonProperty("live_ohlc")
    val liveOhlc: OhlcV3Data?
)

/**
 * V3 OHLC data with volume and timestamp.
 *
 * @property open Opening price
 * @property high Highest price
 * @property low Lowest price
 * @property close Closing price
 * @property volume Trading volume
 * @property ts Candle start timestamp
 */
data class OhlcV3Data(
    @JsonProperty("open")
    val open: Double?,

    @JsonProperty("high")
    val high: Double?,

    @JsonProperty("low")
    val low: Double?,

    @JsonProperty("close")
    val close: Double?,

    @JsonProperty("volume")
    val volume: Long?,

    @JsonProperty("ts")
    val ts: Long?
)

/**
 * OHLC quote interval options.
 */
enum class OhlcInterval {
    /** 1 day interval */
    @JsonProperty("1d")
    ONE_DAY,

    /** 1 minute interval */
    @JsonProperty("I1")
    ONE_MINUTE,

    /** 30 minute interval */
    @JsonProperty("I30")
    THIRTY_MINUTE;

    override fun toString(): String = when (this) {
        ONE_DAY -> "1d"
        ONE_MINUTE -> "I1"
        THIRTY_MINUTE -> "I30"
    }
}