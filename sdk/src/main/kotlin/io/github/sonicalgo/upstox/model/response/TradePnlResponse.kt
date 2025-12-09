package io.github.sonicalgo.upstox.model.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.model.enums.TradeType

/**
 * Trade P&L report metadata.
 *
 * Contains information about the available P&L report data.
 *
 * @property tradesCount Total number of trades in the P&L report
 * @property pageSizeLimit Maximum number of trades per page
 * @see <a href="https://upstox.com/developer/api-documentation/get-report-meta-data">Get Report Metadata API</a>
 */
data class TradePnlMetadata(
    @JsonProperty("trades_count")
    val tradesCount: Int,

    @JsonProperty("page_size_limit")
    val pageSizeLimit: Int
)

/**
 * Trade P&L report entry.
 *
 * Contains profit and loss information for a completed trade.
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
    val quantity: Double,

    @JsonProperty("isin")
    val isin: String? = null,

    @JsonProperty("scrip_name")
    val scripName: String,

    @JsonProperty("trade_type")
    val tradeType: TradeType,

    @JsonProperty("buy_date")
    val buyDate: String,

    @JsonProperty("buy_average")
    val buyAverage: Double,

    @JsonProperty("sell_date")
    val sellDate: String,

    @JsonProperty("sell_average")
    val sellAverage: Double,

    @JsonProperty("buy_amount")
    val buyAmount: Double,

    @JsonProperty("sell_amount")
    val sellAmount: Double
)

/**
 * Trade charges breakdown.
 *
 * Contains detailed breakdown of charges for a trading period.
 *
 * @property chargesBreakdown Detailed charges breakdown
 * @see <a href="https://upstox.com/developer/api-documentation/get-trade-charges">Get Trade Charges API</a>
 */
data class TradeChargesResponse(
    @JsonProperty("charges_breakdown")
    val chargesBreakdown: TradeChargesBreakdown
)

/**
 * Detailed trade charges breakdown.
 *
 * @property total Total charges for the period
 * @property brokerage Total brokerage commission
 * @property taxes Tax components
 * @property charges Other charges components
 */
data class TradeChargesBreakdown(
    @JsonProperty("total")
    val total: Double,

    @JsonProperty("brokerage")
    val brokerage: Double,

    @JsonProperty("taxes")
    val taxes: TradeChargesTaxes,

    @JsonProperty("charges")
    val charges: TradeChargesOther
)

/**
 * Tax breakdown for trade charges.
 *
 * @property gst Goods and Services Tax
 * @property stt Securities Transaction Tax
 * @property stampDuty Stamp duty
 */
data class TradeChargesTaxes(
    @JsonProperty("gst")
    val gst: Double,

    @JsonProperty("stt")
    val stt: Double,

    @JsonProperty("stamp_duty")
    val stampDuty: Double
)

/**
 * Other charges breakdown for trade charges.
 *
 * @property transaction Transaction charges
 * @property clearing Clearing charges
 * @property ipft IPFT charges
 * @property others Other miscellaneous charges
 * @property sebiTurnover SEBI turnover fees
 * @property dematTransaction Demat transaction charges
 */
data class TradeChargesOther(
    @JsonProperty("transaction")
    val transaction: Double,

    @JsonProperty("clearing")
    val clearing: Double,

    @JsonProperty("ipft")
    val ipft: Double? = null,

    @JsonProperty("others")
    val others: Double? = null,

    @JsonProperty("sebi_turnover")
    val sebiTurnover: Double,

    @JsonProperty("demat_transaction")
    val dematTransaction: Double? = null
)
