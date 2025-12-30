package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.TradeSegment
import io.github.sonicalgo.core.util.toQueryParams

/** Gets trade charges breakdown for a period. */
@JvmSynthetic
internal fun executeGetTradeCharges(
    apiClient: ApiClient,
    params: TradeChargesParams
): TradeChargesResponse {
    val response: UpstoxResponse<TradeChargesResponse> = apiClient.get(
        endpoint = "/trade/profit-loss/charges",
        queryParams = toQueryParams(params),
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}

/**
 * Parameters for getting trade charges.
 *
 * @property segment Market segment: EQUITY, FUTURES_OPTIONS, COMMODITY, or CURRENCY_DERIVATIVES
 * @property financialYear Financial year in "YYNN" format
 * @property fromDate Start date in dd-mm-yyyy format
 * @property toDate End date in dd-mm-yyyy format
 */
@GenerateBuilder
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
 * Trade charges breakdown.
 *
 * @property chargesBreakdown Detailed charges breakdown
 * @see <a href="https://upstox.com/developer/api-documentation/get-trade-charges">Get Trade Charges API</a>
 */
data class TradeChargesResponse(
    @JsonProperty("charges_breakdown")
    val chargesBreakdown: TradeChargesBreakdown?
)

/** Detailed trade charges breakdown. */
data class TradeChargesBreakdown(
    @JsonProperty("total")
    val total: Double?,

    @JsonProperty("brokerage")
    val brokerage: Double?,

    @JsonProperty("taxes")
    val taxes: TradeChargesTaxes?,

    @JsonProperty("charges")
    val charges: TradeChargesOther?
)

/** Tax breakdown for trade charges. */
data class TradeChargesTaxes(
    @JsonProperty("gst")
    val gst: Double?,

    @JsonProperty("stt")
    val stt: Double?,

    @JsonProperty("stamp_duty")
    val stampDuty: Double?
)

/** Other charges breakdown for trade charges. */
data class TradeChargesOther(
    @JsonProperty("transaction")
    val transaction: Double?,

    @JsonProperty("clearing")
    val clearing: Double?,

    @JsonProperty("ipft")
    val ipft: Double?,

    @JsonProperty("others")
    val others: Double?,

    @JsonProperty("sebi_turnover")
    val sebiTurnover: Double?,

    @JsonProperty("demat_transaction")
    val dematTransaction: Double?
)