package io.github.sonicalgo.upstox.model.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.model.enums.Exchange
import io.github.sonicalgo.upstox.model.enums.HolidayType
import io.github.sonicalgo.upstox.model.enums.MarketStatus

/**
 * Market holiday information.
 *
 * Contains details about market holidays and special timings.
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
    val date: String,

    @JsonProperty("description")
    val description: String,

    @JsonProperty("holiday_type")
    val holidayType: HolidayType,

    @JsonProperty("closed_exchanges")
    val closedExchanges: List<Exchange>? = null,

    @JsonProperty("open_exchanges")
    val openExchanges: List<ExchangeTiming>? = null
)

/**
 * Exchange timing information.
 *
 * @property exchange Exchange identifier (NSE, BSE, NFO, etc.)
 * @property startTime Market open timestamp in milliseconds
 * @property endTime Market close timestamp in milliseconds
 */
data class ExchangeTiming(
    @JsonProperty("exchange")
    val exchange: Exchange,

    @JsonProperty("start_time")
    val startTime: Long,

    @JsonProperty("end_time")
    val endTime: Long
)

/**
 * Market timing for a specific date.
 *
 * @property exchange Exchange identifier
 * @property startTime Market open timestamp in milliseconds
 * @property endTime Market close timestamp in milliseconds
 * @see <a href="https://upstox.com/developer/api-documentation/get-market-timings">Market Timings API</a>
 */
data class MarketTiming(
    @JsonProperty("exchange")
    val exchange: Exchange,

    @JsonProperty("start_time")
    val startTime: Long,

    @JsonProperty("end_time")
    val endTime: Long
)

/**
 * Exchange market status.
 *
 * Contains current status of an exchange.
 *
 * @property exchange Exchange identifier
 * @property status Current market status (NORMAL_OPEN, NORMAL_CLOSE, PRE_OPEN_START, PRE_OPEN_END, CLOSING_START, CLOSING_END)
 * @property lastUpdated Timestamp when status was last updated (milliseconds)
 * @see <a href="https://upstox.com/developer/api-documentation/get-market-status">Market Status API</a>
 */
data class MarketStatusResponse(
    @JsonProperty("exchange")
    val exchange: Exchange,

    @JsonProperty("status")
    val status: MarketStatus,

    @JsonProperty("last_updated")
    val lastUpdated: Long
)
