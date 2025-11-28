package io.github.sonicalgo.upstox.websocket

import io.github.sonicalgo.upstox.model.marketdata.*
import io.github.sonicalgo.upstox.websocket.proto.MarketDataFeedProto.*

/**
 * Extension functions to convert protobuf messages to Kotlin data classes.
 */

/**
 * Converts protobuf LTPC to [LtpcTick].
 */
internal fun LTPC.toLtpcTick() = LtpcTick(
    ltp = ltp,
    lastTradedTime = ltt,
    lastTradedQty = ltq,
    closePrice = cp
)

/**
 * Converts protobuf Quote to [QuoteTick].
 */
internal fun Quote.toQuoteTick() = QuoteTick(
    bidQty = bidQ,
    bidPrice = bidP,
    askQty = askQ,
    askPrice = askP
)

/**
 * Converts protobuf OHLC to [OhlcTick].
 */
internal fun OHLC.toOhlcTick() = OhlcTick(
    interval = interval,
    open = open,
    high = high,
    low = low,
    close = close,
    volume = vol,
    timestamp = ts
)

/**
 * Converts protobuf OptionGreeks to [OptionGreeksData].
 */
internal fun OptionGreeks.toOptionGreeksData() = OptionGreeksData(
    delta = delta,
    theta = theta,
    gamma = gamma,
    vega = vega,
    rho = rho
)

/**
 * Converts protobuf MarketFullFeed to [FullFeedTick].
 *
 * @param mode The request mode to determine depth levels (5 for full_d5, 30 for full_d30)
 */
internal fun MarketFullFeed.toFullFeedTick(mode: RequestMode) = FullFeedTick(
    ltpc = ltpc.toLtpcTick(),
    marketDepth = marketLevel.bidAskQuoteList.map { it.toQuoteTick() },
    optionGreeks = if (hasOptionGreeks()) optionGreeks.toOptionGreeksData() else null,
    ohlc = marketOHLC.ohlcList.map { it.toOhlcTick() },
    atp = atp,
    volumeTradedToday = vtt,
    openInterest = oi,
    impliedVolatility = iv,
    totalBuyQty = tbq,
    totalSellQty = tsq,
    depthLevels = if (mode == RequestMode.full_d30) 30 else 5
)

/**
 * Converts protobuf IndexFullFeed to [IndexFeedTick].
 */
internal fun IndexFullFeed.toIndexFeedTick() = IndexFeedTick(
    ltpc = ltpc.toLtpcTick(),
    ohlc = marketOHLC.ohlcList.map { it.toOhlcTick() }
)

/**
 * Converts protobuf FirstLevelWithGreeks to [OptionGreeksTick].
 */
internal fun FirstLevelWithGreeks.toOptionGreeksTick() = OptionGreeksTick(
    ltpc = ltpc.toLtpcTick(),
    firstDepth = firstDepth.toQuoteTick(),
    optionGreeks = optionGreeks.toOptionGreeksData(),
    volumeTradedToday = vtt,
    openInterest = oi,
    impliedVolatility = iv
)

/**
 * Converts protobuf MarketInfo segment status map to a String map.
 */
internal fun MarketInfo.toSegmentStatusMap(): Map<String, String> =
    segmentStatusMap.mapValues { (_, status) -> status.name }
