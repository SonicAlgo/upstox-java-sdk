package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConfig
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_HFT
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_SANDBOX
import io.github.sonicalgo.upstox.common.UpstoxResponseWithMetadata
import io.github.sonicalgo.upstox.common.OrderType
import io.github.sonicalgo.upstox.common.Product
import io.github.sonicalgo.upstox.common.TransactionType
import io.github.sonicalgo.upstox.common.Validity

/** Places a single order via HFT endpoint. */
@JvmSynthetic
internal fun executePlaceOrder(
    apiClient: ApiClient,
    config: UpstoxConfig,
    params: PlaceOrderParams
): PlaceOrderResponse {
    val baseUrl = if (config.sandboxEnabled) BASE_URL_SANDBOX else BASE_URL_HFT
    val response: UpstoxResponseWithMetadata<PlaceOrderData> = apiClient.post(
        endpoint = "/order/place",
        body = params,
        overrideBaseUrl = baseUrl
    )
    val data = response.dataOrThrow()
    return PlaceOrderResponse(
        orderIds = data.orderIds,
        latency = response.metadata?.latency
    )
}

/**
 * Parameters for placing a single order (V3 API).
 *
 * @property instrumentToken Key of the instrument (e.g., "NSE_EQ|INE669E01016")
 * @property quantity Quantity to order (lots for commodity segments)
 * @property product Product type: INTRADAY, DELIVERY, or MTF
 * @property validity Order validity: DAY or IOC
 * @property price Order price (0 for MARKET orders)
 * @property orderType Order type: MARKET, LIMIT, SL, or SL_M
 * @property transactionType Transaction type: BUY or SELL
 * @property disclosedQuantity Quantity visible in market depth (0 for full visibility)
 * @property triggerPrice Trigger price for stop loss orders (0 for non-SL)
 * @property isAmo Whether this is an After Market Order
 * @property tag Custom order identifier (max 40 characters)
 * @property slice Enable automatic order slicing for large orders
 */
@GenerateBuilder
data class PlaceOrderParams(
    @JsonProperty("instrument_token")
    val instrumentToken: String,

    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("product")
    val product: Product,

    @JsonProperty("validity")
    val validity: Validity,

    @JsonProperty("price")
    val price: Double,

    @JsonProperty("order_type")
    val orderType: OrderType,

    @JsonProperty("transaction_type")
    val transactionType: TransactionType,

    @JsonProperty("disclosed_quantity")
    val disclosedQuantity: Int = 0,

    @JsonProperty("trigger_price")
    val triggerPrice: Double = 0.0,

    @JsonProperty("is_amo")
    val isAmo: Boolean = false,

    @JsonProperty("tag")
    val tag: String? = null,

    @JsonProperty("slice")
    val slice: Boolean? = null
)

/** Data portion of Place Order API response. */
data class PlaceOrderData(
    @JsonProperty("order_ids")
    val orderIds: List<String>?
)

/**
 * Response from Place Order V3 API.
 *
 * @property orderIds List of order IDs (multiple when slice is enabled)
 * @property latency Processing time in milliseconds
 * @see <a href="https://upstox.com/developer/api-documentation/v3/place-order">Place Order V3 API</a>
 */
data class PlaceOrderResponse(
    @JsonProperty("order_ids")
    val orderIds: List<String>?,

    @JsonProperty("latency")
    val latency: Int?
)