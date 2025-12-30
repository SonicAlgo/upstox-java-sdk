package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConfig
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_HFT
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_SANDBOX
import io.github.sonicalgo.upstox.common.UpstoxResponseWithMetadata

/** Cancels an existing order. */
@JvmSynthetic
internal fun executeCancelOrder(
    apiClient: ApiClient,
    config: UpstoxConfig,
    orderId: String
): CancelOrderResponse {
    val baseUrl = if (config.sandboxEnabled) BASE_URL_SANDBOX else BASE_URL_HFT
    val response: UpstoxResponseWithMetadata<CancelOrderData> = apiClient.delete(
        endpoint = "/order/cancel",
        queryParams = mapOf("order_id" to orderId),
        overrideBaseUrl = baseUrl
    )
    val data = response.dataOrThrow()
    return CancelOrderResponse(
        orderId = data.orderId,
        latency = response.metadata?.latency
    )
}

/** Data portion of Cancel Order API response. */
data class CancelOrderData(
    @JsonProperty("order_id")
    val orderId: String?
)

/**
 * Response from Cancel Order V3 API.
 *
 * @property orderId The cancelled order ID
 * @property latency Processing time in milliseconds
 * @see <a href="https://upstox.com/developer/api-documentation/v3/cancel-order">Cancel Order V3 API</a>
 */
data class CancelOrderResponse(
    @JsonProperty("order_id")
    val orderId: String?,

    @JsonProperty("latency")
    val latency: Int?
)
