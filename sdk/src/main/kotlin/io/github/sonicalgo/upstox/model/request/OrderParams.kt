package io.github.sonicalgo.upstox.model.request

import com.google.gson.annotations.SerializedName
import io.github.sonicalgo.upstox.model.enums.*

/**
 * Parameters for placing a single order (V3 API).
 *
 * Places an order through the high-frequency trading (HFT) endpoint.
 *
 * Example usage:
 * ```kotlin
 * val ordersApi = upstox.getOrdersApi()
 *
 * val params = PlaceOrderParams(
 *     instrumentToken = "NSE_EQ|INE669E01016",
 *     quantity = 1,
 *     product = Product.D,
 *     validity = Validity.DAY,
 *     price = 100.0,
 *     orderType = OrderType.LIMIT,
 *     transactionType = TransactionType.BUY,
 *     disclosedQuantity = 0,
 *     triggerPrice = 0.0,
 *     isAmo = false
 * )
 * val response = ordersApi.placeOrder(params)
 * ```
 *
 * @property instrumentToken Key of the instrument (e.g., "NSE_EQ|INE669E01016")
 * @property quantity Quantity to order (lots for commodity segments)
 * @property product Product type: I (Intraday), D (Delivery), or MTF
 * @property validity Order validity: DAY or IOC
 * @property price Order price (0 for MARKET orders)
 * @property orderType Order type: MARKET, LIMIT, SL, or SL-M
 * @property transactionType Transaction type: BUY or SELL
 * @property disclosedQuantity Quantity visible in market depth (0 for full visibility)
 * @property triggerPrice Trigger price for stop loss orders (0 for non-SL)
 * @property isAmo Whether this is an After Market Order
 * @property tag Custom order identifier (max 40 characters)
 * @property slice Enable automatic order slicing for large orders
 * @see <a href="https://upstox.com/developer/api-documentation/v3/place-order">Place Order V3 API</a>
 */
data class PlaceOrderParams(
    @SerializedName("instrument_token")
    val instrumentToken: String,
    val quantity: Int,
    val product: Product,
    val validity: Validity,
    val price: Double,
    @SerializedName("order_type")
    val orderType: OrderType,
    @SerializedName("transaction_type")
    val transactionType: TransactionType,
    @SerializedName("disclosed_quantity")
    val disclosedQuantity: Int,
    @SerializedName("trigger_price")
    val triggerPrice: Double,
    @SerializedName("is_amo")
    val isAmo: Boolean,
    val tag: String? = null,
    val slice: Boolean? = null
)

/**
 * Parameters for placing multiple orders in a single request.
 *
 * Allows placing up to 25 orders in a single API call.
 *
 * @property correlationId Unique identifier for this order (max 20 chars)
 * @property quantity Quantity to order
 * @property product Product type: I, D, or MTF
 * @property validity Order validity: DAY or IOC
 * @property price Order price (0 for MARKET orders)
 * @property instrumentToken Instrument key identifier
 * @property orderType Order type: MARKET, LIMIT, SL, or SL-M
 * @property transactionType Transaction type: BUY or SELL
 * @property tag Custom order tag (max 40 characters)
 * @property disclosedQuantity Disclosed quantity visible in market depth
 * @property triggerPrice Trigger price for stop loss orders
 * @property isAmo Whether this is an After Market Order
 * @property slice Enable automatic order slicing
 * @see <a href="https://upstox.com/developer/api-documentation/place-multi-order">Place Multi Order API</a>
 */
data class MultiOrderParams(
    @SerializedName("correlation_id")
    val correlationId: String,
    val quantity: Int,
    val product: Product,
    val validity: Validity,
    val price: Double,
    @SerializedName("instrument_token")
    val instrumentToken: String,
    @SerializedName("order_type")
    val orderType: OrderType,
    @SerializedName("transaction_type")
    val transactionType: TransactionType,
    val tag: String? = null,
    @SerializedName("disclosed_quantity")
    val disclosedQuantity: Int? = null,
    @SerializedName("trigger_price")
    val triggerPrice: Double? = null,
    @SerializedName("is_amo")
    val isAmo: Boolean? = null,
    val slice: Boolean? = null
)

/**
 * Parameters for modifying an existing order (V3 API).
 *
 * Example usage:
 * ```kotlin
 * val ordersApi = upstox.getOrdersApi()
 *
 * val params = ModifyOrderParams(
 *     orderId = "240108010918222",
 *     quantity = 2,
 *     validity = Validity.DAY,
 *     price = 16.8,
 *     orderType = OrderType.LIMIT,
 *     disclosedQuantity = 0,
 *     triggerPrice = 0.0
 * )
 * val response = ordersApi.modifyOrder(params)
 * ```
 *
 * @property orderId The order ID to modify
 * @property validity Order validity: DAY or IOC
 * @property orderType Order type: MARKET, LIMIT, SL, or SL-M
 * @property price New order price
 * @property triggerPrice New trigger price for stop loss orders
 * @property quantity New order quantity
 * @property disclosedQuantity New disclosed quantity (min 10% of quantity)
 * @see <a href="https://upstox.com/developer/api-documentation/v3/modify-order">Modify Order V3 API</a>
 */
data class ModifyOrderParams(
    @SerializedName("order_id")
    val orderId: String,
    val validity: Validity,
    @SerializedName("order_type")
    val orderType: OrderType,
    val price: Double,
    @SerializedName("trigger_price")
    val triggerPrice: Double,
    val quantity: Int? = null,
    @SerializedName("disclosed_quantity")
    val disclosedQuantity: Int? = null
)

/**
 * Parameters for cancelling multiple orders.
 *
 * Example usage:
 * ```kotlin
 * val ordersApi = upstox.getOrdersApi()
 *
 * // Cancel all open orders
 * val response = ordersApi.cancelMultiOrder()
 *
 * // Cancel orders by segment
 * val segmentResponse = ordersApi.cancelMultiOrder(CancelMultiOrderParams(
 *     segment = Segment.NSE_EQ
 * ))
 *
 * // Cancel orders by tag
 * val tagResponse = ordersApi.cancelMultiOrder(CancelMultiOrderParams(
 *     tag = "my-strategy-tag"
 * ))
 * ```
 *
 * @property segment Market segment filter (only orders in this segment)
 * @property tag Order tag filter (only orders with this tag)
 * @see <a href="https://upstox.com/developer/api-documentation/cancel-multi-order">Cancel Multi Order API</a>
 */
data class CancelMultiOrderParams(
    val segment: Segment? = null,
    val tag: String? = null
)

/**
 * Parameters for exiting all positions.
 *
 * Example usage:
 * ```kotlin
 * val ordersApi = upstox.getOrdersApi()
 *
 * // Exit all open positions
 * val response = ordersApi.exitAllPositions()
 *
 * // Exit positions by segment
 * val segmentResponse = ordersApi.exitAllPositions(ExitAllPositionsParams(
 *     segment = Segment.NSE_FO
 * ))
 * ```
 *
 * @property segment Market segment filter (only positions in this segment)
 * @property tag Order tag filter (valid only for intraday positions)
 * @see <a href="https://upstox.com/developer/api-documentation/exit-all-positions">Exit All Positions API</a>
 */
data class ExitAllPositionsParams(
    val segment: Segment? = null,
    val tag: String? = null
)
