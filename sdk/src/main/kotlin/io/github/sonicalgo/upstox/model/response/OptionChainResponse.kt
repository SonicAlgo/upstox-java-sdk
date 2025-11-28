package io.github.sonicalgo.upstox.model.response

import com.google.gson.annotations.SerializedName

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
 * @property instrumentType Option type: CE (Call) or PE (Put)
 * @property freezeQuantity Maximum quantity that can be frozen
 * @property underlyingKey Instrument key of the underlying asset
 * @property underlyingType Underlying type: COM, INDEX, EQUITY, CUR, or IRD
 * @property underlyingSymbol Symbol of underlying asset
 * @property strikePrice Option strike price
 * @property minimumLot Minimum lot size
 * @property weekly Whether this is a weekly expiry contract
 * @see <a href="https://upstox.com/developer/api-documentation/get-option-contracts">Option Contracts API</a>
 */
data class OptionContract(
    val name: String,
    val segment: String,
    val exchange: String,
    val expiry: String,
    @SerializedName("instrument_key")
    val instrumentKey: String,
    @SerializedName("exchange_token")
    val exchangeToken: String,
    @SerializedName("trading_symbol")
    val tradingSymbol: String,
    @SerializedName("tick_size")
    val tickSize: Double,
    @SerializedName("lot_size")
    val lotSize: Int,
    @SerializedName("instrument_type")
    val instrumentType: String,
    @SerializedName("freeze_quantity")
    val freezeQuantity: Int,
    @SerializedName("underlying_key")
    val underlyingKey: String,
    @SerializedName("underlying_type")
    val underlyingType: String,
    @SerializedName("underlying_symbol")
    val underlyingSymbol: String,
    @SerializedName("strike_price")
    val strikePrice: Double,
    @SerializedName("minimum_lot")
    val minimumLot: Int,
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
    val expiry: String,
    val pcr: Double? = null,
    @SerializedName("strike_price")
    val strikePrice: Double,
    @SerializedName("underlying_key")
    val underlyingKey: String,
    @SerializedName("underlying_spot_price")
    val underlyingSpotPrice: Double,
    @SerializedName("call_options")
    val callOptions: OptionData,
    @SerializedName("put_options")
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
    @SerializedName("instrument_key")
    val instrumentKey: String,
    @SerializedName("market_data")
    val marketData: OptionMarketData,
    @SerializedName("option_greeks")
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
    val ltp: Double,
    @SerializedName("close_price")
    val closePrice: Double,
    val volume: Long,
    val oi: Long,
    @SerializedName("bid_price")
    val bidPrice: Double,
    @SerializedName("bid_qty")
    val bidQty: Int,
    @SerializedName("ask_price")
    val askPrice: Double,
    @SerializedName("ask_qty")
    val askQty: Int,
    @SerializedName("prev_oi")
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
    val vega: Double,
    val theta: Double,
    val gamma: Double,
    val delta: Double,
    val iv: Double,
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
 * @property instrumentType Instrument type: CE, PE, or FUT
 * @property freezeQuantity Maximum quantity that can be frozen
 * @property underlyingKey Instrument key of the underlying
 * @property underlyingType Underlying type: COM, INDEX, EQUITY, CUR, or IRD
 * @property underlyingSymbol Symbol of underlying
 * @property strikePrice Option strike price (not present for futures)
 * @property minimumLot Minimum lot size
 * @property weekly Whether this was a weekly expiry contract
 * @see <a href="https://upstox.com/developer/api-documentation/get-expired-option-contracts">Expired Option Contracts API</a>
 */
data class ExpiredContract(
    val name: String,
    val segment: String,
    val exchange: String,
    val expiry: String,
    @SerializedName("instrument_key")
    val instrumentKey: String,
    @SerializedName("exchange_token")
    val exchangeToken: String,
    @SerializedName("trading_symbol")
    val tradingSymbol: String,
    @SerializedName("tick_size")
    val tickSize: Double,
    @SerializedName("lot_size")
    val lotSize: Int,
    @SerializedName("instrument_type")
    val instrumentType: String,
    @SerializedName("freeze_quantity")
    val freezeQuantity: Int,
    @SerializedName("underlying_key")
    val underlyingKey: String,
    @SerializedName("underlying_type")
    val underlyingType: String,
    @SerializedName("underlying_symbol")
    val underlyingSymbol: String,
    @SerializedName("strike_price")
    val strikePrice: Double? = null,
    @SerializedName("minimum_lot")
    val minimumLot: Int,
    val weekly: Boolean? = null
)
