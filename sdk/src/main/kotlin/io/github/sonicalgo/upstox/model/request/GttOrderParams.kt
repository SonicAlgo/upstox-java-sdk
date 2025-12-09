package io.github.sonicalgo.upstox.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.model.enums.*

/**
 * Parameters for placing a GTT (Good Till Triggered) order.
 *
 * GTT orders remain active until triggered by price conditions or until expiry.
 *
 * Example - Single GTT order:
 * ```kotlin
 * val gttApi = upstox.getGttOrdersApi()
 *
 * val params = PlaceGttOrderParams(
 *     type = GttType.SINGLE,
 *     quantity = 1,
 *     product = Product.DELIVERY,
 *     instrumentToken = "NSE_EQ|INE669E01016",
 *     transactionType = TransactionType.BUY,
 *     rules = listOf(
 *         GttRule(
 *             strategy = GttStrategy.ENTRY,
 *             triggerType = GttTriggerType.ABOVE,
 *             triggerPrice = 100.0
 *         )
 *     )
 * )
 * val response = gttApi.placeGttOrder(params)
 * ```
 *
 * Example - Multiple GTT order with target and stop loss:
 * ```kotlin
 * val params = PlaceGttOrderParams(
 *     type = GttType.MULTIPLE,
 *     quantity = 1,
 *     product = Product.DELIVERY,
 *     instrumentToken = "NSE_EQ|INE669E01016",
 *     transactionType = TransactionType.BUY,
 *     rules = listOf(
 *         GttRule(strategy = GttStrategy.ENTRY, triggerType = GttTriggerType.ABOVE, triggerPrice = 100.0),
 *         GttRule(strategy = GttStrategy.TARGET, triggerType = GttTriggerType.IMMEDIATE, triggerPrice = 110.0),
 *         GttRule(strategy = GttStrategy.STOP_LOSS, triggerType = GttTriggerType.IMMEDIATE, triggerPrice = 95.0)
 *     )
 * )
 * ```
 *
 * @property type GTT order type: SINGLE (1 rule) or MULTIPLE (2-3 rules)
 * @property quantity Order quantity
 * @property product Product type: INTRADAY, DELIVERY, or MTF
 * @property instrumentToken Instrument key identifier
 * @property transactionType Transaction type: BUY or SELL
 * @property rules List of GTT rules (SINGLE: 1 ENTRY, MULTIPLE: ENTRY + TARGET/STOP_LOSS)
 * @see <a href="https://upstox.com/developer/api-documentation/place-gtt-order">Place GTT Order API</a>
 */
data class PlaceGttOrderParams(
    @JsonProperty("type")
    val type: GttType,

    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("product")
    val product: Product,

    @JsonProperty("instrument_token")
    val instrumentToken: String,

    @JsonProperty("transaction_type")
    val transactionType: TransactionType,

    @JsonProperty("rules")
    val rules: List<GttRule>
)

/**
 * GTT order rule definition.
 *
 * Defines the trigger conditions for a GTT order.
 *
 * @property strategy Rule strategy: ENTRY, TARGET, or STOP_LOSS
 * @property triggerType Trigger type: ABOVE, BELOW, or IMMEDIATE
 * @property triggerPrice Price at which the order should be triggered
 * @property trailingGap Gap for trailing stop-loss (STOP_LOSS only, min 10% of LTP-to-trigger diff)
 */
data class GttRule(
    @JsonProperty("strategy")
    val strategy: GttStrategy,

    @JsonProperty("trigger_type")
    val triggerType: GttTriggerType,

    @JsonProperty("trigger_price")
    val triggerPrice: Double,

    @JsonProperty("trailing_gap")
    val trailingGap: Double? = null
)

/**
 * Parameters for modifying a GTT order.
 *
 * Example usage:
 * ```kotlin
 * val gttApi = upstox.getGttOrdersApi()
 *
 * val params = ModifyGttOrderParams(
 *     gttOrderId = "GTT-C25270200137952",
 *     type = GttType.SINGLE,
 *     quantity = 1,
 *     rules = listOf(
 *         GttRule(strategy = GttStrategy.ENTRY, triggerType = GttTriggerType.ABOVE, triggerPrice = 7.3)
 *     )
 * )
 * val response = gttApi.modifyGttOrder(params)
 * ```
 *
 * @property gttOrderId The GTT order ID to modify (starts with "GTT-")
 * @property type GTT order type
 * @property quantity Order quantity (cannot modify after OPEN status)
 * @property rules Updated list of GTT rules
 * @see <a href="https://upstox.com/developer/api-documentation/modify-gtt-order">Modify GTT Order API</a>
 */
data class ModifyGttOrderParams(
    @JsonProperty("gtt_order_id")
    val gttOrderId: String,

    @JsonProperty("type")
    val type: GttType,

    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("rules")
    val rules: List<GttRule>
)

/**
 * Parameters for cancelling a GTT order.
 *
 * Example usage:
 * ```kotlin
 * val gttApi = upstox.getGttOrdersApi()
 *
 * val params = CancelGttOrderParams(gttOrderId = "GTT-C25280200137522")
 * val response = gttApi.cancelGttOrder(params)
 * ```
 *
 * @property gttOrderId The GTT order ID to cancel (starts with "GTT-")
 * @see <a href="https://upstox.com/developer/api-documentation/cancel-gtt-order">Cancel GTT Order API</a>
 */
data class CancelGttOrderParams(
    @JsonProperty("gtt_order_id")
    val gttOrderId: String
)
