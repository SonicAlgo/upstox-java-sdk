package io.github.sonicalgo.upstox.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.model.enums.TradeSegment

/**
 * Parameters for getting trade P&L report metadata.
 *
 * Retrieves metadata about the P&L report including total trade count.
 *
 * Example usage:
 * ```kotlin
 * val pnlApi = upstox.getTradePnlApi()
 *
 * val params = TradePnlMetadataParams(
 *     segment = TradeSegment.EQUITY,
 *     financialYear = "2324",
 *     fromDate = "01-04-2023",
 *     toDate = "31-03-2024"
 * )
 * val metadata = pnlApi.getReportMetadata(params)
 * ```
 *
 * @property segment Market segment: EQUITY, FUTURES_OPTIONS, COMMODITY, or CURRENCY_DERIVATIVES
 * @property financialYear Financial year in "YYNN" format (e.g., "2324" for 2023-2024)
 * @property fromDate Start date in dd-mm-yyyy format (within the financial year)
 * @property toDate End date in dd-mm-yyyy format (within the financial year)
 * @see <a href="https://upstox.com/developer/api-documentation/get-report-meta-data">Get Report Metadata API</a>
 */
data class TradePnlMetadataParams(
    @JsonProperty("segment")
    val segment: TradeSegment,

    @JsonProperty("financial_year")
    val financialYear: String,

    @JsonProperty("from_date")
    val fromDate: String? = null,

    @JsonProperty("to_date")
    val toDate: String? = null
)

/**
 * Parameters for getting the trade P&L report.
 *
 * Retrieves detailed profit and loss data for trades.
 *
 * Example usage:
 * ```kotlin
 * val pnlApi = upstox.getTradePnlApi()
 *
 * val params = TradePnlReportParams(
 *     segment = TradeSegment.EQUITY,
 *     financialYear = "2324",
 *     pageNumber = 1,
 *     pageSize = 100,
 *     fromDate = "01-04-2023",
 *     toDate = "31-03-2024"
 * )
 * val report = pnlApi.getProfitAndLossReport(params)
 * ```
 *
 * @property segment Market segment: EQUITY, FUTURES_OPTIONS, COMMODITY, or CURRENCY_DERIVATIVES
 * @property financialYear Financial year in "YYNN" format
 * @property pageNumber Page number (1-indexed)
 * @property pageSize Results per page (maximum 5000)
 * @property fromDate Start date in dd-mm-yyyy format
 * @property toDate End date in dd-mm-yyyy format
 * @see <a href="https://upstox.com/developer/api-documentation/get-profit-and-loss-report">Get P&L Report API</a>
 */
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

/**
 * Parameters for getting trade charges.
 *
 * Retrieves charges breakdown for trades within a period.
 *
 * Example usage:
 * ```kotlin
 * val pnlApi = upstox.getTradePnlApi()
 *
 * val params = TradeChargesParams(
 *     segment = TradeSegment.EQUITY,
 *     financialYear = "2324",
 *     fromDate = "01-04-2023",
 *     toDate = "31-03-2024"
 * )
 * val charges = pnlApi.getTradeCharges(params)
 * ```
 *
 * @property segment Market segment: EQUITY, FUTURES_OPTIONS, COMMODITY, or CURRENCY_DERIVATIVES
 * @property financialYear Financial year in "YYNN" format
 * @property fromDate Start date in dd-mm-yyyy format
 * @property toDate End date in dd-mm-yyyy format
 * @see <a href="https://upstox.com/developer/api-documentation/get-trade-charges">Get Trade Charges API</a>
 */
data class TradeChargesParams(
    @JsonProperty("segment")
    val segment: TradeSegment,

    @JsonProperty("financial_year")
    val financialYear: String,

    @JsonProperty("from_date")
    val fromDate: String? = null,

    @JsonProperty("to_date")
    val toDate: String? = null
)

/**
 * Parameters for getting historical trades.
 *
 * Retrieves historical trade records with pagination.
 *
 * Example usage:
 * ```kotlin
 * val ordersApi = upstox.getOrdersApi()
 *
 * val params = HistoricalTradesParams(
 *     startDate = "2022-04-01",
 *     endDate = "2023-03-31",
 *     pageNumber = 1,
 *     pageSize = 100,
 *     segment = TradeSegment.EQUITY
 * )
 * val trades = ordersApi.getHistoricalTrades(params)
 * ```
 *
 * @property startDate Start date in YYYY-mm-dd format (within last 3 financial years)
 * @property endDate End date in YYYY-mm-dd format (within last 3 financial years)
 * @property pageNumber Page number starting from 1
 * @property pageSize Results per page (1-5000)
 * @property segment Market segment filter (optional, returns all segments if omitted)
 * @see <a href="https://upstox.com/developer/api-documentation/get-historical-trades">Get Historical Trades API</a>
 */
data class HistoricalTradesParams(
    @JsonProperty("start_date")
    val startDate: String,

    @JsonProperty("end_date")
    val endDate: String,

    @JsonProperty("page_number")
    val pageNumber: Int,

    @JsonProperty("page_size")
    val pageSize: Int,

    @JsonProperty("segment")
    val segment: TradeSegment? = null
)
