package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.PaginatedResponse
import io.github.sonicalgo.upstox.common.TradeSegment
import io.github.sonicalgo.core.util.toQueryParams

/** Gets the profit and loss report. */
@JvmSynthetic
internal fun executeGetTradePnlReport(
    apiClient: ApiClient,
    params: TradePnlReportParams
): PaginatedResponse<List<TradePnlEntry>> {
    return apiClient.get(
        endpoint = "/trade/profit-loss/data",
        queryParams = toQueryParams(params),
        overrideBaseUrl = BASE_URL_V2
    )
}

/**
 * Parameters for getting the trade P&L report.
 *
 * @property segment Market segment: EQUITY, FUTURES_OPTIONS, COMMODITY, or CURRENCY_DERIVATIVES
 * @property financialYear Financial year in "YYNN" format
 * @property pageNumber Page number (1-indexed)
 * @property pageSize Results per page (maximum 5000)
 * @property fromDate Start date in dd-mm-yyyy format
 * @property toDate End date in dd-mm-yyyy format
 */
@GenerateBuilder
data class TradePnlReportParams(
    @JsonProperty("segment")
    val segment: TradeSegment,

    @JsonProperty("financial_year")
    val financialYear: String,

    @JsonProperty("page_number")
    val pageNumber: Int,

    @JsonProperty("page_size")
    val pageSize: Int,

    @JsonProperty("from_date")
    val fromDate: String? = null,

    @JsonProperty("to_date")
    val toDate: String? = null
)

/** Trade type in P&L reports. */
enum class TradeType {
    @JsonProperty("FUT") FUTURES,
    @JsonProperty("OPT") OPTIONS,
    @JsonProperty("EQ") EQUITY
}

/**
 * Trade P&L report entry.
 *
 * @property quantity Trade quantity
 * @property isin Standard ISIN
 * @property scripName Security name
 * @property tradeType Trade type: FUTURES, OPTIONS, or EQUITY
 * @property buyDate Buy date in dd-mm-yyyy format
 * @property buyAverage Average buy price
 * @property sellDate Sell date in dd-mm-yyyy format
 * @property sellAverage Average sell price
 * @property buyAmount Total buy amount
 * @property sellAmount Total sell amount
 * @see <a href="https://upstox.com/developer/api-documentation/get-profit-and-loss-report">Get P&L Report API</a>
 */
data class TradePnlEntry(
    @JsonProperty("quantity")
    val quantity: Double?,

    @JsonProperty("isin")
    val isin: String?,

    @JsonProperty("scrip_name")
    val scripName: String?,

    @JsonProperty("trade_type")
    val tradeType: TradeType?,

    @JsonProperty("buy_date")
    val buyDate: String?,

    @JsonProperty("buy_average")
    val buyAverage: Double?,

    @JsonProperty("sell_date")
    val sellDate: String?,

    @JsonProperty("sell_average")
    val sellAverage: Double?,

    @JsonProperty("buy_amount")
    val buyAmount: Double?,

    @JsonProperty("sell_amount")
    val sellAmount: Double?
)