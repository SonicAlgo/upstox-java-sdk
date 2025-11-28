package io.github.sonicalgo.upstox.model.request

import com.google.gson.annotations.SerializedName
import io.github.sonicalgo.upstox.model.enums.CandleInterval
import io.github.sonicalgo.upstox.model.enums.CandleUnit

/**
 * Parameters for getting historical candle data (V2 API - Deprecated).
 *
 * Retrieves OHLC candle data for a specified instrument and time range.
 *
 * Example usage:
 * ```kotlin
 * val histApi = upstox.getHistoricalDataApi()
 *
 * val params = HistoricalCandleParams(
 *     instrumentKey = "NSE_EQ|INE848E01016",
 *     interval = CandleInterval.DAY,
 *     toDate = "2023-11-13",
 *     fromDate = "2023-11-01"
 * )
 * val candles = histApi.getHistoricalCandle(params)
 * ```
 *
 * @property instrumentKey The unique identifier for the financial instrument
 * @property interval Timeframe options: 1minute, 30minute, day, week, month
 * @property toDate End date (inclusive) in YYYY-MM-DD format
 * @property fromDate Start date in YYYY-MM-DD format (optional)
 * @see <a href="https://upstox.com/developer/api-documentation/get-historical-candle-data">Historical Candle API</a>
 */
data class HistoricalCandleParams(
    @SerializedName("instrument_key")
    val instrumentKey: String,
    val interval: CandleInterval,
    @SerializedName("to_date")
    val toDate: String,
    @SerializedName("from_date")
    val fromDate: String? = null
)

/**
 * Parameters for getting intraday candle data (V2 API - Deprecated).
 *
 * Retrieves intraday OHLC candle data for the current day.
 *
 * Example usage:
 * ```kotlin
 * val histApi = upstox.getHistoricalDataApi()
 *
 * val params = IntradayCandleParams(
 *     instrumentKey = "NSE_EQ|INE848E01016",
 *     interval = CandleInterval.ONE_MINUTE
 * )
 * val candles = histApi.getIntradayCandle(params)
 * ```
 *
 * @property instrumentKey The unique identifier for the financial instrument
 * @property interval Timeframe: 1minute or 30minute
 * @see <a href="https://upstox.com/developer/api-documentation/get-intra-day-candle-data">Intraday Candle API</a>
 */
data class IntradayCandleParams(
    @SerializedName("instrument_key")
    val instrumentKey: String,
    val interval: CandleInterval
)

/**
 * Parameters for getting historical candle data (V3 API).
 *
 * The V3 API provides more flexible interval options.
 *
 * Example usage:
 * ```kotlin
 * val histApi = upstox.getHistoricalDataApi()
 *
 * val params = HistoricalCandleV3Params(
 *     instrumentKey = "NSE_EQ|INE848E01016",
 *     unit = CandleUnit.MINUTES,
 *     interval = 15,
 *     toDate = "2025-01-02",
 *     fromDate = "2025-01-01"
 * )
 * val candles = histApi.getHistoricalCandleV3(params)
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
 * @see <a href="https://upstox.com/developer/api-documentation/v3/get-historical-candle-data">Historical Candle V3 API</a>
 */
data class HistoricalCandleV3Params(
    @SerializedName("instrument_key")
    val instrumentKey: String,
    val unit: CandleUnit,
    val interval: Int,
    @SerializedName("to_date")
    val toDate: String,
    @SerializedName("from_date")
    val fromDate: String? = null
)

/**
 * Parameters for getting intraday candle data (V3 API).
 *
 * Example usage:
 * ```kotlin
 * val histApi = upstox.getHistoricalDataApi()
 *
 * val params = IntradayCandleV3Params(
 *     instrumentKey = "NSE_EQ|INE848E01016",
 *     unit = CandleUnit.MINUTES,
 *     interval = 5
 * )
 * val candles = histApi.getIntradayCandleV3(params)
 * ```
 *
 * @property instrumentKey The unique identifier for the financial instrument
 * @property unit Timeframe unit: minutes, hours, or days
 * @property interval Numeric interval value (minutes: 1-300, hours: 1-5, days: 1)
 * @see <a href="https://upstox.com/developer/api-documentation/v3/get-intra-day-candle-data">Intraday Candle V3 API</a>
 */
data class IntradayCandleV3Params(
    @SerializedName("instrument_key")
    val instrumentKey: String,
    val unit: CandleUnit,
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
    @SerializedName("expired_instrument_key")
    val expiredInstrumentKey: String,
    val interval: String,
    @SerializedName("to_date")
    val toDate: String,
    @SerializedName("from_date")
    val fromDate: String
)
