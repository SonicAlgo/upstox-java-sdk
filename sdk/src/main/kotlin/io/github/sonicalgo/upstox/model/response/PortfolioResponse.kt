package io.github.sonicalgo.upstox.model.response

import com.google.gson.annotations.SerializedName

/**
 * Position details.
 *
 * Contains information about an open position.
 *
 * @property exchange Exchange identifier (NSE, BSE, NFO, MCX, CDS)
 * @property multiplier Quantity/lot size multiplier for P&L calculations
 * @property value Net position value
 * @property pnl Profit/loss on the position
 * @property product Product type: I, D, CO
 * @property instrumentToken Instrument key identifier
 * @property averagePrice Mean acquisition price for net quantity
 * @property buyValue Net bought quantities value
 * @property overnightQuantity Quantity carried forward from previous session
 * @property dayBuyValue Intraday purchase amount
 * @property dayBuyPrice Average intraday purchase price
 * @property overnightBuyAmount Previous session purchase amount
 * @property overnightBuyQuantity Previous session buy quantity
 * @property dayBuyQuantity Intraday buy quantity
 * @property daySellValue Intraday sale amount
 * @property daySellPrice Average intraday sale price
 * @property overnightSellAmount Previous session sale amount
 * @property overnightSellQuantity Previous session short quantity
 * @property daySellQuantity Intraday sale quantity
 * @property quantity Net remaining quantity
 * @property lastPrice Current market price
 * @property unrealised Unrealized P&L on open positions
 * @property realised Realized P&L on closed positions
 * @property sellValue Net sold quantities value
 * @property tradingSymbol Instrument trading symbol
 * @property closePrice Previous trading day closing price
 * @property buyPrice Mean purchase price
 * @property sellPrice Mean sale price
 * @see <a href="https://upstox.com/developer/api-documentation/get-positions">Get Positions API</a>
 */
data class Position(
    val exchange: String,
    val multiplier: Double,
    val value: Double,
    val pnl: Double,
    val product: String,
    @SerializedName("instrument_token")
    val instrumentToken: String,
    @SerializedName("average_price")
    val averagePrice: Double,
    @SerializedName("buy_value")
    val buyValue: Double,
    @SerializedName("overnight_quantity")
    val overnightQuantity: Int,
    @SerializedName("day_buy_value")
    val dayBuyValue: Double,
    @SerializedName("day_buy_price")
    val dayBuyPrice: Double,
    @SerializedName("overnight_buy_amount")
    val overnightBuyAmount: Double,
    @SerializedName("overnight_buy_quantity")
    val overnightBuyQuantity: Int,
    @SerializedName("day_buy_quantity")
    val dayBuyQuantity: Int,
    @SerializedName("day_sell_value")
    val daySellValue: Double,
    @SerializedName("day_sell_price")
    val daySellPrice: Double,
    @SerializedName("overnight_sell_amount")
    val overnightSellAmount: Double,
    @SerializedName("overnight_sell_quantity")
    val overnightSellQuantity: Int,
    @SerializedName("day_sell_quantity")
    val daySellQuantity: Int,
    val quantity: Int,
    @SerializedName("last_price")
    val lastPrice: Double,
    val unrealised: Double,
    val realised: Double,
    @SerializedName("sell_value")
    val sellValue: Double,
    @SerializedName("trading_symbol")
    val tradingSymbol: String,
    @SerializedName("close_price")
    val closePrice: Double,
    @SerializedName("buy_price")
    val buyPrice: Double,
    @SerializedName("sell_price")
    val sellPrice: Double
)

/**
 * Holdings details.
 *
 * Contains information about long-term holdings in the demat account.
 *
 * @property isin Standard ISIN for multi-exchange listed stocks
 * @property cncUsedQuantity Quantity blocked for open/completed orders
 * @property collateralType RMS-assigned collateral category
 * @property companyName Company name
 * @property haircut RMS haircut percentage for collateral cases
 * @property product Product type: I, D, CO, MTF
 * @property quantity Total holding quantity
 * @property tradingSymbol Trading symbol
 * @property lastPrice Last traded price
 * @property closePrice Previous trading day closing price
 * @property pnl Profit and loss value
 * @property dayChange Absolute daily change in price
 * @property dayChangePercentage Percentage daily change
 * @property instrumentToken Instrument key identifier
 * @property averagePrice Average acquisition price
 * @property collateralQuantity RMS-marked collateral quantity
 * @property collateralUpdateQuantity Updated collateral quantity
 * @property t1Quantity T+1 day post-execution quantity
 * @property exchange Associated exchange
 * @see <a href="https://upstox.com/developer/api-documentation/get-holdings">Get Holdings API</a>
 */
data class Holding(
    val isin: String,
    @SerializedName("cnc_used_quantity")
    val cncUsedQuantity: Int,
    @SerializedName("collateral_type")
    val collateralType: String? = null,
    @SerializedName("company_name")
    val companyName: String,
    val haircut: Double,
    val product: String,
    val quantity: Int,
    @SerializedName("trading_symbol")
    val tradingSymbol: String,
    @SerializedName("last_price")
    val lastPrice: Double,
    @SerializedName("close_price")
    val closePrice: Double,
    val pnl: Double,
    @SerializedName("day_change")
    val dayChange: Double,
    @SerializedName("day_change_percentage")
    val dayChangePercentage: Double,
    @SerializedName("instrument_token")
    val instrumentToken: String,
    @SerializedName("average_price")
    val averagePrice: Double,
    @SerializedName("collateral_quantity")
    val collateralQuantity: Int,
    @SerializedName("collateral_update_quantity")
    val collateralUpdateQuantity: Int? = null,
    @SerializedName("t1_quantity")
    val t1Quantity: Int,
    val exchange: String
)

/**
 * Response from Convert Position API.
 *
 * @property status Conversion status message
 * @see <a href="https://upstox.com/developer/api-documentation/convert-positions">Convert Positions API</a>
 */
data class ConvertPositionResponse(
    val status: String
)
