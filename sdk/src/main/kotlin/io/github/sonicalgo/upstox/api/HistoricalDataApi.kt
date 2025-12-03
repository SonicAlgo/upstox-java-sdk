package io.github.sonicalgo.upstox.api

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants
import io.github.sonicalgo.upstox.exception.UpstoxApiException
import io.github.sonicalgo.upstox.model.request.HistoricalCandleParams
import io.github.sonicalgo.upstox.model.request.IntradayCandleParams
import io.github.sonicalgo.upstox.model.response.CandleData
import java.net.URLEncoder

/**
 * API module for historical candle data.
 *
 * Provides methods for retrieving OHLC (Open, High, Low, Close) candle data
 * for both historical and intraday periods.
 *
 * Example usage:
 * ```kotlin
 * val upstox = Upstox.getInstance()
 *
 * // Get daily historical data
 * val candles = upstox.getHistoricalDataApi().getHistoricalCandle(HistoricalCandleParams(
 *     instrumentKey = "NSE_EQ|INE848E01016",
 *     unit = CandleUnit.DAYS,
 *     interval = 1,
 *     toDate = "2025-01-15",
 *     fromDate = "2025-01-01"
 * ))
 *
 * // Get intraday data
 * val intradayCandles = upstox.getHistoricalDataApi().getIntradayCandle(IntradayCandleParams(
 *     instrumentKey = "NSE_EQ|INE848E01016",
 *     unit = CandleUnit.MINUTES,
 *     interval = 5
 * ))
 * ```
 *
 * @see <a href="https://upstox.com/developer/api-documentation/v3/get-historical-candle-data">Historical Candle API</a>
 */
class HistoricalDataApi private constructor() {

    /**
     * Gets historical candle data.
     *
     * Provides flexible interval options from minutes to months.
     *
     * Data availability:
     * - minutes: From January 2022, max 1 month (1-15 min) or 1 quarter (>15 min)
     * - hours: From January 2022, max 1 quarter
     * - days: From January 2000, max 1 decade
     * - weeks/months: From January 2000, no limit
     *
     * Example:
     * ```kotlin
     * val histApi = upstox.getHistoricalDataApi()
     *
     * val candles = histApi.getHistoricalCandle(HistoricalCandleParams(
     *     instrumentKey = "NSE_EQ|INE848E01016",
     *     unit = CandleUnit.MINUTES,
     *     interval = 15,
     *     toDate = "2025-01-15",
     *     fromDate = "2025-01-01"
     * ))
     * candles.toCandles().forEach { candle ->
     *     println("${candle.timestamp}: O=${candle.open} H=${candle.high} L=${candle.low} C=${candle.close}")
     * }
     * ```
     *
     * @param params Historical candle query parameters
     * @return [CandleData] containing OHLCV arrays
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/v3/get-historical-candle-data">Historical Candle API</a>
     */
    fun getHistoricalCandle(params: HistoricalCandleParams): CandleData {
        val encodedKey = URLEncoder.encode(params.instrumentKey, "UTF-8")
        val endpoint = buildString {
            append("${Endpoints.HISTORICAL_CANDLE_BASE}/$encodedKey/${params.unit}/${params.interval}/${params.toDate}")
            params.fromDate?.let { append("/$it") }
        }

        return ApiClient.get(endpoint = endpoint, baseUrl = UpstoxConstants.BASE_URL_V3)
    }

    /**
     * Gets intraday candle data.
     *
     * Returns candle data for the current trading day.
     *
     * Example:
     * ```kotlin
     * val histApi = upstox.getHistoricalDataApi()
     *
     * val candles = histApi.getIntradayCandle(IntradayCandleParams(
     *     instrumentKey = "NSE_EQ|INE848E01016",
     *     unit = CandleUnit.MINUTES,
     *     interval = 1
     * ))
     * candles.toCandles().forEach { candle ->
     *     println("${candle.timestamp}: ${candle.close} (Vol: ${candle.volume})")
     * }
     * ```
     *
     * @param params Intraday candle query parameters
     * @return [CandleData] for current day
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/v3/get-intra-day-candle-data">Intraday Candle API</a>
     */
    fun getIntradayCandle(params: IntradayCandleParams): CandleData {
        val encodedKey = URLEncoder.encode(params.instrumentKey, "UTF-8")
        val endpoint = "${Endpoints.INTRADAY_CANDLE_BASE}/$encodedKey/${params.unit}/${params.interval}"
        return ApiClient.get(endpoint = endpoint, baseUrl = UpstoxConstants.BASE_URL_V3)
    }

    internal object Endpoints {
        const val HISTORICAL_CANDLE_BASE = "/historical-candle"
        const val INTRADAY_CANDLE_BASE = "/historical-candle/intraday"
    }

    companion object {
        internal val instance by lazy { HistoricalDataApi() }
    }
}
