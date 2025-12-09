package io.github.sonicalgo.upstox.api

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.exception.UpstoxApiException
import io.github.sonicalgo.upstox.model.common.PaginatedResponse
import io.github.sonicalgo.upstox.model.common.UpstoxResponse
import io.github.sonicalgo.upstox.model.request.TradeChargesParams
import io.github.sonicalgo.upstox.model.request.TradePnlMetadataParams
import io.github.sonicalgo.upstox.model.request.TradePnlReportParams
import io.github.sonicalgo.upstox.model.response.TradeChargesResponse
import io.github.sonicalgo.upstox.model.response.TradePnlEntry
import io.github.sonicalgo.upstox.model.response.TradePnlMetadata
import io.github.sonicalgo.upstox.util.toQueryParams

/**
 * API module for trade profit and loss reports.
 *
 * Provides methods for retrieving P&L reports and trade charges.
 *
 * Example usage:
 * ```kotlin
 * val pnlApi = upstox.getTradePnlApi()
 *
 * // Get report metadata
 * val metadata = pnlApi.getReportMetadata(TradePnlMetadataParams(
 *     segment = TradeSegment.EQUITY,
 *     financialYear = "2324"
 * ))
 * println("Total trades: ${metadata.tradesCount}")
 *
 * // Get P&L report
 * val report = pnlApi.getProfitAndLossReport(TradePnlReportParams(
 *     segment = TradeSegment.EQUITY,
 *     financialYear = "2324",
 *     pageNumber = 1,
 *     pageSize = 100
 * ))
 * ```
 *
 * @see <a href="https://upstox.com/developer/api-documentation/get-report-meta-data">Get Report Metadata API</a>
 */
class TradePnlApi internal constructor(private val apiClient: ApiClient) {

    /**
     * Gets metadata about the P&L report.
     *
     * Returns the total number of trades and page size limit for the report.
     *
     * Example:
     * ```kotlin
     * val pnlApi = upstox.getTradePnlApi()
     *
     * val metadata = pnlApi.getReportMetadata(TradePnlMetadataParams(
     *     segment = TradeSegment.EQUITY,
     *     financialYear = "2324",
     *     fromDate = "01-04-2023",
     *     toDate = "31-03-2024"
     * ))
     * println("Total trades: ${metadata.tradesCount}")
     * println("Page size limit: ${metadata.pageSizeLimit}")
     * ```
     *
     * @param params Metadata query parameters
     * @return [TradePnlMetadata] P&L report metadata
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-report-meta-data">Get Report Metadata API</a>
     */
    fun getReportMetadata(params: TradePnlMetadataParams): TradePnlMetadata {
        val response: UpstoxResponse<TradePnlMetadata> = apiClient.get(
            endpoint = Endpoints.GET_REPORT_METADATA,
            queryParams = toQueryParams(params),
            overrideBaseUrl = BASE_URL_V2
        )
        return response.dataOrThrow()
    }

    /**
     * Gets the profit and loss report.
     *
     * Returns detailed P&L information for closed trades with pagination.
     *
     * Example:
     * ```kotlin
     * val pnlApi = upstox.getTradePnlApi()
     *
     * val report = pnlApi.getProfitAndLossReport(TradePnlReportParams(
     *     segment = TradeSegment.EQUITY,
     *     financialYear = "2324",
     *     pageNumber = 1,
     *     pageSize = 100,
     *     fromDate = "01-04-2023",
     *     toDate = "31-03-2024"
     * ))
     * report.data?.forEach { entry ->
     *     val pnl = entry.sellAmount - entry.buyAmount
     *     println("${entry.scripName}: Buy=${entry.buyAmount}, Sell=${entry.sellAmount}, P&L=$pnl")
     * }
     * ```
     *
     * @param params P&L report query parameters
     * @return [PaginatedResponse]<List<[TradePnlEntry]>> Paginated P&L report
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-profit-and-loss-report">Get P&L Report API</a>
     */
    fun getProfitAndLossReport(params: TradePnlReportParams): PaginatedResponse<List<TradePnlEntry>> {
        val response: UpstoxResponse<List<TradePnlEntry>> = apiClient.get(
            endpoint = Endpoints.GET_PROFIT_AND_LOSS_REPORT,
            queryParams = toQueryParams(params),
            overrideBaseUrl = BASE_URL_V2
        )
        return PaginatedResponse(
            status = response.status,
            data = response.data,
            errors = response.errors
        )
    }

    /**
     * Gets the trade charges breakdown for a period.
     *
     * Returns detailed breakdown of all charges including brokerage,
     * taxes, and other fees.
     *
     * Example:
     * ```kotlin
     * val pnlApi = upstox.getTradePnlApi()
     *
     * val charges = pnlApi.getTradeCharges(TradeChargesParams(
     *     segment = TradeSegment.EQUITY,
     *     financialYear = "2324",
     *     fromDate = "01-04-2023",
     *     toDate = "31-03-2024"
     * ))
     * println("Total Charges: ${charges.chargesBreakdown.total}")
     * println("Brokerage: ${charges.chargesBreakdown.brokerage}")
     * println("GST: ${charges.chargesBreakdown.taxes.gst}")
     * println("STT: ${charges.chargesBreakdown.taxes.stt}")
     * ```
     *
     * @param params Trade charges query parameters
     * @return [TradeChargesResponse] Trade charges breakdown
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-trade-charges">Get Trade Charges API</a>
     */
    fun getTradeCharges(params: TradeChargesParams): TradeChargesResponse {
        val response: UpstoxResponse<TradeChargesResponse> = apiClient.get(
            endpoint = Endpoints.GET_TRADE_CHARGES,
            queryParams = toQueryParams(params),
            overrideBaseUrl = BASE_URL_V2
        )
        return response.dataOrThrow()
    }

    internal object Endpoints {
        const val GET_REPORT_METADATA = "/trade/profit-loss/metadata"
        const val GET_PROFIT_AND_LOSS_REPORT = "/trade/profit-loss/data"
        const val GET_TRADE_CHARGES = "/trade/profit-loss/charges"
    }
}
