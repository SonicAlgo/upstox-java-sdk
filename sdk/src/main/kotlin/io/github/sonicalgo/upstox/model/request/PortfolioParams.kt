package io.github.sonicalgo.upstox.model.request

import com.google.gson.annotations.SerializedName
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
 *     newProduct = Product.D,
 *     oldProduct = Product.I,
 *     transactionType = TransactionType.BUY,
 *     quantity = 1
 * )
 * val response = portfolioApi.convertPosition(params)
 * ```
 *
 * @property instrumentToken Key identifying the instrument
 * @property newProduct Target product type: I (Intraday) or D (Delivery)
 * @property oldProduct Current product type: I (Intraday) or D (Delivery)
 * @property transactionType Trade direction: BUY or SELL
 * @property quantity Number of units to convert
 * @see <a href="https://upstox.com/developer/api-documentation/convert-positions">Convert Positions API</a>
 */
data class ConvertPositionParams(
    @SerializedName("instrument_token")
    val instrumentToken: String,
    @SerializedName("new_product")
    val newProduct: Product,
    @SerializedName("old_product")
    val oldProduct: Product,
    @SerializedName("transaction_type")
    val transactionType: TransactionType,
    val quantity: Int
)
