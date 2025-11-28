package io.github.sonicalgo.upstox.model.response

import com.google.gson.annotations.SerializedName

/**
 * Full market quote data.
 *
 * Contains comprehensive market data including OHLC, depth, and other metrics.
 *
 * @property ohlc OHLC (Open, High, Low, Close) prices
 * @property depth Market depth with top 5 buy/sell orders
 * @property timestamp Feed update timestamp in ISO format
 * @property instrumentToken Instrument key identifier
 * @property symbol Trading symbol
 * @property lastPrice Most recent traded price
 * @property volume Today's trading volume
 * @property averagePrice Session average price
 * @property oi Open interest (for F&O instruments)
 * @property netChange Price change from previous close
 * @property totalBuyQuantity Aggregate bid quantity
 * @property totalSellQuantity Aggregate ask quantity
 * @property lowerCircuitLimit Lower circuit limit price
 * @property upperCircuitLimit Upper circuit limit price
 * @property lastTradeTime Last trade timestamp in milliseconds
 * @property oiDayHigh Daily open interest high
 * @property oiDayLow Daily open interest low
 * @see <a href="https://upstox.com/developer/api-documentation/get-full-market-quote">Full Market Quote API</a>
 */
data class FullMarketQuote(
    val ohlc: OhlcData,
    val depth: MarketDepth,
    val timestamp: String? = null,
    @SerializedName("instrument_token")
    val instrumentToken: String,
    val symbol: String? = null,
    @SerializedName("last_price")
    val lastPrice: Double,
    val volume: Long,
    @SerializedName("average_price")
    val averagePrice: Double? = null,
    val oi: Long? = null,
    @SerializedName("net_change")
    val netChange: Double? = null,
    @SerializedName("total_buy_quantity")
    val totalBuyQuantity: Long? = null,
    @SerializedName("total_sell_quantity")
    val totalSellQuantity: Long? = null,
    @SerializedName("lower_circuit_limit")
    val lowerCircuitLimit: Double? = null,
    @SerializedName("upper_circuit_limit")
    val upperCircuitLimit: Double? = null,
    @SerializedName("last_trade_time")
    val lastTradeTime: String? = null,
    @SerializedName("oi_day_high")
    val oiDayHigh: Long? = null,
    @SerializedName("oi_day_low")
    val oiDayLow: Long? = null
)

/**
 * OHLC price data.
 *
 * @property open Opening price
 * @property high Highest price
 * @property low Lowest price
 * @property close Closing/last price
 */
data class OhlcData(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double
)

/**
 * Market depth with bid/ask levels.
 *
 * @property buy Top 5 buy orders
 * @property sell Top 5 sell orders
 */
data class MarketDepth(
    val buy: List<DepthLevel>,
    val sell: List<DepthLevel>
)

/**
 * Single depth level in order book.
 *
 * @property quantity Order quantity at this level
 * @property price Price at this level
 * @property orders Number of orders at this level
 */
data class DepthLevel(
    val quantity: Int,
    val price: Double,
    val orders: Int
)

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
    @SerializedName("last_price")
    val lastPrice: Double,
    @SerializedName("instrument_token")
    val instrumentToken: String,
    val ohlc: OhlcData? = null,
    @SerializedName("prev_ohlc")
    val prevOhlc: OhlcV3Data? = null,
    @SerializedName("live_ohlc")
    val liveOhlc: OhlcV3Data? = null
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
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long? = null,
    val ts: Long? = null
)

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
    @SerializedName("last_price")
    val lastPrice: Double,
    @SerializedName("instrument_token")
    val instrumentToken: String,
    val ltq: Int? = null,
    val volume: Long? = null,
    val cp: Double? = null
)

/**
 * Option Greeks quote.
 *
 * Contains option pricing metrics and Greeks.
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
    @SerializedName("last_price")
    val lastPrice: Double,
    @SerializedName("instrument_token")
    val instrumentToken: String,
    val ltq: Int? = null,
    val volume: Long? = null,
    val cp: Double? = null,
    val iv: Double? = null,
    val delta: Double? = null,
    val gamma: Double? = null,
    val theta: Double? = null,
    val vega: Double? = null,
    val oi: Long? = null
)
