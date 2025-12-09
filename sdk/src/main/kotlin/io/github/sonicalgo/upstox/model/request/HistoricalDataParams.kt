package io.github.sonicalgo.upstox.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.model.enums.CandleUnit

/**
 * Parameters for getting historical candle data.
 *
 * Provides flexible interval options from minutes to months.
 *
 * Example usage:
 * ```kotlin
 * val histApi = upstox.getHistoricalDataApi()
 *
 * val params = HistoricalCandleParams(
 *     instrumentKey = "NSE_EQ|INE848E01016",
 *     unit = CandleUnit.MINUTES,
 *     interval = 15,
 *     toDate = "2025-01-02",
 *     fromDate = "2025-01-01"
 * )
 * val candles = histApi.getHistoricalCandle(params)
 * ```
 *
 * Interval ranges by unit:
 * - minutes: 1-300
 * - hours: 1-5
 * - days: 1 only
 * - weeks: 1 only
 * - months: 1 only
 *
 * @property instrumentKey The unique identifier for the financial instrument
 * @property unit Timeframe unit: minutes, hours, days, weeks, months
 * @property interval Numeric interval value (valid ranges depend on unit)
 * @property toDate End date (inclusive) in YYYY-MM-DD format
 * @property fromDate Start date in YYYY-MM-DD format (optional)
 * @see <a href="https://upstox.com/developer/api-documentation/v3/get-historical-candle-data">Historical Candle API</a>
 */
data class HistoricalCandleParams(
    @JsonProperty("instrument_key")
    val instrumentKey: String,

    @JsonProperty("unit")
    val unit: CandleUnit,

    @JsonProperty("interval")
    val interval: Int,

    @JsonProperty("to_date")
    val toDate: String,

    @JsonProperty("from_date")
    val fromDate: String? = null
)

/**
 * Parameters for getting intraday candle data.
 *
 * Returns candle data for the current trading day.
 *
 * Example usage:
 * ```kotlin
 * val histApi = upstox.getHistoricalDataApi()
 *
 * val params = IntradayCandleParams(
 *     instrumentKey = "NSE_EQ|INE848E01016",
 *     unit = CandleUnit.MINUTES,
 *     interval = 5
 * )
 * val candles = histApi.getIntradayCandle(params)
 * ```
 *
 * @property instrumentKey The unique identifier for the financial instrument
 * @property unit Timeframe unit: minutes, hours, or days
 * @property interval Numeric interval value (minutes: 1-300, hours: 1-5, days: 1)
 * @see <a href="https://upstox.com/developer/api-documentation/v3/get-intra-day-candle-data">Intraday Candle API</a>
 */
data class IntradayCandleParams(
    @JsonProperty("instrument_key")
    val instrumentKey: String,

    @JsonProperty("unit")
    val unit: CandleUnit,

    @JsonProperty("interval")
    val interval: Int
)

/**
 * Parameters for getting expired historical candle data.
 *
 * Retrieves historical data for expired derivatives contracts.
 *
 * Example usage:
 * ```kotlin
 * val expiredApi = upstox.getExpiredInstrumentsApi()
 *
 * val params = ExpiredHistoricalCandleParams(
 *     expiredInstrumentKey = "NSE_FO|NIFTY22D0117800CE",
 *     interval = "day",
 *     toDate = "2022-11-30",
 *     fromDate = "2022-11-01"
 * )
 * val candles = expiredApi.getExpiredHistoricalCandle(params)
 * ```
 *
 * @property expiredInstrumentKey Unique identifier for the expired instrument with expiry date
 * @property interval Timeframe: 1minute, 3minute, 5minute, 15minute, 30minute, or day
 * @property toDate End date (inclusive) in YYYY-MM-DD format
 * @property fromDate Start date in YYYY-MM-DD format
 * @see <a href="https://upstox.com/developer/api-documentation/get-expired-historical-candle-data">Expired Historical Candle API</a>
 */
data class ExpiredHistoricalCandleParams(
    @JsonProperty("expired_instrument_key")
    val expiredInstrumentKey: String,

    @JsonProperty("interval")
    val interval: String,

    @JsonProperty("to_date")
    val toDate: String,

    @JsonProperty("from_date")
    val fromDate: String
)
