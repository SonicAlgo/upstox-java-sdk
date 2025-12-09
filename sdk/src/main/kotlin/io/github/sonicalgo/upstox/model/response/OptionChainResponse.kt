package io.github.sonicalgo.upstox.model.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.model.enums.*

/**
 * Option contract information.
 *
 * Contains details about an option contract.
 *
 * @property name Contract name
 * @property segment Market segment (NSE_FO, BSE_FO, MCX_FO, etc.)
 * @property exchange Exchange: NSE, BSE, or MCX
 * @property expiry Expiry date in YYYY-MM-dd format
 * @property instrumentKey Unique instrument identifier
 * @property exchangeToken Exchange-specific token
 * @property tradingSymbol Trading symbol
 * @property tickSize Minimum price movement
 * @property lotSize Size of one lot
 * @property instrumentType Option type: CALL_OPTION or PUT_OPTION
 * @property freezeQuantity Maximum quantity that can be frozen
 * @property underlyingKey Instrument key of the underlying asset
 * @property underlyingType Underlying type: COMMODITY, INDEX, EQUITY, CURRENCY, or INTEREST_RATE_DERIVATIVE
 * @property underlyingSymbol Symbol of underlying asset
 * @property strikePrice Option strike price
 * @property minimumLot Minimum lot size
 * @property weekly Whether this is a weekly expiry contract
 * @see <a href="https://upstox.com/developer/api-documentation/get-option-contracts">Option Contracts API</a>
 */
data class OptionContract(
    @JsonProperty("name")
    val name: String,

    @JsonProperty("segment")
    val segment: Segment,

    @JsonProperty("exchange")
    val exchange: Exchange,

    @JsonProperty("expiry")
    val expiry: String,

    @JsonProperty("instrument_key")
    val instrumentKey: String,

    @JsonProperty("exchange_token")
    val exchangeToken: String,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String,

    @JsonProperty("tick_size")
    val tickSize: Double,

    @JsonProperty("lot_size")
    val lotSize: Int,

    @JsonProperty("instrument_type")
    val instrumentType: InstrumentType,

    @JsonProperty("freeze_quantity")
    val freezeQuantity: Int,

    @JsonProperty("underlying_key")
    val underlyingKey: String,

    @JsonProperty("underlying_type")
    val underlyingType: UnderlyingType,

    @JsonProperty("underlying_symbol")
    val underlyingSymbol: String,

    @JsonProperty("strike_price")
    val strikePrice: Double,

    @JsonProperty("minimum_lot")
    val minimumLot: Int,

    @JsonProperty("weekly")
    val weekly: Boolean
)

/**
 * Put/Call option chain entry.
 *
 * Contains option chain data for a specific strike price.
 *
 * @property expiry Expiry date in YYYY-MM-dd format
 * @property pcr Put-Call Ratio (ratio of put options to call options volume/OI)
 * @property strikePrice Strike price
 * @property underlyingKey Underlying asset's instrument key
 * @property underlyingSpotPrice Current spot price of underlying
 * @property callOptions Call option data
 * @property putOptions Put option data
 * @see <a href="https://upstox.com/developer/api-documentation/get-pc-option-chain">Option Chain API</a>
 */
data class OptionChainEntry(
    @JsonProperty("expiry")
    val expiry: String,

    @JsonProperty("pcr")
    val pcr: Double? = null,

    @JsonProperty("strike_price")
    val strikePrice: Double,

    @JsonProperty("underlying_key")
    val underlyingKey: String,

    @JsonProperty("underlying_spot_price")
    val underlyingSpotPrice: Double,

    @JsonProperty("call_options")
    val callOptions: OptionData,

    @JsonProperty("put_options")
    val putOptions: OptionData
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
    val instrumentKey: String,

    @JsonProperty("market_data")
    val marketData: OptionMarketData,

    @JsonProperty("option_greeks")
    val optionGreeks: OptionGreeks
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
    val ltp: Double,

    @JsonProperty("close_price")
    val closePrice: Double,

    @JsonProperty("volume")
    val volume: Long,

    @JsonProperty("oi")
    val oi: Long,

    @JsonProperty("bid_price")
    val bidPrice: Double,

    @JsonProperty("bid_qty")
    val bidQty: Int,

    @JsonProperty("ask_price")
    val askPrice: Double,

    @JsonProperty("ask_qty")
    val askQty: Int,

    @JsonProperty("prev_oi")
    val prevOi: Long
)

/**
 * Option Greeks.
 *
 * Contains Greek values for option pricing analysis.
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
    val vega: Double,

    @JsonProperty("theta")
    val theta: Double,

    @JsonProperty("gamma")
    val gamma: Double,

    @JsonProperty("delta")
    val delta: Double,

    @JsonProperty("iv")
    val iv: Double,

    @JsonProperty("pop")
    val pop: Double? = null
)

/**
 * Expired instrument (option or future) contract.
 *
 * Contains details about expired derivative contracts.
 *
 * @property name Contract name
 * @property segment Market segment
 * @property exchange Exchange: NSE, BSE, or MCX
 * @property expiry Expiry date in YYYY-MM-dd format
 * @property instrumentKey Unique instrument identifier for the expired contract
 * @property exchangeToken Exchange-specific token
 * @property tradingSymbol Trading symbol
 * @property tickSize Minimum price movement
 * @property lotSize Size of one lot
 * @property instrumentType Instrument type: FUTURES, CALL_OPTION, or PUT_OPTION
 * @property freezeQuantity Maximum quantity that can be frozen
 * @property underlyingKey Instrument key of the underlying
 * @property underlyingType Underlying type: COMMODITY, INDEX, EQUITY, CURRENCY, or INTEREST_RATE_DERIVATIVE
 * @property underlyingSymbol Symbol of underlying
 * @property strikePrice Option strike price (not present for futures)
 * @property minimumLot Minimum lot size
 * @property weekly Whether this was a weekly expiry contract
 * @see <a href="https://upstox.com/developer/api-documentation/get-expired-option-contracts">Expired Option Contracts API</a>
 */
data class ExpiredContract(
    @JsonProperty("name")
    val name: String,

    @JsonProperty("segment")
    val segment: Segment,

    @JsonProperty("exchange")
    val exchange: Exchange,

    @JsonProperty("expiry")
    val expiry: String,

    @JsonProperty("instrument_key")
    val instrumentKey: String,

    @JsonProperty("exchange_token")
    val exchangeToken: String,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String,

    @JsonProperty("tick_size")
    val tickSize: Double,

    @JsonProperty("lot_size")
    val lotSize: Int,

    @JsonProperty("instrument_type")
    val instrumentType: InstrumentType,

    @JsonProperty("freeze_quantity")
    val freezeQuantity: Int,

    @JsonProperty("underlying_key")
    val underlyingKey: String,

    @JsonProperty("underlying_type")
    val underlyingType: UnderlyingType,

    @JsonProperty("underlying_symbol")
    val underlyingSymbol: String,

    @JsonProperty("strike_price")
    val strikePrice: Double? = null,

    @JsonProperty("minimum_lot")
    val minimumLot: Int,

    @JsonProperty("weekly")
    val weekly: Boolean? = null
)
