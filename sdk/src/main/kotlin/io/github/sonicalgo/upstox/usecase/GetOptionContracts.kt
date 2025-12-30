package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.Exchange
import io.github.sonicalgo.upstox.common.Segment

/** Gets option contracts for an underlying instrument. */
@JvmSynthetic
internal fun executeGetOptionContracts(
    apiClient: ApiClient,
    instrumentKey: String,
    expiryDate: String? = null
): List<OptionContract> {
    val queryParams = mutableMapOf<String, String?>("instrument_key" to instrumentKey)
    expiryDate?.let { queryParams["expiry_date"] = it }

    val response: UpstoxResponse<List<OptionContract>> = apiClient.get(
        endpoint = "/option/contract",
        queryParams = queryParams,
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}

/** Option type for derivatives. */
enum class OptionType {
    /** Call Option */
    @JsonProperty("CE")
    CALL_OPTION,

    /** Put Option */
    @JsonProperty("PE")
    PUT_OPTION
}

/** Underlying asset type for derivatives. */
enum class UnderlyingType {
    /** Commodity */
    @JsonProperty("COM")
    COMMODITY,

    /** Index */
    INDEX,

    /** Equity */
    EQUITY,

    /** Currency */
    @JsonProperty("CUR")
    CURRENCY,

    /** Interest Rate Derivative */
    @JsonProperty("IRD")
    INTEREST_RATE_DERIVATIVE
}

/** Instrument type for futures and options. */
enum class InstrumentType {
    /** Futures contract */
    @JsonProperty("FUT")
    FUTURES,

    /** Call Option */
    @JsonProperty("CE")
    CALL_OPTION,

    /** Put Option */
    @JsonProperty("PE")
    PUT_OPTION
}

/**
 * Option contract information.
 *
 * @property name Contract name
 * @property segment Market segment
 * @property exchange Exchange: NSE, BSE, or MCX
 * @property expiry Expiry date (yyyy-MM-dd)
 * @property instrumentKey Unique instrument identifier
 * @property exchangeToken Exchange-specific token
 * @property tradingSymbol Trading symbol
 * @property tickSize Minimum price movement
 * @property lotSize Size of one lot
 * @property instrumentType Option type
 * @property freezeQuantity Maximum quantity that can be frozen
 * @property underlyingKey Instrument key of underlying asset
 * @property underlyingType Underlying type
 * @property underlyingSymbol Symbol of underlying asset
 * @property strikePrice Option strike price
 * @property minimumLot Minimum lot size
 * @property weekly Whether this is a weekly expiry contract
 * @see <a href="https://upstox.com/developer/api-documentation/get-option-contracts">Option Contracts API</a>
 */
data class OptionContract(
    @JsonProperty("name")
    val name: String?,

    @JsonProperty("segment")
    val segment: Segment?,

    @JsonProperty("exchange")
    val exchange: Exchange?,

    @JsonProperty("expiry")
    val expiry: String?,

    @JsonProperty("instrument_key")
    val instrumentKey: String?,

    @JsonProperty("exchange_token")
    val exchangeToken: String?,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String?,

    @JsonProperty("tick_size")
    val tickSize: Double?,

    @JsonProperty("lot_size")
    val lotSize: Int?,

    @JsonProperty("instrument_type")
    val instrumentType: InstrumentType?,

    @JsonProperty("freeze_quantity")
    val freezeQuantity: Int?,

    @JsonProperty("underlying_key")
    val underlyingKey: String?,

    @JsonProperty("underlying_type")
    val underlyingType: UnderlyingType?,

    @JsonProperty("underlying_symbol")
    val underlyingSymbol: String?,

    @JsonProperty("strike_price")
    val strikePrice: Double?,

    @JsonProperty("minimum_lot")
    val minimumLot: Int?,

    @JsonProperty("weekly")
    val weekly: Boolean?
)