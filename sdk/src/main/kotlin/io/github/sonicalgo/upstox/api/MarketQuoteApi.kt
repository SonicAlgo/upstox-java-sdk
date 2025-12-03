package io.github.sonicalgo.upstox.api

import com.google.gson.JsonObject
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants
import io.github.sonicalgo.upstox.exception.UpstoxApiException
import io.github.sonicalgo.upstox.model.enums.OhlcInterval
import io.github.sonicalgo.upstox.model.response.FullMarketQuote
import io.github.sonicalgo.upstox.model.response.LtpQuote
import io.github.sonicalgo.upstox.model.response.OhlcQuote
import io.github.sonicalgo.upstox.model.response.OptionGreeksQuote
import io.github.sonicalgo.upstox.validation.Validators

/**
 * API module for market quotes.
 *
 * Provides methods for retrieving real-time market data including
 * full quotes, OHLC, LTP, and option Greeks.
 *
 * Example usage:
 * ```kotlin
 * val upstox = Upstox.getInstance()
 *
 * // Get full market quote
 * val quotes = upstox.getMarketQuoteApi().getFullQuote(listOf("NSE_EQ|INE669E01016"))
 * quotes.forEach { (key, quote) ->
 *     println("$key: LTP=${quote.lastPrice}, Volume=${quote.volume}")
 * }
 *
 * // Get LTP
 * val ltpQuotes = upstox.getMarketQuoteApi().getLtp(listOf("NSE_EQ|INE669E01016"))
 * ```
 *
 * @see <a href="https://upstox.com/developer/api-documentation/get-full-market-quote">Full Market Quote API</a>
 */
class MarketQuoteApi private constructor() {

    /**
     * Gets full market quotes for instruments.
     *
     * Returns comprehensive market data including OHLC, depth,
     * volume, OI, and circuit limits. Maximum 500 instruments per request.
     *
     * Example:
     * ```kotlin
     * val marketQuoteApi = upstox.getMarketQuoteApi()
     *
     * val quotes = marketQuoteApi.getFullQuote(listOf(
     *     "NSE_EQ|INE669E01016",
     *     "NSE_EQ|INE002A01018"
     * ))
     * quotes.forEach { (instrumentKey, quote) ->
     *     println("$instrumentKey:")
     *     println("  LTP: ${quote.lastPrice}")
     *     println("  OHLC: O=${quote.ohlc.open} H=${quote.ohlc.high} L=${quote.ohlc.low} C=${quote.ohlc.close}")
     *     println("  Volume: ${quote.volume}")
     *     println("  Circuit: ${quote.lowerCircuitLimit} - ${quote.upperCircuitLimit}")
     * }
     * ```
     *
     * @param instrumentKeys List of instrument keys (max 500)
     * @return Map of instrument key to [FullMarketQuote]
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-full-market-quote">Full Market Quote API</a>
     */
    fun getFullQuote(instrumentKeys: List<String>): Map<String, FullMarketQuote> {
        Validators.validateListSize(instrumentKeys, MAX_QUOTE_INSTRUMENTS, "getFullQuote")

        val queryParams = mapOf("instrument_key" to instrumentKeys.joinToString(","))
        val rawResponse = ApiClient.getRaw(
            endpoint = Endpoints.GET_FULL_QUOTE,
            queryParams = queryParams,
            baseUrl = UpstoxConstants.BASE_URL_V2
        )
        return parseMapResponse(rawResponse)
    }

    /**
     * Gets OHLC quotes.
     *
     * Returns OHLC data with both previous and live candle information.
     *
     * Example:
     * ```kotlin
     * val marketQuoteApi = upstox.getMarketQuoteApi()
     *
     * val quotes = marketQuoteApi.getOhlcQuote(
     *     instrumentKeys = listOf("NSE_EQ|INE669E01016"),
     *     interval = OhlcInterval.ONE_DAY
     * )
     * quotes.forEach { (key, quote) ->
     *     println("$key: LTP=${quote.lastPrice}")
     *     println("  Live: O=${quote.liveOhlc?.open} C=${quote.liveOhlc?.close}")
     *     println("  Prev: O=${quote.prevOhlc?.open} C=${quote.prevOhlc?.close}")
     * }
     * ```
     *
     * @param instrumentKeys List of instrument keys (max 500)
     * @param interval OHLC interval (1d, I1, I30)
     * @return Map of instrument key to [OhlcQuote]
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-market-quote-ohlc-v3">OHLC Quote API</a>
     */
    fun getOhlcQuote(instrumentKeys: List<String>, interval: OhlcInterval): Map<String, OhlcQuote> {
        Validators.validateListSize(instrumentKeys, MAX_QUOTE_INSTRUMENTS, "getOhlcQuote")

        val queryParams = mapOf(
            "instrument_key" to instrumentKeys.joinToString(","),
            "interval" to interval.toString()
        )
        val rawResponse = ApiClient.getRaw(
            endpoint = Endpoints.GET_OHLC_QUOTE,
            queryParams = queryParams,
            baseUrl = UpstoxConstants.BASE_URL_V3
        )
        return parseMapResponse(rawResponse)
    }

    /**
     * Gets LTP quotes.
     *
     * Returns last traded price along with volume and previous close.
     *
     * Example:
     * ```kotlin
     * val marketQuoteApi = upstox.getMarketQuoteApi()
     *
     * val quotes = marketQuoteApi.getLtp(listOf("NSE_EQ|INE669E01016"))
     * quotes.forEach { (key, quote) ->
     *     println("$key: LTP=${quote.lastPrice}, Volume=${quote.volume}, PrevClose=${quote.cp}")
     * }
     * ```
     *
     * @param instrumentKeys List of instrument keys (max 500)
     * @return Map of instrument key to [LtpQuote]
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/ltp-v3">LTP API</a>
     */
    fun getLtp(instrumentKeys: List<String>): Map<String, LtpQuote> {
        Validators.validateListSize(instrumentKeys, MAX_QUOTE_INSTRUMENTS, "getLtp")

        val queryParams = mapOf("instrument_key" to instrumentKeys.joinToString(","))
        val rawResponse = ApiClient.getRaw(
            endpoint = Endpoints.GET_LTP,
            queryParams = queryParams,
            baseUrl = UpstoxConstants.BASE_URL_V3
        )
        return parseMapResponse(rawResponse)
    }

    /**
     * Gets option Greeks for instruments.
     *
     * Returns option pricing metrics including delta, gamma, theta, vega, and IV.
     * Maximum 50 instruments per request.
     *
     * Example:
     * ```kotlin
     * val marketQuoteApi = upstox.getMarketQuoteApi()
     *
     * val greeks = marketQuoteApi.getOptionGreeks(listOf("NSE_FO|43885"))
     * greeks.forEach { (key, quote) ->
     *     println("$key:")
     *     println("  LTP: ${quote.lastPrice}")
     *     println("  IV: ${quote.iv}")
     *     println("  Delta: ${quote.delta}")
     *     println("  Gamma: ${quote.gamma}")
     *     println("  Theta: ${quote.theta}")
     *     println("  Vega: ${quote.vega}")
     * }
     * ```
     *
     * @param instrumentKeys List of option instrument keys (max 50)
     * @return Map of instrument key to [OptionGreeksQuote]
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/option-greek">Option Greek API</a>
     */
    fun getOptionGreeks(instrumentKeys: List<String>): Map<String, OptionGreeksQuote> {
        val queryParams = mapOf("instrument_key" to instrumentKeys.joinToString(","))
        val rawResponse = ApiClient.getRaw(
            endpoint = Endpoints.GET_OPTION_GREEKS,
            queryParams = queryParams,
            baseUrl = UpstoxConstants.BASE_URL_V3
        )
        return parseMapResponse(rawResponse)
    }

    private inline fun <reified T> parseMapResponse(rawResponse: String): Map<String, T> {
        val jsonObject = ApiClient.gson.fromJson(rawResponse, JsonObject::class.java)
        val dataObject = jsonObject.getAsJsonObject("data")
        val result = mutableMapOf<String, T>()

        dataObject?.entrySet()?.forEach { entry ->
            val value = ApiClient.gson.fromJson(entry.value, T::class.java)
            result[entry.key] = value
        }

        return result
    }

    internal object Endpoints {
        const val GET_FULL_QUOTE = "/market-quote/quotes"
        const val GET_OHLC_QUOTE = "/market-quote/ohlc"
        const val GET_LTP = "/market-quote/ltp"
        const val GET_OPTION_GREEKS = "/market-quote/option-greek"
    }

    companion object {
        /** Maximum number of instruments for quote APIs */
        private const val MAX_QUOTE_INSTRUMENTS = 500

        internal val instance by lazy { MarketQuoteApi() }
    }
}
