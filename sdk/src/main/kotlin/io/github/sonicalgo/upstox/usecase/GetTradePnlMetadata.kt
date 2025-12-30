package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.TradeSegment
import io.github.sonicalgo.core.util.toQueryParams

/** Gets metadata about the P&L report. */
@JvmSynthetic
internal fun executeGetTradePnlMetadata(
    apiClient: ApiClient,
    params: TradePnlMetadataParams
): TradePnlMetadata {
    val response: UpstoxResponse<TradePnlMetadata> = apiClient.get(
        endpoint = "/trade/profit-loss/metadata",
        queryParams = toQueryParams(params),
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}

/**
 * Parameters for getting trade P&L report metadata.
 *
 * @property segment Market segment: EQUITY, FUTURES_OPTIONS, COMMODITY, or CURRENCY_DERIVATIVES
 * @property financialYear Financial year in "YYNN" format (e.g., "2324" for 2023-2024)
 * @property fromDate Start date in dd-mm-yyyy format (within the financial year)
 * @property toDate End date in dd-mm-yyyy format (within the financial year)
 */
@GenerateBuilder
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
 * Trade P&L report metadata.
 *
 * @property tradesCount Total number of trades in the P&L report
 * @property pageSizeLimit Maximum number of trades per page
 * @see <a href="https://upstox.com/developer/api-documentation/get-report-meta-data">Get Report Metadata API</a>
 */
data class TradePnlMetadata(
    @JsonProperty("trades_count")
    val tradesCount: Int?,

    @JsonProperty("page_size_limit")
    val pageSizeLimit: Int?
)
