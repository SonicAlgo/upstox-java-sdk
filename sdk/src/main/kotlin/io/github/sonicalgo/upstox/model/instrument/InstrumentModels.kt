package io.github.sonicalgo.upstox.model.instrument

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.model.enums.Exchange
import io.github.sonicalgo.upstox.model.enums.Segment

/**
 * Type of instrument list to download from the Upstox instruments master.
 *
 * Used with [io.github.sonicalgo.upstox.api.InstrumentsApi.getInstruments] to specify
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
 *                         Format: "EXCHANGE|TOKEN" (e.g., "NSE_EQ|INE002A01018")
 * @property exchangeToken Exchange-specific identifier. Note: May be reused after expiry for derivatives.
 * @property tradingSymbol The symbol used for trading (e.g., "RELIANCE", "NIFTY23DECFUT")
 * @property name Full name of the instrument
 * @property segment Market segment (e.g., "NSE_EQ", "NSE_FO", "BSE_EQ")
 * @property exchange Exchange where the instrument is traded (e.g., "NSE", "BSE", "MCX")
 * @property instrumentType Type of instrument (e.g., "EQ", "FUTIDX", "OPTIDX", "FUTSTK")
 * @property isin International Securities Identification Number (equity instruments only)
 * @property lotSize Trading lot size
 * @property freezeQuantity Maximum quantity allowed in single order
 * @property tickSize Minimum price movement
 * @property securityType Security classification
 * @property expiry Expiry date for derivatives (format: varies)
 * @property underlyingSymbol Symbol of underlying instrument (for derivatives)
 * @property underlyingKey Instrument key of underlying instrument (for derivatives)
 * @property strikePrice Strike price for options
 * @property minimumLot Minimum lot size for derivatives
 * @property lastTradingDate Last trading date for derivatives
 * @property mtfEnabled Whether MTF (Margin Trading Facility) is enabled
 * @property mtfBracket MTF bracket/category
 * @property intradayMargin Margin required for intraday trading
 * @property intradayLeverage Leverage available for intraday trading
 *
 * @see <a href="https://upstox.com/developer/api-documentation/instruments">Instruments API</a>
 */
data class Instrument(
    @JsonProperty("instrument_key")
    val instrumentKey: String,

    @JsonProperty("exchange_token")
    val exchangeToken: String? = null,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String? = null,

    @JsonProperty("name")
    val name: String? = null,

    @JsonProperty("segment")
    val segment: Segment? = null,

    @JsonProperty("exchange")
    val exchange: Exchange? = null,

    @JsonProperty("instrument_type")
    val instrumentType: String? = null,

    @JsonProperty("isin")
    val isin: String? = null,

    @JsonProperty("lot_size")
    val lotSize: Int? = null,

    @JsonProperty("freeze_quantity")
    val freezeQuantity: Int? = null,

    @JsonProperty("tick_size")
    val tickSize: Double? = null,

    @JsonProperty("security_type")
    val securityType: String? = null,

    @JsonProperty("expiry")
    val expiry: String? = null,

    @JsonProperty("underlying_symbol")
    val underlyingSymbol: String? = null,

    @JsonProperty("underlying_key")
    val underlyingKey: String? = null,

    @JsonProperty("strike_price")
    val strikePrice: Double? = null,

    @JsonProperty("minimum_lot")
    val minimumLot: Int? = null,

    @JsonProperty("last_trading_date")
    val lastTradingDate: String? = null,

    @JsonProperty("mtf_enabled")
    val mtfEnabled: Boolean? = null,

    @JsonProperty("mtf_bracket")
    val mtfBracket: String? = null,

    @JsonProperty("intraday_margin")
    val intradayMargin: Double? = null,

    @JsonProperty("intraday_leverage")
    val intradayLeverage: Double? = null
)
