package io.github.sonicalgo.upstox.api

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants
import io.github.sonicalgo.upstox.exception.UpstoxApiException
import io.github.sonicalgo.upstox.model.common.UpstoxResponse
import io.github.sonicalgo.upstox.model.common.UpstoxResponseWithMetadata
import io.github.sonicalgo.upstox.model.request.CancelGttOrderParams
import io.github.sonicalgo.upstox.model.request.ModifyGttOrderParams
import io.github.sonicalgo.upstox.model.request.PlaceGttOrderParams
import io.github.sonicalgo.upstox.model.response.GttOrder
import io.github.sonicalgo.upstox.model.response.GttOrderResponse

/**
 * API module for GTT (Good Till Triggered) order operations.
 *
 * GTT orders remain active until triggered by price conditions or until expiry.
 * They are useful for automated entry, target, and stop-loss strategies.
 *
 * Example usage:
 * ```kotlin
 * val upstox = Upstox.getInstance()
 *
 * // Place a single GTT order
 * val response = upstox.getGttOrdersApi().placeGttOrder(PlaceGttOrderParams(
 *     type = GttType.SINGLE,
 *     quantity = 1,
 *     product = Product.D,
 *     instrumentToken = "NSE_EQ|INE669E01016",
 *     transactionType = TransactionType.BUY,
 *     rules = listOf(
 *         GttRule(
 *             strategy = GttStrategy.ENTRY,
 *             triggerType = GttTriggerType.ABOVE,
 *             triggerPrice = 100.0
 *         )
 *     )
 * ))
 * ```
 *
 * @see <a href="https://upstox.com/developer/api-documentation/place-gtt-order">Place GTT Order API</a>
 */
class GttOrdersApi private constructor() {

    /**
     * Places a GTT (Good Till Triggered) order.
     *
     * GTT orders support two types:
     * - SINGLE: One entry rule
     * - MULTIPLE: Entry with optional target and/or stop-loss
     *
     * Example - Single GTT:
     * ```kotlin
     * val gttApi = upstox.getGttOrdersApi()
     *
     * val response = gttApi.placeGttOrder(PlaceGttOrderParams(
     *     type = GttType.SINGLE,
     *     quantity = 1,
     *     product = Product.D,
     *     instrumentToken = "NSE_EQ|INE669E01016",
     *     transactionType = TransactionType.BUY,
     *     rules = listOf(
     *         GttRule(
     *             strategy = GttStrategy.ENTRY,
     *             triggerType = GttTriggerType.ABOVE,
     *             triggerPrice = 100.0
     *         )
     *     )
     * ))
     * println("GTT Order ID: ${response.data?.gttOrderIds?.first()}")
     * ```
     *
     * Example - Multiple GTT with target and stop-loss:
     * ```kotlin
     * val multiResponse = gttApi.placeGttOrder(PlaceGttOrderParams(
     *     type = GttType.MULTIPLE,
     *     quantity = 1,
     *     product = Product.D,
     *     instrumentToken = "NSE_EQ|INE669E01016",
     *     transactionType = TransactionType.BUY,
     *     rules = listOf(
     *         GttRule(GttStrategy.ENTRY, GttTriggerType.ABOVE, 100.0),
     *         GttRule(GttStrategy.TARGET, GttTriggerType.IMMEDIATE, 110.0),
     *         GttRule(GttStrategy.STOPLOSS, GttTriggerType.IMMEDIATE, 95.0, trailingGap = 2.0)
     *     )
     * ))
     * ```
     *
     * @param params GTT order placement parameters
     * @return [UpstoxResponseWithMetadata]<[GttOrderResponse]> Response with GTT order IDs and latency
     * @throws UpstoxApiException if order placement fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/place-gtt-order">Place GTT Order API</a>
     */
    fun placeGttOrder(params: PlaceGttOrderParams): UpstoxResponseWithMetadata<GttOrderResponse> {
        val response: UpstoxResponse<GttOrderResponse> = ApiClient.post(
            endpoint = Endpoints.PLACE_GTT_ORDER,
            body = params,
            baseUrl = UpstoxConstants.BASE_URL_V3,
            unwrap = false
        )
        return UpstoxResponseWithMetadata(status = response.status, data = response.data)
    }

    /**
     * Modifies an existing GTT order.
     *
     * Note: Quantity cannot be modified once the order reaches OPEN status.
     *
     * Example:
     * ```kotlin
     * val gttApi = upstox.getGttOrdersApi()
     *
     * val response = gttApi.modifyGttOrder(ModifyGttOrderParams(
     *     gttOrderId = "GTT-C25270200137952",
     *     type = GttType.SINGLE,
     *     quantity = 1,
     *     rules = listOf(
     *         GttRule(
     *             strategy = GttStrategy.ENTRY,
     *             triggerType = GttTriggerType.ABOVE,
     *             triggerPrice = 105.0
     *         )
     *     )
     * ))
     * println("Modified GTT Order: ${response.data?.gttOrderIds?.first()}")
     * ```
     *
     * @param params GTT order modification parameters
     * @return [UpstoxResponseWithMetadata]<[GttOrderResponse]> Response with modified GTT order IDs and latency
     * @throws UpstoxApiException if modification fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/modify-gtt-order">Modify GTT Order API</a>
     */
    fun modifyGttOrder(params: ModifyGttOrderParams): UpstoxResponseWithMetadata<GttOrderResponse> {
        val response: UpstoxResponse<GttOrderResponse> = ApiClient.put(
            endpoint = Endpoints.MODIFY_GTT_ORDER,
            body = params,
            baseUrl = UpstoxConstants.BASE_URL_V3,
            unwrap = false
        )
        return UpstoxResponseWithMetadata(status = response.status, data = response.data)
    }

    /**
     * Cancels a GTT order.
     *
     * Example:
     * ```kotlin
     * val gttApi = upstox.getGttOrdersApi()
     *
     * val response = gttApi.cancelGttOrder(CancelGttOrderParams(
     *     gttOrderId = "GTT-C25280200137522"
     * ))
     * println("Cancelled GTT Order: ${response.data?.gttOrderIds?.first()}")
     * ```
     *
     * @param params GTT order cancellation parameters
     * @return [UpstoxResponseWithMetadata]<[GttOrderResponse]> Response with cancelled GTT order IDs and latency
     * @throws UpstoxApiException if cancellation fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/cancel-gtt-order">Cancel GTT Order API</a>
     */
    fun cancelGttOrder(params: CancelGttOrderParams): UpstoxResponseWithMetadata<GttOrderResponse> {
        val response: UpstoxResponse<GttOrderResponse> = ApiClient.delete(
            endpoint = Endpoints.CANCEL_GTT_ORDER,
            body = params,
            baseUrl = UpstoxConstants.BASE_URL_V3,
            unwrap = false
        )
        return UpstoxResponseWithMetadata(status = response.status, data = response.data)
    }

    /**
     * Gets details of GTT orders.
     *
     * If no GTT order ID is provided, returns all GTT orders.
     *
     * Example - Get specific GTT order:
     * ```kotlin
     * val gttApi = upstox.getGttOrdersApi()
     *
     * val orders = gttApi.getGttOrderDetails("GTT-C25280200071351")
     * orders.forEach { order ->
     *     println("GTT ${order.gttOrderId}: ${order.type}")
     *     order.rules.forEach { rule ->
     *         println("  ${rule.strategy}: ${rule.status} @ ${rule.triggerPrice}")
     *     }
     * }
     * ```
     *
     * Example - Get all GTT orders:
     * ```kotlin
     * val allOrders = gttApi.getGttOrderDetails()
     * ```
     *
     * @param gttOrderId Optional GTT order ID to get specific order details
     * @return List of [GttOrder] details
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-gtt-order-details">Get GTT Order Details API</a>
     */
    @JvmOverloads
    fun getGttOrderDetails(gttOrderId: String? = null): List<GttOrder> {
        val queryParams = mutableMapOf<String, String?>()
        gttOrderId?.let { queryParams["gtt_order_id"] = it }

        return ApiClient.get(
            endpoint = Endpoints.GET_GTT_ORDER_DETAILS,
            queryParams = queryParams,
            baseUrl = UpstoxConstants.BASE_URL_V3
        )
    }

    internal object Endpoints {
        const val PLACE_GTT_ORDER = "/order/gtt/place"
        const val MODIFY_GTT_ORDER = "/order/gtt/modify"
        const val CANCEL_GTT_ORDER = "/order/gtt/cancel"
        const val GET_GTT_ORDER_DETAILS = "/order/gtt"
    }

    companion object {
        internal val instance by lazy { GttOrdersApi() }
    }
}
