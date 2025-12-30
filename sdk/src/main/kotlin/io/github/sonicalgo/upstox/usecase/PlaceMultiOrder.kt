package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConfig
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_SANDBOX
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.MultiOrderError
import io.github.sonicalgo.upstox.common.MultiOrderSummary
import io.github.sonicalgo.upstox.common.OrderType
import io.github.sonicalgo.upstox.common.Product
import io.github.sonicalgo.upstox.common.TransactionType
import io.github.sonicalgo.upstox.common.Validity
import io.github.sonicalgo.upstox.validation.Validators

private const val MAX_MULTI_ORDER_COUNT = 25

/** Places multiple orders in a single request. */
@JvmSynthetic
internal fun executePlaceMultiOrder(
    apiClient: ApiClient,
    config: UpstoxConfig,
    orders: List<MultiOrderParams>
): PlaceMultiOrderResponse {
    Validators.validateListSize(orders, MAX_MULTI_ORDER_COUNT, "placeMultiOrder")
    val baseUrl = if (config.sandboxEnabled) BASE_URL_SANDBOX else BASE_URL_V2
    return apiClient.post(
        endpoint = "/order/multi/place",
        body = orders,
        overrideBaseUrl = baseUrl
    )
}

/**
 * Parameters for placing multiple orders in a single request.
 *
 * @property correlationId Unique identifier for this order (max 20 chars)
 * @property quantity Quantity to order
 * @property product Product type: INTRADAY, DELIVERY, or MTF
 * @property validity Order validity: DAY or IOC
 * @property price Order price (0 for MARKET orders)
 * @property instrumentToken Instrument key identifier
 * @property orderType Order type: MARKET, LIMIT, SL, or SL_M
 * @property transactionType Transaction type: BUY or SELL
 * @property tag Custom order tag (max 40 characters)
 * @property disclosedQuantity Quantity visible in market depth (default 0 for full visibility)
 * @property triggerPrice Trigger price for stop loss orders (default 0.0 for non-SL)
 * @property isAmo After Market Order flag (default false; system automatically infers value)
 * @property slice Enable automatic order slicing (default null to omit)
 */
@GenerateBuilder
data class MultiOrderParams(
    @JsonProperty("correlation_id")
    val correlationId: String,

    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("product")
    val product: Product,

    @JsonProperty("validity")
    val validity: Validity,

    @JsonProperty("price")
    val price: Double,

    @JsonProperty("instrument_token")
    val instrumentToken: String,

    @JsonProperty("order_type")
    val orderType: OrderType,

    @JsonProperty("transaction_type")
    val transactionType: TransactionType,

    @JsonProperty("tag")
    val tag: String? = null,

    @JsonProperty("disclosed_quantity")
    val disclosedQuantity: Int = 0,

    @JsonProperty("trigger_price")
    val triggerPrice: Double = 0.0,

    @JsonProperty("is_amo")
    val isAmo: Boolean = false,

    @JsonProperty("slice")
    val slice: Boolean? = null
)

/** Individual order result in Place Multi Order response. */
data class MultiOrderPlaceItem(
    @JsonProperty("correlation_id")
    val correlationId: String?,

    @JsonProperty("order_id")
    val orderId: String?
)

/**
 * Response from Place Multi Order API.
 *
 * @property status Response status: "success", "partial_success", or "error"
 * @property data List of successfully placed orders
 * @property errors List of errors for failed orders
 * @property summary Summary statistics for the batch operation
 * @see <a href="https://upstox.com/developer/api-documentation/place-multi-order">Place Multi Order API</a>
 */
data class PlaceMultiOrderResponse(
    @JsonProperty("status")
    val status: String?,

    @JsonProperty("data")
    val data: List<MultiOrderPlaceItem>?,

    @JsonProperty("errors")
    val errors: List<MultiOrderError>?,

    @JsonProperty("summary")
    val summary: MultiOrderSummary?
) {
    val isSuccess: Boolean get() = status == "success"
    val isPartialSuccess: Boolean get() = status == "partial_success"
    val isError: Boolean get() = status != "success" && status != "partial_success"
}
