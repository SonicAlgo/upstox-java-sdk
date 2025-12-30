package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.PaginatedResponse
import io.github.sonicalgo.upstox.common.Exchange
import io.github.sonicalgo.upstox.common.TradeSegment
import io.github.sonicalgo.upstox.common.TransactionType
import io.github.sonicalgo.core.util.toQueryParams

/** Gets historical trades with pagination. */
@JvmSynthetic
internal fun executeGetHistoricalTrades(
    apiClient: ApiClient,
    params: HistoricalTradesParams
): PaginatedResponse<List<HistoricalTrade>> {
    return apiClient.get(
        endpoint = "/charges/historical-trades",
        queryParams = toQueryParams(params),
        overrideBaseUrl = BASE_URL_V2
    )
}

/**
 * Parameters for getting historical trades.
 *
 * @property startDate Start date (yyyy-MM-dd), within last 3 financial years
 * @property endDate End date (yyyy-MM-dd), must be >= startDate
 * @property pageNumber Page number (1-indexed, default 1)
 * @property pageSize Number of results per page (1-5000, default 500)
 * @property segment Trade segment filter (EQ, FO, COM, CD, MF)
 */
@GenerateBuilder
data class HistoricalTradesParams(
    @JsonProperty("start_date")
    val startDate: String,

    @JsonProperty("end_date")
    val endDate: String,

    @JsonProperty("page_number")
    val pageNumber: Int = 1,

    @JsonProperty("page_size")
    val pageSize: Int = 500,

    @JsonProperty("segment")
    val segment: TradeSegment? = null
)

/**
 * Historical trade record.
 *
 * @property exchange Exchange identifier
 * @property segment Market segment
 * @property optionType Option type: CALL_OPTION or PUT_OPTION (FO/CD segments only)
 * @property quantity Order quantity
 * @property amount Total transaction amount
 * @property tradeId Exchange-generated trade identifier
 * @property tradeDate Transaction date (yyyy-MM-dd)
 * @property transactionType Transaction type: BUY or SELL
 * @property scripName Security name
 * @property strikePrice Option strike price
 * @property expiry Derivative expiry date (yyyy-MM-dd)
 * @property price Per-unit execution price
 * @property isin Standard ISIN (EQ/MF segments)
 * @property symbol Trading symbol (EQ/FO segments)
 * @property instrumentToken Instrument key (EQ/MF segments)
 * @see <a href="https://upstox.com/developer/api-documentation/get-historical-trades">Get Historical Trades API</a>
 */
data class HistoricalTrade(
    @JsonProperty("exchange")
    val exchange: Exchange?,

    @JsonProperty("segment")
    val segment: TradeSegment?,

    @JsonProperty("option_type")
    val optionType: OptionType?,

    @JsonProperty("quantity")
    val quantity: Int?,

    @JsonProperty("amount")
    val amount: Double?,

    @JsonProperty("trade_id")
    val tradeId: String?,

    @JsonProperty("trade_date")
    val tradeDate: String?,

    @JsonProperty("transaction_type")
    val transactionType: TransactionType?,

    @JsonProperty("scrip_name")
    val scripName: String?,

    @JsonProperty("strike_price")
    val strikePrice: String?,

    @JsonProperty("expiry")
    val expiry: String?,

    @JsonProperty("price")
    val price: Double?,

    @JsonProperty("isin")
    val isin: String?,

    @JsonProperty("symbol")
    val symbol: String?,

    @JsonProperty("instrument_token")
    val instrumentToken: String?
)