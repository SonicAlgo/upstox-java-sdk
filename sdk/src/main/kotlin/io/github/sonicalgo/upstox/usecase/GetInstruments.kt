package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.exception.UpstoxApiException
import io.github.sonicalgo.upstox.common.Exchange
import io.github.sonicalgo.upstox.common.Segment

private const val BASE_URL = "https://assets.upstox.com/market-quote/instruments/exchange"

private val INSTRUMENT_URLS = mapOf(
    InstrumentDownloadType.COMPLETE to "$BASE_URL/complete.json.gz",
    InstrumentDownloadType.NSE to "$BASE_URL/NSE.json.gz",
    InstrumentDownloadType.BSE to "$BASE_URL/BSE.json.gz",
    InstrumentDownloadType.MCX to "$BASE_URL/MCX.json.gz",
    InstrumentDownloadType.SUSPENDED to "$BASE_URL/suspended-instrument.json.gz",
    InstrumentDownloadType.MTF to "$BASE_URL/MTF.json.gz",
    InstrumentDownloadType.NSE_MIS to "$BASE_URL/NSE_MIS.json.gz",
    InstrumentDownloadType.BSE_MIS to "$BASE_URL/BSE_MIS.json.gz"
)

/** Gets instruments for the specified type. */
@JvmSynthetic
internal fun executeGetInstruments(apiClient: ApiClient, type: InstrumentDownloadType): List<Instrument> {
    val url = INSTRUMENT_URLS[type]
        ?: throw UpstoxApiException("Unknown instrument type: $type", null)

    return apiClient.downloadGzipJson(url)
}

/**
 * Returns the download URL for the specified instrument type.
 */
@JvmSynthetic
internal fun executeGetInstrumentsUrl(type: InstrumentDownloadType): String {
    return INSTRUMENT_URLS[type]
        ?: throw UpstoxApiException("Unknown instrument type: $type", null)
}

/**
 * Type of instrument list to download from the Upstox instruments master.
 *
 * Used with [io.github.sonicalgo.upstox.Upstox.getInstruments] to specify
 * which instrument list to download.
 *
 * @see <a href="https://upstox.com/developer/api-documentation/instruments">Instruments API</a>
 */
enum class InstrumentDownloadType {
    /** All instruments across all exchanges */
    COMPLETE,
    /** NSE instruments only */
    NSE,
    /** BSE instruments only */
    BSE,
    /** MCX instruments only */
    MCX,
    /** Suspended instruments across all exchanges */
    SUSPENDED,
    /** Margin Trading Facility eligible instruments */
    MTF,
    /** NSE Margin Intraday Square-off eligible instruments */
    NSE_MIS,
    /** BSE Margin Intraday Square-off eligible instruments */
    BSE_MIS
}

/**
 * Represents a tradeable instrument from the Upstox instruments master.
 *
 * This data class contains all fields that may be present in the instrument JSON.
 * Fields may be null depending on the instrument type (equity, derivative, etc.)
 * and the instrument list type (BOD, MTF, MIS).
 *
 * The instruments master is refreshed daily around 6 AM.
 *
 * @property instrumentKey Unique identifier for the instrument across all Upstox APIs.
 *                         Format: "SEGMENT|IDENTIFIER" (e.g., "NSE_EQ|INE002A01018")
 * @property exchangeToken Exchange-specific identifier. Note: May be reused after expiry for derivatives.
 * @property tradingSymbol The symbol used for trading (e.g., "RELIANCE", "NIFTY23DECFUT")
 * @property name Full name of the instrument
 * @property shortName Shorter or abbreviated name of the instrument (equity instruments)
 * @property segment Market segment (e.g., NSE_EQ, NSE_FO, BSE_EQ)
 * @property exchange Exchange where the instrument is traded (e.g., NSE, BSE, MCX)
 * @property instrumentType Type of instrument (e.g., "EQ", "FUT", "CE", "PE", "INDEX")
 * @property isin International Securities Identification Number (equity instruments only)
 * @property lotSize Trading lot size
 * @property freezeQuantity Maximum quantity allowed in single order
 * @property tickSize Minimum price movement
 * @property securityType Security classification (e.g., "NORMAL")
 * @property weekly Whether this is a weekly expiry contract (futures/options)
 * @property expiry Expiry timestamp in epoch milliseconds (futures/options)
 * @property underlyingSymbol Symbol of underlying instrument (for derivatives)
 * @property underlyingKey Instrument key of underlying instrument (for derivatives)
 * @property underlyingType Type of underlying asset (e.g., "EQUITY", "INDEX", "COMMODITY")
 * @property strikePrice Strike price for options
 * @property minimumLot Minimum lot size for derivatives
 * @property lastTradingDate Last trading date for derivatives
 * @property mtfEnabled Whether MTF (Margin Trading Facility) is enabled
 * @property mtfBracket MTF leverage factor/bracket
 * @property intradayMargin Percentage of LTP required for intraday trading (MIS)
 * @property intradayLeverage Leverage factor for intraday trading (MIS)
 *
 * @see <a href="https://upstox.com/developer/api-documentation/instruments">Instruments API</a>
 */
data class Instrument(
    @JsonProperty("instrument_key")
    val instrumentKey: String?,

    @JsonProperty("exchange_token")
    val exchangeToken: String?,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String?,

    @JsonProperty("name")
    val name: String?,

    @JsonProperty("short_name")
    val shortName: String?,

    @JsonProperty("segment")
    val segment: Segment?,

    @JsonProperty("exchange")
    val exchange: Exchange?,

    @JsonProperty("instrument_type")
    val instrumentType: String?,

    @JsonProperty("isin")
    val isin: String?,

    @JsonProperty("lot_size")
    val lotSize: Int?,

    @JsonProperty("freeze_quantity")
    val freezeQuantity: Double?,

    @JsonProperty("tick_size")
    val tickSize: Double?,

    @JsonProperty("security_type")
    val securityType: String?,

    @JsonProperty("weekly")
    val weekly: Boolean?,

    @JsonProperty("expiry")
    val expiry: Long?,

    @JsonProperty("underlying_symbol")
    val underlyingSymbol: String?,

    @JsonProperty("underlying_key")
    val underlyingKey: String?,

    @JsonProperty("underlying_type")
    val underlyingType: String?,

    @JsonProperty("strike_price")
    val strikePrice: Double?,

    @JsonProperty("minimum_lot")
    val minimumLot: Int?,

    @JsonProperty("last_trading_date")
    val lastTradingDate: String?,

    @JsonProperty("mtf_enabled")
    val mtfEnabled: Boolean?,

    @JsonProperty("mtf_bracket")
    val mtfBracket: Double?,

    @JsonProperty("intraday_margin")
    val intradayMargin: Double?,

    @JsonProperty("intraday_leverage")
    val intradayLeverage: Double?
)