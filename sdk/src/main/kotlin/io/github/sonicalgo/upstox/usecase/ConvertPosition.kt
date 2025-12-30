package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.Product
import io.github.sonicalgo.upstox.common.TransactionType

/** Converts a position from one product type to another. */
@JvmSynthetic
internal fun executeConvertPosition(
    apiClient: ApiClient,
    params: ConvertPositionParams
): ConvertPositionResponse {
    val response: UpstoxResponse<ConvertPositionResponse> = apiClient.put(
        endpoint = "/portfolio/convert-position",
        body = params,
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}

/**
 * Parameters for converting a position.
 *
 * @property instrumentToken Instrument key
 * @property newProduct Target product type
 * @property oldProduct Current product type
 * @property transactionType Transaction type of the position
 * @property quantity Quantity to convert
 */
@GenerateBuilder
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

/**
 * Response from Convert Position API.
 *
 * @property status Conversion status message
 * @see <a href="https://upstox.com/developer/api-documentation/convert-positions">Convert Positions API</a>
 */
data class ConvertPositionResponse(
    @JsonProperty("status")
    val status: String?
)
