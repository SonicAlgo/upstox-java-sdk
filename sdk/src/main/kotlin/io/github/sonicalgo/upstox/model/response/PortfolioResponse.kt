package io.github.sonicalgo.upstox.model.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.model.enums.Exchange
import io.github.sonicalgo.upstox.model.enums.Product

/**
 * Position details.
 *
 * Contains information about an open position.
 *
 * @property exchange Exchange identifier (NSE, BSE, NFO, MCX, CDS)
 * @property multiplier Quantity/lot size multiplier for P&L calculations
 * @property value Net position value
 * @property pnl Profit/loss on the position
 * @property product Product type: INTRADAY, DELIVERY, COVER_ORDER
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
    @JsonProperty("exchange")
    val exchange: Exchange,

    @JsonProperty("multiplier")
    val multiplier: Double,

    @JsonProperty("value")
    val value: Double,

    @JsonProperty("pnl")
    val pnl: Double,

    @JsonProperty("product")
    val product: Product,

    @JsonProperty("instrument_token")
    val instrumentToken: String,

    @JsonProperty("average_price")
    val averagePrice: Double,

    @JsonProperty("buy_value")
    val buyValue: Double,

    @JsonProperty("overnight_quantity")
    val overnightQuantity: Int,

    @JsonProperty("day_buy_value")
    val dayBuyValue: Double,

    @JsonProperty("day_buy_price")
    val dayBuyPrice: Double,

    @JsonProperty("overnight_buy_amount")
    val overnightBuyAmount: Double,

    @JsonProperty("overnight_buy_quantity")
    val overnightBuyQuantity: Int,

    @JsonProperty("day_buy_quantity")
    val dayBuyQuantity: Int,

    @JsonProperty("day_sell_value")
    val daySellValue: Double,

    @JsonProperty("day_sell_price")
    val daySellPrice: Double,

    @JsonProperty("overnight_sell_amount")
    val overnightSellAmount: Double,

    @JsonProperty("overnight_sell_quantity")
    val overnightSellQuantity: Int,

    @JsonProperty("day_sell_quantity")
    val daySellQuantity: Int,

    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("last_price")
    val lastPrice: Double,

    @JsonProperty("unrealised")
    val unrealised: Double,

    @JsonProperty("realised")
    val realised: Double,

    @JsonProperty("sell_value")
    val sellValue: Double,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String,

    @JsonProperty("close_price")
    val closePrice: Double,

    @JsonProperty("buy_price")
    val buyPrice: Double,

    @JsonProperty("sell_price")
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
 * @property product Product type: INTRADAY, DELIVERY, COVER_ORDER, MTF
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
    @JsonProperty("isin")
    val isin: String,

    @JsonProperty("cnc_used_quantity")
    val cncUsedQuantity: Int,

    @JsonProperty("collateral_type")
    val collateralType: String? = null,

    @JsonProperty("company_name")
    val companyName: String,

    @JsonProperty("haircut")
    val haircut: Double,

    @JsonProperty("product")
    val product: Product,

    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String,

    @JsonProperty("last_price")
    val lastPrice: Double,

    @JsonProperty("close_price")
    val closePrice: Double,

    @JsonProperty("pnl")
    val pnl: Double,

    @JsonProperty("day_change")
    val dayChange: Double,

    @JsonProperty("day_change_percentage")
    val dayChangePercentage: Double,

    @JsonProperty("instrument_token")
    val instrumentToken: String,

    @JsonProperty("average_price")
    val averagePrice: Double,

    @JsonProperty("collateral_quantity")
    val collateralQuantity: Int,

    @JsonProperty("collateral_update_quantity")
    val collateralUpdateQuantity: Int? = null,

    @JsonProperty("t1_quantity")
    val t1Quantity: Int,

    @JsonProperty("exchange")
    val exchange: Exchange
)

/**
 * Response from Convert Position API.
 *
 * @property status Conversion status message
 * @see <a href="https://upstox.com/developer/api-documentation/convert-positions">Convert Positions API</a>
 */
data class ConvertPositionResponse(
    @JsonProperty("status")
    val status: String
)
