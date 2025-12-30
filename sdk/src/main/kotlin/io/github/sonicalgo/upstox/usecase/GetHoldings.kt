package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.Exchange
import io.github.sonicalgo.upstox.common.Product

/** Gets all holdings in the demat account. */
@JvmSynthetic
internal fun executeGetHoldings(apiClient: ApiClient): List<Holding> {
    val response: UpstoxResponse<List<Holding>> = apiClient.get(
        endpoint = "/portfolio/long-term-holdings",
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}

/**
 * Holdings details.
 *
 * @property isin Standard ISIN for multi-exchange listed stocks
 * @property cncUsedQuantity Quantity blocked for open/completed orders
 * @property collateralType RMS-assigned collateral category
 * @property companyName Company name
 * @property haircut RMS haircut percentage for collateral cases
 * @property product Product type
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
    val isin: String?,

    @JsonProperty("cnc_used_quantity")
    val cncUsedQuantity: Int?,

    @JsonProperty("collateral_type")
    val collateralType: String?,

    @JsonProperty("company_name")
    val companyName: String?,

    @JsonProperty("haircut")
    val haircut: Double?,

    @JsonProperty("product")
    val product: Product?,

    @JsonProperty("quantity")
    val quantity: Int?,

    @JsonProperty("trading_symbol")
    val tradingSymbol: String?,

    @JsonProperty("last_price")
    val lastPrice: Double?,

    @JsonProperty("close_price")
    val closePrice: Double?,

    @JsonProperty("pnl")
    val pnl: Double?,

    @JsonProperty("day_change")
    val dayChange: Double?,

    @JsonProperty("day_change_percentage")
    val dayChangePercentage: Double?,

    @JsonProperty("instrument_token")
    val instrumentToken: String?,

    @JsonProperty("average_price")
    val averagePrice: Double?,

    @JsonProperty("collateral_quantity")
    val collateralQuantity: Int?,

    @JsonProperty("collateral_update_quantity")
    val collateralUpdateQuantity: Int?,

    @JsonProperty("t1_quantity")
    val t1Quantity: Int?,

    @JsonProperty("exchange")
    val exchange: Exchange?
)