package io.github.sonicalgo.upstox.model.response

import com.google.gson.annotations.SerializedName

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
    @SerializedName("trades_count")
    val tradesCount: Int,
    @SerializedName("page_size_limit")
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
 * @property tradeType Trade type: FUT, OPT, or EQ
 * @property buyDate Buy date in dd-mm-yyyy format
 * @property buyAverage Average buy price
 * @property sellDate Sell date in dd-mm-yyyy format
 * @property sellAverage Average sell price
 * @property buyAmount Total buy amount
 * @property sellAmount Total sell amount
 * @see <a href="https://upstox.com/developer/api-documentation/get-profit-and-loss-report">Get P&L Report API</a>
 */
data class TradePnlEntry(
    val quantity: Double,
    val isin: String? = null,
    @SerializedName("scrip_name")
    val scripName: String,
    @SerializedName("trade_type")
    val tradeType: String,
    @SerializedName("buy_date")
    val buyDate: String,
    @SerializedName("buy_average")
    val buyAverage: Double,
    @SerializedName("sell_date")
    val sellDate: String,
    @SerializedName("sell_average")
    val sellAverage: Double,
    @SerializedName("buy_amount")
    val buyAmount: Double,
    @SerializedName("sell_amount")
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
    @SerializedName("charges_breakdown")
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
    val total: Double,
    val brokerage: Double,
    val taxes: TradeChargesTaxes,
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
    val gst: Double,
    val stt: Double,
    @SerializedName("stamp_duty")
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
    val transaction: Double,
    val clearing: Double,
    val ipft: Double? = null,
    val others: Double? = null,
    @SerializedName("sebi_turnover")
    val sebiTurnover: Double,
    @SerializedName("demat_transaction")
    val dematTransaction: Double? = null
)
