package io.github.sonicalgo.upstox.api

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants
import io.github.sonicalgo.upstox.exception.UpstoxApiException
import io.github.sonicalgo.upstox.model.response.MarketHoliday
import io.github.sonicalgo.upstox.model.response.MarketStatusResponse
import io.github.sonicalgo.upstox.model.response.MarketTiming

/**
 * API module for market information.
 *
 * Provides methods for retrieving market holidays, timings, and status.
 *
 * Example usage:
 * ```kotlin
 * val upstox = Upstox.getInstance()
 *
 * // Get all holidays for the year
 * val holidays = upstox.getMarketInfoApi().getMarketHolidays()
 * holidays.forEach { holiday ->
 *     println("${holiday.date}: ${holiday.description} (${holiday.holidayType})")
 * }
 *
 * // Get market status
 * val status = upstox.getMarketInfoApi().getMarketStatus("NSE")
 * println("NSE Status: ${status.status}")
 * ```
 *
 * @see <a href="https://upstox.com/developer/api-documentation/get-market-holidays">Market Holidays API</a>
 */
class MarketInfoApi private constructor() {

    /**
     * Gets all market holidays.
     *
     * Returns a list of all holidays including trading holidays,
     * settlement holidays, and special timing days.
     *
     * Example:
     * ```kotlin
     * val marketInfoApi = upstox.getMarketInfoApi()
     *
     * val holidays = marketInfoApi.getMarketHolidays()
     * holidays.forEach { holiday ->
     *     println("${holiday.date}: ${holiday.description}")
     *     println("  Type: ${holiday.holidayType}")
     *     holiday.closedExchanges?.let { println("  Closed: $it") }
     *     holiday.openExchanges?.forEach { timing ->
     *         println("  Open: ${timing.exchange} (${timing.startTime} - ${timing.endTime})")
     *     }
     * }
     * ```
     *
     * @return List of [MarketHoliday] entries
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-market-holidays">Market Holidays API</a>
     */
    fun getMarketHolidays(): List<MarketHoliday> {
        return ApiClient.get(endpoint = Endpoints.GET_MARKET_HOLIDAYS, baseUrl = UpstoxConstants.BASE_URL_V2)
    }

    /**
     * Gets market holiday information for a specific date.
     *
     * Example:
     * ```kotlin
     * val marketInfoApi = upstox.getMarketInfoApi()
     *
     * val holiday = marketInfoApi.getMarketHoliday("2024-01-26")
     * holiday?.let {
     *     println("${it.description} - ${it.holidayType}")
     * }
     * ```
     *
     * @param date Date in YYYY-MM-DD format
     * @return [MarketHoliday] if the date is a holiday, null otherwise
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-market-holidays">Market Holidays API</a>
     */
    fun getMarketHoliday(date: String): MarketHoliday? {
        val holidays: List<MarketHoliday> = ApiClient.get(
            endpoint = "${Endpoints.GET_MARKET_HOLIDAYS}/$date",
            baseUrl = UpstoxConstants.BASE_URL_V2
        )
        return holidays.firstOrNull()
    }

    /**
     * Gets market timings for a specific date.
     *
     * Returns trading hours for all exchanges on the specified date.
     *
     * Example:
     * ```kotlin
     * val marketInfoApi = upstox.getMarketInfoApi()
     *
     * val timings = marketInfoApi.getMarketTimings("2024-01-22")
     * timings.forEach { timing ->
     *     println("${timing.exchange}: ${timing.startTime} - ${timing.endTime}")
     * }
     * ```
     *
     * @param date Date in YYYY-MM-DD format
     * @return List of [MarketTiming] for each exchange
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-market-timings">Market Timings API</a>
     */
    fun getMarketTimings(date: String): List<MarketTiming> {
        return ApiClient.get(endpoint = "${Endpoints.GET_MARKET_TIMINGS_BASE}/$date", baseUrl = UpstoxConstants.BASE_URL_V2)
    }

    /**
     * Gets the current market status for an exchange.
     *
     * Returns the current trading status of the exchange.
     *
     * Possible status values:
     * - NORMAL_OPEN: Normal trading session open
     * - NORMAL_CLOSE: Normal trading session closed
     * - PRE_OPEN_START: Pre-open session started
     * - PRE_OPEN_END: Pre-open session ended
     * - CLOSING_START: Closing session started
     * - CLOSING_END: Closing session ended
     *
     * Example:
     * ```kotlin
     * val marketInfoApi = upstox.getMarketInfoApi()
     *
     * val status = marketInfoApi.getMarketStatus("NSE")
     * println("Exchange: ${status.exchange}")
     * println("Status: ${status.status}")
     * println("Last Updated: ${status.lastUpdated}")
     * ```
     *
     * @param exchange Exchange identifier (NSE, BSE, etc.)
     * @return [MarketStatusResponse] Market status information
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-market-status">Market Status API</a>
     */
    fun getMarketStatus(exchange: String): MarketStatusResponse {
        return ApiClient.get(endpoint = "${Endpoints.GET_MARKET_STATUS_BASE}/$exchange", baseUrl = UpstoxConstants.BASE_URL_V2)
    }

    internal object Endpoints {
        const val GET_MARKET_HOLIDAYS = "/market/holidays"
        const val GET_MARKET_TIMINGS_BASE = "/market/timings"
        const val GET_MARKET_STATUS_BASE = "/market/status"
    }

    companion object {
        internal val instance by lazy { MarketInfoApi() }
    }
}
