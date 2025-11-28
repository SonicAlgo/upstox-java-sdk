package io.github.sonicalgo.upstox.model.response

import com.google.gson.annotations.SerializedName

/**
 * Brokerage charges breakdown.
 *
 * Contains detailed breakdown of charges for a trade.
 *
 * @property charges Charges breakdown details
 * @see <a href="https://upstox.com/developer/api-documentation/get-brokerage">Get Brokerage API</a>
 */
data class BrokerageResponse(
    val charges: ChargesBreakdown
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
    val total: Double,
    val brokerage: Double,
    val taxes: TaxBreakdown,
    @SerializedName("other_charges")
    val otherCharges: OtherChargesBreakdown? = null,
    @SerializedName("dp_plan")
    val dpPlan: DpPlan? = null
)

/**
 * Tax breakdown for a trade.
 *
 * @property gst Goods and Services Tax
 * @property stt Securities Transaction Tax
 * @property stampDuty Stamp duty levied on the trade
 */
data class TaxBreakdown(
    val gst: Double,
    val stt: Double,
    @SerializedName("stamp_duty")
    val stampDuty: Double
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
    val transaction: Double,
    val clearing: Double,
    val ipft: Double? = null,
    @SerializedName("sebi_turnover")
    val sebiTurnover: Double,
    val others: Double? = null,
    @SerializedName("demat_transaction")
    val dematTransaction: Double? = null
)

/**
 * DP (Depository Participant) plan details.
 *
 * @property name Name of the DP plan
 * @property minExpense Minimum expense under this plan
 */
data class DpPlan(
    val name: String,
    @SerializedName("min_expense")
    val minExpense: Double
)

/**
 * Margin calculation response.
 *
 * Contains margin requirements for a basket of instruments.
 *
 * @property margins Individual margin details for each instrument
 * @property requiredMargin Total margin required to execute all orders
 * @property finalMargin Total margin after applying hedging benefit
 * @see <a href="https://upstox.com/developer/api-documentation/margin">Margin API</a>
 */
data class MarginResponse(
    val margins: List<InstrumentMargin>,
    @SerializedName("required_margin")
    val requiredMargin: Double,
    @SerializedName("final_margin")
    val finalMargin: Double
)

/**
 * Margin details for a single instrument.
 *
 * @property spanMargin Upfront SPAN margin mandatory by exchange
 * @property exposureMargin Exposure margin based on ELM percentage
 * @property equityMargin Margin applicable for equity trades
 * @property netBuyPremium Option premium required for option buying
 * @property additionalMargin Additional margin for MCX FNO trades
 * @property totalMargin Total margin required for this instrument
 * @property tenderMargin Tender margin as futures approach expiration
 */
data class InstrumentMargin(
    @SerializedName("span_margin")
    val spanMargin: Double,
    @SerializedName("exposure_margin")
    val exposureMargin: Double,
    @SerializedName("equity_margin")
    val equityMargin: Double,
    @SerializedName("net_buy_premium")
    val netBuyPremium: Double,
    @SerializedName("additional_margin")
    val additionalMargin: Double,
    @SerializedName("total_margin")
    val totalMargin: Double,
    @SerializedName("tender_margin")
    val tenderMargin: Double? = null
)
