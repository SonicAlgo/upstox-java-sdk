package io.github.sonicalgo.upstox.model.marketdata

import io.github.sonicalgo.upstox.websocket.FeedMode

/**
 * Market status event received on WebSocket connection.
 *
 * Contains the trading status of all market segments.
 *
 * @property segmentStatus Map of segment name to status (e.g., "NSE_EQ" -> "NORMAL_OPEN")
 * @property timestamp Server timestamp in milliseconds
 */
data class MarketStatusEvent(
    val segmentStatus: Map<String, String>,
    val timestamp: Long
)

/**
 * Last Traded Price and Close tick data.
 *
 * Minimal market data containing only price information.
 * Received when subscribed with [FeedMode.LTPC].
 *
 * @property ltp Last traded price
 * @property lastTradedTime Timestamp of last trade in milliseconds
 * @property lastTradedQty Quantity of last trade
 * @property closePrice Previous day's closing price
 */
data class LtpcTick(
    val ltp: Double,
    val lastTradedTime: Long,
    val lastTradedQty: Long,
    val closePrice: Double
)

/**
 * Bid/Ask quote for market depth.
 *
 * Represents a single level in the order book.
 *
 * @property bidQty Quantity available at bid price
 * @property bidPrice Best bid price at this level
 * @property askQty Quantity available at ask price
 * @property askPrice Best ask price at this level
 */
data class QuoteTick(
    val bidQty: Long,
    val bidPrice: Double,
    val askQty: Long,
    val askPrice: Double
)

/**
 * OHLC (Open-High-Low-Close) candle data.
 *
 * @property interval Candle interval (e.g., "1d", "I1" for 1 minute)
 * @property open Opening price
 * @property high Highest price
 * @property low Lowest price
 * @property close Closing price
 * @property volume Trading volume
 * @property timestamp Candle timestamp in milliseconds
 */
data class OhlcTick(
    val interval: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long,
    val timestamp: Long
)

/**
 * Option Greeks data for derivatives.
 *
 * @property delta Rate of change of option price with respect to underlying price
 * @property theta Rate of change of option price with respect to time (time decay)
 * @property gamma Rate of change of delta with respect to underlying price
 * @property vega Rate of change of option price with respect to volatility
 * @property rho Rate of change of option price with respect to interest rate
 */
data class OptionGreeksData(
    val delta: Double,
    val theta: Double,
    val gamma: Double,
    val vega: Double,
    val rho: Double
)

/**
 * Full market feed tick for stocks and F&O instruments.
 *
 * Contains complete market data including depth, OHLC, and option greeks.
 * Received when subscribed with [FeedMode.FULL]
 * or [FeedMode.FULL_D30].
 *
 * @property ltpc Last traded price and close data
 * @property marketDepth List of bid/ask quotes (5 levels for FULL, 30 for FULL_D30)
 * @property optionGreeks Option greeks data (null for equity instruments)
 * @property ohlc OHLC candle data
 * @property atp Average traded price
 * @property volumeTradedToday Total volume traded today
 * @property openInterest Open interest (for derivatives)
 * @property impliedVolatility Implied volatility (for options)
 * @property totalBuyQty Total buy quantity in order book
 * @property totalSellQty Total sell quantity in order book
 * @property depthLevels Number of market depth levels (5 or 30)
 */
data class FullFeedTick(
    val ltpc: LtpcTick,
    val marketDepth: List<QuoteTick>,
    val optionGreeks: OptionGreeksData?,
    val ohlc: List<OhlcTick>,
    val atp: Double,
    val volumeTradedToday: Long,
    val openInterest: Double,
    val impliedVolatility: Double,
    val totalBuyQty: Double,
    val totalSellQty: Double,
    val depthLevels: Int
)

/**
 * Full market feed tick for index instruments.
 *
 * Contains price and OHLC data for indices (no market depth available).
 * Received when subscribed to an index with [FeedMode.FULL].
 *
 * @property ltpc Last traded price and close data
 * @property ohlc OHLC candle data
 */
data class IndexFeedTick(
    val ltpc: LtpcTick,
    val ohlc: List<OhlcTick>
)

/**
 * Option Greeks feed tick with first-level market depth.
 *
 * Contains option greeks with minimal market depth.
 * Received when subscribed with [FeedMode.OPTION_GREEKS].
 *
 * @property ltpc Last traded price and close data
 * @property firstDepth First level bid/ask quote
 * @property optionGreeks Option greeks data
 * @property volumeTradedToday Total volume traded today
 * @property openInterest Open interest
 * @property impliedVolatility Implied volatility
 */
data class OptionGreeksTick(
    val ltpc: LtpcTick,
    val firstDepth: QuoteTick,
    val optionGreeks: OptionGreeksData,
    val volumeTradedToday: Long,
    val openInterest: Double,
    val impliedVolatility: Double
)
