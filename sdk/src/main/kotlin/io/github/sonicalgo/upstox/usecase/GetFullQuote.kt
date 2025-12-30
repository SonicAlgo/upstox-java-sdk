package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.util.parseMapResponse
import io.github.sonicalgo.upstox.validation.Validators

private const val MAX_QUOTE_INSTRUMENTS = 500

/** Gets full market quotes for instruments. */
@JvmSynthetic
internal fun executeGetFullQuote(
    apiClient: ApiClient,
    instrumentKeys: List<String>
): Map<String, FullMarketQuote> {
    Validators.validateListSize(instrumentKeys, MAX_QUOTE_INSTRUMENTS, "getFullQuote")

    val queryParams = mapOf("instrument_key" to instrumentKeys.joinToString(","))
    val rawResponse = apiClient.getRaw(
        endpoint = "/market-quote/quotes",
        queryParams = queryParams,
        overrideBaseUrl = BASE_URL_V2
    )
    return parseMapResponse(rawResponse)
}

/**
 * Full market quote data.
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
    val ohlc: OhlcData?,

    @JsonProperty("depth")
    val depth: MarketDepth?,

    @JsonProperty("timestamp")
    val timestamp: String?,

    @JsonProperty("instrument_token")
    val instrumentToken: String?,

    @JsonProperty("symbol")
    val symbol: String?,

    @JsonProperty("last_price")
    val lastPrice: Double?,

    @JsonProperty("volume")
    val volume: Long?,

    @JsonProperty("average_price")
    val averagePrice: Double?,

    @JsonProperty("oi")
    val oi: Long?,

    @JsonProperty("net_change")
    val netChange: Double?,

    @JsonProperty("total_buy_quantity")
    val totalBuyQuantity: Long?,

    @JsonProperty("total_sell_quantity")
    val totalSellQuantity: Long?,

    @JsonProperty("lower_circuit_limit")
    val lowerCircuitLimit: Double?,

    @JsonProperty("upper_circuit_limit")
    val upperCircuitLimit: Double?,

    @JsonProperty("last_trade_time")
    val lastTradeTime: String?,

    @JsonProperty("oi_day_high")
    val oiDayHigh: Long?,

    @JsonProperty("oi_day_low")
    val oiDayLow: Long?
)

/** OHLC price data. */
data class OhlcData(
    @JsonProperty("open")
    val open: Double?,

    @JsonProperty("high")
    val high: Double?,

    @JsonProperty("low")
    val low: Double?,

    @JsonProperty("close")
    val close: Double?
)

/** Market depth with bid/ask levels. */
data class MarketDepth(
    @JsonProperty("buy")
    val buy: List<DepthLevel>?,

    @JsonProperty("sell")
    val sell: List<DepthLevel>?
)

/** Single depth level in order book. */
data class DepthLevel(
    @JsonProperty("quantity")
    val quantity: Int?,

    @JsonProperty("price")
    val price: Double?,

    @JsonProperty("orders")
    val orders: Int?
)