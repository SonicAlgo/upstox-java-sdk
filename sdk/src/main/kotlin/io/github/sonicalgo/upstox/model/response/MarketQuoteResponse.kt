package io.github.sonicalgo.upstox.model.response

import com.fasterxml.jackson.annotation.JsonProperty

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
    @JsonProperty("ohlc")
    val ohlc: OhlcData,

    @JsonProperty("depth")
    val depth: MarketDepth,

    @JsonProperty("timestamp")
    val timestamp: String? = null,

    @JsonProperty("instrument_token")
    val instrumentToken: String,

    @JsonProperty("symbol")
    val symbol: String? = null,

    @JsonProperty("last_price")
    val lastPrice: Double,

    @JsonProperty("volume")
    val volume: Long,

    @JsonProperty("average_price")
    val averagePrice: Double? = null,

    @JsonProperty("oi")
    val oi: Long? = null,

    @JsonProperty("net_change")
    val netChange: Double? = null,

    @JsonProperty("total_buy_quantity")
    val totalBuyQuantity: Long? = null,

    @JsonProperty("total_sell_quantity")
    val totalSellQuantity: Long? = null,

    @JsonProperty("lower_circuit_limit")
    val lowerCircuitLimit: Double? = null,

    @JsonProperty("upper_circuit_limit")
    val upperCircuitLimit: Double? = null,

    @JsonProperty("last_trade_time")
    val lastTradeTime: String? = null,

    @JsonProperty("oi_day_high")
    val oiDayHigh: Long? = null,

    @JsonProperty("oi_day_low")
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
    @JsonProperty("open")
    val open: Double,

    @JsonProperty("high")
    val high: Double,

    @JsonProperty("low")
    val low: Double,

    @JsonProperty("close")
    val close: Double
)

/**
 * Market depth with bid/ask levels.
 *
 * @property buy Top 5 buy orders
 * @property sell Top 5 sell orders
 */
data class MarketDepth(
    @JsonProperty("buy")
    val buy: List<DepthLevel>,

    @JsonProperty("sell")
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
    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("price")
    val price: Double,

    @JsonProperty("orders")
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
    @JsonProperty("last_price")
    val lastPrice: Double,

    @JsonProperty("instrument_token")
    val instrumentToken: String,

    @JsonProperty("ohlc")
    val ohlc: OhlcData? = null,

    @JsonProperty("prev_ohlc")
    val prevOhlc: OhlcV3Data? = null,

    @JsonProperty("live_ohlc")
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
    @JsonProperty("open")
    val open: Double,

    @JsonProperty("high")
    val high: Double,

    @JsonProperty("low")
    val low: Double,

    @JsonProperty("close")
    val close: Double,

    @JsonProperty("volume")
    val volume: Long? = null,

    @JsonProperty("ts")
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
    @JsonProperty("last_price")
    val lastPrice: Double,

    @JsonProperty("instrument_token")
    val instrumentToken: String,

    @JsonProperty("ltq")
    val ltq: Int? = null,

    @JsonProperty("volume")
    val volume: Long? = null,

    @JsonProperty("cp")
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
    @JsonProperty("last_price")
    val lastPrice: Double,

    @JsonProperty("instrument_token")
    val instrumentToken: String,

    @JsonProperty("ltq")
    val ltq: Int? = null,

    @JsonProperty("volume")
    val volume: Long? = null,

    @JsonProperty("cp")
    val cp: Double? = null,

    @JsonProperty("iv")
    val iv: Double? = null,

    @JsonProperty("delta")
    val delta: Double? = null,

    @JsonProperty("gamma")
    val gamma: Double? = null,

    @JsonProperty("theta")
    val theta: Double? = null,

    @JsonProperty("vega")
    val vega: Double? = null,

    @JsonProperty("oi")
    val oi: Long? = null
)
