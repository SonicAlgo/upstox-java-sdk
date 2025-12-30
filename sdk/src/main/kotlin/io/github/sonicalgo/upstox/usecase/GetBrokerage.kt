package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.Product
import io.github.sonicalgo.upstox.common.TransactionType
import io.github.sonicalgo.core.util.toQueryParams

/** Gets brokerage charges for a potential trade. */
@JvmSynthetic
internal fun executeGetBrokerage(apiClient: ApiClient, params: BrokerageParams): BrokerageResponse {
    val response: UpstoxResponse<BrokerageResponse> = apiClient.get(
        endpoint = "/charges/brokerage",
        queryParams = toQueryParams(params),
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}

/**
 * Parameters for getting brokerage charges.
 *
 * @property instrumentToken Key of the instrument (e.g., "NSE_EQ|INE669E01016")
 * @property quantity Quantity with which the order is to be placed
 * @property product Product type: DELIVERY or INTRADAY
 * @property transactionType Transaction type: BUY or SELL
 * @property price Price at which the order is to be placed
 */
@GenerateBuilder
data class BrokerageParams(
    @JsonProperty("instrument_token")
    val instrumentToken: String,

    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("product")
    val product: Product,

    @JsonProperty("transaction_type")
    val transactionType: TransactionType,

    @JsonProperty("price")
    val price: Double
)

/**
 * Brokerage charges breakdown.
 *
 * @property charges Charges breakdown details
 * @see <a href="https://upstox.com/developer/api-documentation/get-brokerage">Get Brokerage API</a>
 */
data class BrokerageResponse(
    @JsonProperty("charges")
    val charges: ChargesBreakdown?
)

/**
 * Detailed breakdown of trading charges.
 *
 * @property total Total charges including all components
 * @property brokerage Brokerage commission on the trade
 * @property taxes Tax components breakdown
 * @property otherCharges Other charges breakdown
 * @property dpPlan DP plan details
 */
data class ChargesBreakdown(
    @JsonProperty("total")
    val total: Double?,

    @JsonProperty("brokerage")
    val brokerage: Double?,

    @JsonProperty("taxes")
    val taxes: TaxBreakdown?,

    @JsonProperty("other_charges")
    val otherCharges: OtherChargesBreakdown?,

    @JsonProperty("dp_plan")
    val dpPlan: DpPlan?
)

/**
 * Tax breakdown for a trade.
 *
 * @property gst Goods and Services Tax
 * @property stt Securities Transaction Tax
 * @property stampDuty Stamp duty levied on the trade
 */
data class TaxBreakdown(
    @JsonProperty("gst")
    val gst: Double?,

    @JsonProperty("stt")
    val stt: Double?,

    @JsonProperty("stamp_duty")
    val stampDuty: Double?
)

/**
 * Other charges breakdown.
 *
 * @property transaction Transaction charges levied by the exchange
 * @property clearing Clearing charges
 * @property ipft IPFT (Investor Protection Fund Trust) charges
 * @property sebiTurnover SEBI turnover fees
 * @property others Other miscellaneous charges
 * @property dematTransaction Demat transaction charges
 */
data class OtherChargesBreakdown(
    @JsonProperty("transaction")
    val transaction: Double?,

    @JsonProperty("clearing")
    val clearing: Double?,

    @JsonProperty("ipft")
    val ipft: Double?,

    @JsonProperty("sebi_turnover")
    val sebiTurnover: Double?,

    @JsonProperty("others")
    val others: Double?,

    @JsonProperty("demat_transaction")
    val dematTransaction: Double?
)

/**
 * DP (Depository Participant) plan details.
 *
 * @property name Name of the DP plan
 * @property minExpense Minimum expense under this plan
 */
data class DpPlan(
    @JsonProperty("name")
    val name: String?,

    @JsonProperty("min_expense")
    val minExpense: Double?
)