package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.Exchange

/** Gets all market holidays for the year. */
@JvmSynthetic
internal fun executeGetMarketHolidays(apiClient: ApiClient): List<MarketHoliday> {
    val response: UpstoxResponse<List<MarketHoliday>> = apiClient.get(
        endpoint = "/market/holidays",
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}

/** Gets market holiday information for a specific date. */
@JvmSynthetic
internal fun executeGetMarketHoliday(apiClient: ApiClient, date: String): MarketHoliday? {
    val response: UpstoxResponse<List<MarketHoliday>> = apiClient.get(
        endpoint = "/market/holidays/$date",
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow().firstOrNull()
}

/** Holiday types. */
enum class HolidayType {
    SETTLEMENT_HOLIDAY,
    TRADING_HOLIDAY,
    SPECIAL_TIMING
}

/**
 * Market holiday information.
 *
 * @property date Holiday date in YYYY-MM-DD format
 * @property description Holiday description/name
 * @property holidayType Holiday type: SETTLEMENT_HOLIDAY, TRADING_HOLIDAY, or SPECIAL_TIMING
 * @property closedExchanges Exchanges with closed markets on this holiday
 * @property openExchanges Exchanges with open markets (possibly modified timings)
 * @see <a href="https://upstox.com/developer/api-documentation/get-market-holidays">Market Holidays API</a>
 */
data class MarketHoliday(
    @JsonProperty("date")
    val date: String?,

    @JsonProperty("description")
    val description: String?,

    @JsonProperty("holiday_type")
    val holidayType: HolidayType?,

    @JsonProperty("closed_exchanges")
    val closedExchanges: List<Exchange>?,

    @JsonProperty("open_exchanges")
    val openExchanges: List<ExchangeTiming>?
)

/** Exchange timing information for holidays. */
data class ExchangeTiming(
    @JsonProperty("exchange")
    val exchange: Exchange?,

    @JsonProperty("start_time")
    val startTime: Long?,

    @JsonProperty("end_time")
    val endTime: Long?
)