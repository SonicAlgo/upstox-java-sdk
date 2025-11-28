package io.github.sonicalgo.upstox.api

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants
import io.github.sonicalgo.upstox.exception.UpstoxApiException
import io.github.sonicalgo.upstox.exception.UpstoxPlusRequiredException
import io.github.sonicalgo.upstox.model.request.ExpiredHistoricalCandleParams
import io.github.sonicalgo.upstox.model.response.CandleData
import io.github.sonicalgo.upstox.model.response.ExpiredContract
import java.net.URLEncoder

/**
 * API module for expired instruments data.
 *
 * Provides methods for retrieving information about expired derivative contracts
 * and their historical data. Requires Upstox Plus subscription.
 *
 * Example usage:
 * ```kotlin
 * val upstox = Upstox.getInstance()
 *
 * // Get available expiries
 * val expiries = upstox.getExpiredInstrumentsApi().getExpiries("NSE_INDEX|Nifty 50")
 * println("Available expiries: $expiries")
 *
 * // Get expired option contracts
 * val contracts = upstox.getExpiredInstrumentsApi().getExpiredOptionContracts(
 *     instrumentKey = "NSE_INDEX|Nifty 50",
 *     expiryDate = "2024-10-03"
 * )
 *
 * // Get historical data for expired contract
 * val candles = upstox.getExpiredInstrumentsApi().getExpiredHistoricalCandle(ExpiredHistoricalCandleParams(
 *     expiredInstrumentKey = "NSE_FO|NIFTY22D0117800CE",
 *     interval = "day",
 *     toDate = "2022-11-30",
 *     fromDate = "2022-11-01"
 * ))
 * ```
 *
 * @see <a href="https://upstox.com/developer/api-documentation/get-expiries">Get Expiries API</a>
 */
class ExpiredInstrumentsApi private constructor() {

    /**
     * Gets available expiry dates for expired instruments.
     *
     * Returns up to six months of historical expiries for the underlying.
     * Currently unavailable for MCX instruments.
     *
     * Example:
     * ```kotlin
     * val expiredApi = upstox.getExpiredInstrumentsApi()
     *
     * val expiries = expiredApi.getExpiries("NSE_INDEX|Nifty 50")
     * expiries.forEach { expiryDate ->
     *     println("Expiry: $expiryDate")
     * }
     * ```
     *
     * @param instrumentKey Key of the underlying instrument
     * @return List of expiry date strings in YYYY-MM-dd format
     * @throws UpstoxApiException if retrieval fails
     * @throws UpstoxPlusRequiredException if subscription is not active
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-expiries">Get Expiries API</a>
     */
    fun getExpiries(instrumentKey: String): List<String> {
        return ApiClient.get(
            endpoint = Endpoints.GET_EXPIRIES,
            queryParams = mapOf("instrument_key" to instrumentKey),
            baseUrl = UpstoxConstants.BASE_URL_V2
        )
    }

    /**
     * Gets expired option contracts for an underlying and expiry date.
     *
     * Returns all option contracts that expired on the specified date.
     *
     * Example:
     * ```kotlin
     * val expiredApi = upstox.getExpiredInstrumentsApi()
     *
     * val contracts = expiredApi.getExpiredOptionContracts(
     *     instrumentKey = "NSE_INDEX|Nifty 50",
     *     expiryDate = "2024-10-03"
     * )
     * contracts.forEach { contract ->
     *     println("${contract.tradingSymbol}")
     *     println("  Type: ${contract.instrumentType}")
     *     println("  Strike: ${contract.strikePrice}")
     *     println("  Expired Key: ${contract.instrumentKey}")
     * }
     * ```
     *
     * @param instrumentKey Key of the underlying instrument
     * @param expiryDate Expiry date in YYYY-MM-DD format
     * @return List of [ExpiredContract] expired option contracts
     * @throws UpstoxApiException if retrieval fails
     * @throws UpstoxPlusRequiredException if subscription is not active
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-expired-option-contracts">Expired Option Contracts API</a>
     */
    fun getExpiredOptionContracts(instrumentKey: String, expiryDate: String): List<ExpiredContract> {
        return ApiClient.get(
            endpoint = Endpoints.GET_EXPIRED_OPTION_CONTRACTS,
            queryParams = mapOf("instrument_key" to instrumentKey, "expiry_date" to expiryDate),
            baseUrl = UpstoxConstants.BASE_URL_V2
        )
    }

    /**
     * Gets expired future contracts for an underlying and expiry date.
     *
     * Returns all future contracts that expired on the specified date.
     *
     * Example:
     * ```kotlin
     * val expiredApi = upstox.getExpiredInstrumentsApi()
     *
     * val contracts = expiredApi.getExpiredFutureContracts(
     *     instrumentKey = "NSE_INDEX|Nifty 50",
     *     expiryDate = "2024-11-27"
     * )
     * contracts.forEach { contract ->
     *     println("${contract.tradingSymbol}")
     *     println("  Expired Key: ${contract.instrumentKey}")
     *     println("  Lot Size: ${contract.lotSize}")
     * }
     * ```
     *
     * @param instrumentKey Key of the underlying instrument
     * @param expiryDate Expiry date in YYYY-MM-DD format
     * @return List of [ExpiredContract] expired future contracts
     * @throws UpstoxApiException if retrieval fails
     * @throws UpstoxPlusRequiredException if subscription is not active
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-expired-future-contracts">Expired Future Contracts API</a>
     */
    fun getExpiredFutureContracts(instrumentKey: String, expiryDate: String): List<ExpiredContract> {
        return ApiClient.get(
            endpoint = Endpoints.GET_EXPIRED_FUTURE_CONTRACTS,
            queryParams = mapOf("instrument_key" to instrumentKey, "expiry_date" to expiryDate),
            baseUrl = UpstoxConstants.BASE_URL_V2
        )
    }

    /**
     * Gets historical candle data for an expired instrument.
     *
     * Returns OHLCV data for contracts that have already expired.
     *
     * Supported intervals: 1minute, 3minute, 5minute, 15minute, 30minute, day
     *
     * Example:
     * ```kotlin
     * val expiredApi = upstox.getExpiredInstrumentsApi()
     *
     * val candles = expiredApi.getExpiredHistoricalCandle(ExpiredHistoricalCandleParams(
     *     expiredInstrumentKey = "NSE_FO|NIFTY22D0117800CE",
     *     interval = "day",
     *     toDate = "2022-11-30",
     *     fromDate = "2022-11-01"
     * ))
     * candles.toCandles().forEach { candle ->
     *     println("${candle.timestamp}: O=${candle.open} H=${candle.high} L=${candle.low} C=${candle.close}")
     * }
     * ```
     *
     * @param params Historical candle query parameters
     * @return [CandleData] for the expired instrument
     * @throws UpstoxApiException if retrieval fails
     * @throws UpstoxPlusRequiredException if subscription is not active
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-expired-historical-candle-data">Expired Historical Candle API</a>
     */
    fun getExpiredHistoricalCandle(params: ExpiredHistoricalCandleParams): CandleData {
        val encodedKey = URLEncoder.encode(params.expiredInstrumentKey, "UTF-8")
        val endpoint = "${Endpoints.GET_EXPIRED_HISTORICAL_CANDLE_BASE}/$encodedKey/${params.interval}/${params.toDate}/${params.fromDate}"
        return ApiClient.get(endpoint = endpoint, baseUrl = UpstoxConstants.BASE_URL_V2)
    }

    internal object Endpoints {
        const val GET_EXPIRIES = "/expired-instruments/expiries"
        const val GET_EXPIRED_OPTION_CONTRACTS = "/expired-instruments/option/contract"
        const val GET_EXPIRED_FUTURE_CONTRACTS = "/expired-instruments/future/contract"
        const val GET_EXPIRED_HISTORICAL_CANDLE_BASE = "/expired-instruments/historical-candle"
    }

    companion object {
        internal val instance by lazy { ExpiredInstrumentsApi() }
    }
}
