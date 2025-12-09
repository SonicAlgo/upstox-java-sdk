package io.github.sonicalgo.upstox.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.model.enums.Product
import io.github.sonicalgo.upstox.model.enums.TransactionType

/**
 * Parameters for converting positions between product types.
 *
 * Converts positions from one product type to another
 * (e.g., Intraday to Delivery or vice versa).
 *
 * Example usage:
 * ```kotlin
 * val portfolioApi = upstox.getPortfolioApi()
 *
 * val params = ConvertPositionParams(
 *     instrumentToken = "NSE_EQ|INE528G01035",
 *     newProduct = Product.DELIVERY,
 *     oldProduct = Product.INTRADAY,
 *     transactionType = TransactionType.BUY,
 *     quantity = 1
 * )
 * val response = portfolioApi.convertPosition(params)
 * ```
 *
 * @property instrumentToken Key identifying the instrument
 * @property newProduct Target product type: INTRADAY or DELIVERY
 * @property oldProduct Current product type: INTRADAY or DELIVERY
 * @property transactionType Trade direction: BUY or SELL
 * @property quantity Number of units to convert
 * @see <a href="https://upstox.com/developer/api-documentation/convert-positions">Convert Positions API</a>
 */
data class ConvertPositionParams(
    @JsonProperty("instrument_token")
    val instrumentToken: String,

    @JsonProperty("new_product")
    val newProduct: Product,

    @JsonProperty("old_product")
    val oldProduct: Product,

    @JsonProperty("transaction_type")
    val transactionType: TransactionType,

    @JsonProperty("quantity")
    val quantity: Int
)
