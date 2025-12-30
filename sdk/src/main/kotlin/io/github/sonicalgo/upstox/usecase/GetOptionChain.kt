package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse

/** Gets the put/call option chain for an underlying. */
@JvmSynthetic
internal fun executeGetOptionChain(
    apiClient: ApiClient,
    instrumentKey: String,
    expiryDate: String
): List<OptionChainEntry> {
    val response: UpstoxResponse<List<OptionChainEntry>> = apiClient.get(
        endpoint = "/option/chain",
        queryParams = mapOf("instrument_key" to instrumentKey, "expiry_date" to expiryDate),
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}

/**
 * Put/Call option chain entry.
 *
 * @property expiry Expiry date (yyyy-MM-dd)
 * @property pcr Put-Call Ratio
 * @property strikePrice Strike price
 * @property underlyingKey Underlying asset's instrument key
 * @property underlyingSpotPrice Current spot price of underlying
 * @property callOptions Call option data
 * @property putOptions Put option data
 * @see <a href="https://upstox.com/developer/api-documentation/get-pc-option-chain">Option Chain API</a>
 */
data class OptionChainEntry(
    @JsonProperty("expiry")
    val expiry: String?,

    @JsonProperty("pcr")
    val pcr: Double?,

    @JsonProperty("strike_price")
    val strikePrice: Double?,

    @JsonProperty("underlying_key")
    val underlyingKey: String?,

    @JsonProperty("underlying_spot_price")
    val underlyingSpotPrice: Double?,

    @JsonProperty("call_options")
    val callOptions: OptionData?,

    @JsonProperty("put_options")
    val putOptions: OptionData?
)

/**
 * Option data within the option chain.
 *
 * @property instrumentKey Instrument key for this option
 * @property marketData Market data for this option
 * @property optionGreeks Option Greeks for this option
 */
data class OptionData(
    @JsonProperty("instrument_key")
    val instrumentKey: String?,

    @JsonProperty("market_data")
    val marketData: OptionMarketData?,

    @JsonProperty("option_greeks")
    val optionGreeks: OptionGreeks?
)

/**
 * Market data for an option.
 *
 * @property ltp Last traded price
 * @property closePrice Previous closing price
 * @property volume Total traded volume
 * @property oi Open interest
 * @property bidPrice Best bid price
 * @property bidQty Best bid quantity
 * @property askPrice Best ask price
 * @property askQty Best ask quantity
 * @property prevOi Previous day's open interest
 */
data class OptionMarketData(
    @JsonProperty("ltp")
    val ltp: Double?,

    @JsonProperty("close_price")
    val closePrice: Double?,

    @JsonProperty("volume")
    val volume: Long?,

    @JsonProperty("oi")
    val oi: Long?,

    @JsonProperty("bid_price")
    val bidPrice: Double?,

    @JsonProperty("bid_qty")
    val bidQty: Int?,

    @JsonProperty("ask_price")
    val askPrice: Double?,

    @JsonProperty("ask_qty")
    val askQty: Int?,

    @JsonProperty("prev_oi")
    val prevOi: Long?
)

/**
 * Option Greeks.
 *
 * @property vega Vega - premium sensitivity to volatility changes
 * @property theta Theta - time decay impact on premium
 * @property gamma Gamma - rate of delta change
 * @property delta Delta - premium sensitivity to underlying movement
 * @property iv Implied Volatility
 * @property pop Probability of Profit
 */
data class OptionGreeks(
    @JsonProperty("vega")
    val vega: Double?,

    @JsonProperty("theta")
    val theta: Double?,

    @JsonProperty("gamma")
    val gamma: Double?,

    @JsonProperty("delta")
    val delta: Double?,

    @JsonProperty("iv")
    val iv: Double?,

    @JsonProperty("pop")
    val pop: Double?
)