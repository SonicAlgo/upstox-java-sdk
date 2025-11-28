package io.github.sonicalgo.upstox.model.request

import com.google.gson.annotations.SerializedName
import io.github.sonicalgo.upstox.model.enums.Product
import io.github.sonicalgo.upstox.model.enums.TransactionType

/**
 * Parameters for getting brokerage charges.
 *
 * Calculates the charges that would be incurred for a trade.
 *
 * Example usage:
 * ```kotlin
 * val chargesApi = upstox.getChargesApi()
 *
 * val params = BrokerageParams(
 *     instrumentToken = "NSE_EQ|INE669E01016",
 *     quantity = 10,
 *     product = Product.D,
 *     transactionType = TransactionType.BUY,
 *     price = 13.7
 * )
 * val charges = chargesApi.getBrokerage(params)
 * ```
 *
 * @property instrumentToken Key of the instrument (e.g., "NSE_EQ|INE669E01016")
 * @property quantity Quantity with which the order is to be placed
 * @property product Product type: D (Delivery) or I (Intraday)
 * @property transactionType Transaction type: BUY or SELL
 * @property price Price at which the order is to be placed
 * @see <a href="https://upstox.com/developer/api-documentation/get-brokerage">Get Brokerage API</a>
 */
data class BrokerageParams(
    @SerializedName("instrument_token")
    val instrumentToken: String,
    val quantity: Int,
    val product: Product,
    @SerializedName("transaction_type")
    val transactionType: TransactionType,
    val price: Double
)

/**
 * Instrument details for margin calculation.
 *
 * @property instrumentKey Key of the instrument
 * @property quantity Quantity (must be a multiple of the lot size)
 * @property transactionType Transaction type: BUY or SELL
 * @property product Product type: I, D, CO, or MTF
 * @property price Price at which the order is to be placed (optional)
 */
data class MarginInstrument(
    @SerializedName("instrument_key")
    val instrumentKey: String,
    val quantity: Int,
    @SerializedName("transaction_type")
    val transactionType: TransactionType,
    val product: Product,
    val price: Double? = null
)

/**
 * Parameters for calculating margin requirements.
 *
 * Calculates the margin required for placing one or more orders.
 * Maximum 20 instruments per request.
 *
 * Example usage:
 * ```kotlin
 * val marginsApi = upstox.getMarginsApi()
 *
 * val params = MarginParams(
 *     instruments = listOf(
 *         MarginInstrument(
 *             instrumentKey = "NSE_EQ|INE669E01016",
 *             quantity = 1,
 *             transactionType = TransactionType.BUY,
 *             product = Product.D
 *         )
 *     )
 * )
 * val margin = marginsApi.getMargin(params)
 * ```
 *
 * @property instruments List of instruments for margin calculation (max 20, no duplicates)
 * @see <a href="https://upstox.com/developer/api-documentation/margin">Margin API</a>
 */
data class MarginParams(
    val instruments: List<MarginInstrument>
)
