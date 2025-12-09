package io.github.sonicalgo.upstox.model.request

import com.fasterxml.jackson.annotation.JsonProperty
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
 *     product = Product.DELIVERY,
 *     transactionType = TransactionType.BUY,
 *     price = 13.7
 * )
 * val charges = chargesApi.getBrokerage(params)
 * ```
 *
 * @property instrumentToken Key of the instrument (e.g., "NSE_EQ|INE669E01016")
 * @property quantity Quantity with which the order is to be placed
 * @property product Product type: DELIVERY or INTRADAY
 * @property transactionType Transaction type: BUY or SELL
 * @property price Price at which the order is to be placed
 * @see <a href="https://upstox.com/developer/api-documentation/get-brokerage">Get Brokerage API</a>
 */
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
 * Instrument details for margin calculation.
 *
 * @property instrumentKey Key of the instrument
 * @property quantity Quantity (must be a multiple of the lot size)
 * @property transactionType Transaction type: BUY or SELL
 * @property product Product type: INTRADAY, DELIVERY, COVER_ORDER, or MTF
 * @property price Price at which the order is to be placed (optional)
 */
data class MarginInstrument(
    @JsonProperty("instrument_key")
    val instrumentKey: String,

    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("transaction_type")
    val transactionType: TransactionType,

    @JsonProperty("product")
    val product: Product,

    @JsonProperty("price")
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
 *             product = Product.DELIVERY
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
    @JsonProperty("instruments")
    val instruments: List<MarginInstrument>
)
