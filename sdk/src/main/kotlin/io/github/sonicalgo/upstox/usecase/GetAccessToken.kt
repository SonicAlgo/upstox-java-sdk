package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.Exchange
import io.github.sonicalgo.upstox.common.OrderType
import io.github.sonicalgo.upstox.common.Product

/** Exchanges an authorization code for an access token. */
@JvmSynthetic
internal fun executeGetAccessToken(
    apiClient: ApiClient,
    code: String,
    clientId: String,
    clientSecret: String,
    redirectUri: String
): TokenResponse {
    require(code.isNotBlank()) { "code cannot be blank" }
    require(clientId.isNotBlank()) { "clientId cannot be blank" }
    require(clientSecret.isNotBlank()) { "clientSecret cannot be blank" }
    require(redirectUri.isNotBlank()) { "redirectUri cannot be blank" }

    return apiClient.post(
        endpoint = "/login/authorization/token",
        formParams = mapOf(
            "code" to code,
            "client_id" to clientId,
            "client_secret" to clientSecret,
            "redirect_uri" to redirectUri,
            "grant_type" to "authorization_code"
        ),
        overrideBaseUrl = BASE_URL_V2
    )
}

/**
 * Response from the Get Token API.
 *
 * @property email User's email address
 * @property exchanges List of enabled exchanges (NSE, NFO, BSE, CDS, BFO, BCD)
 * @property products Product types enabled (INTRADAY, DELIVERY, COVER_ORDER, MTF)
 * @property broker Broker identifier
 * @property userId Unique user identifier (UCC)
 * @property userName User's registered name
 * @property orderTypes Enabled order types (MARKET, LIMIT, SL, SL_M)
 * @property userType User registration role (typically "individual")
 * @property poa Power of attorney authorization status
 * @property isActive Account active status
 * @property accessToken Authentication token for API requests (valid until 3:30 AM next day)
 * @property extendedToken Token for prolonged read-only API access
 * @see <a href="https://upstox.com/developer/api-documentation/get-token">Get Token API</a>
 */
data class TokenResponse(
    @JsonProperty("email")
    val email: String?,

    @JsonProperty("exchanges")
    val exchanges: List<Exchange>?,

    @JsonProperty("products")
    val products: List<Product>?,

    @JsonProperty("broker")
    val broker: String?,

    @JsonProperty("user_id")
    val userId: String?,

    @JsonProperty("user_name")
    val userName: String?,

    @JsonProperty("order_types")
    val orderTypes: List<OrderType>?,

    @JsonProperty("user_type")
    val userType: String?,

    @JsonProperty("poa")
    val poa: Boolean?,

    @JsonProperty("is_active")
    val isActive: Boolean?,

    @JsonProperty("access_token")
    val accessToken: String?,

    @JsonProperty("extended_token")
    val extendedToken: String?
)