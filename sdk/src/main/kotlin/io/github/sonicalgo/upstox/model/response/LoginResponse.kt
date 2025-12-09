package io.github.sonicalgo.upstox.model.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.model.enums.Exchange
import io.github.sonicalgo.upstox.model.enums.OrderType
import io.github.sonicalgo.upstox.model.enums.Product

/**
 * Response from the Get Token API.
 *
 * Contains user details and authentication tokens.
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
    val email: String,

    @JsonProperty("exchanges")
    val exchanges: List<Exchange>,

    @JsonProperty("products")
    val products: List<Product>,

    @JsonProperty("broker")
    val broker: String,

    @JsonProperty("user_id")
    val userId: String,

    @JsonProperty("user_name")
    val userName: String,

    @JsonProperty("order_types")
    val orderTypes: List<OrderType>,

    @JsonProperty("user_type")
    val userType: String,

    @JsonProperty("poa")
    val poa: Boolean,

    @JsonProperty("is_active")
    val isActive: Boolean,

    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("extended_token")
    val extendedToken: String? = null
)

/**
 * Response from the V3 Access Token Request API.
 *
 * Contains information about the token generation process.
 *
 * @property authorizationExpiry Expiration time (ms timestamp) for token generation
 * @property notifierUrl Webhook endpoint where access token will be sent
 * @see <a href="https://upstox.com/developer/api-documentation/access-token-request">Access Token Request API</a>
 */
data class AccessTokenRequestResponse(
    @JsonProperty("authorization_expiry")
    val authorizationExpiry: String,

    @JsonProperty("notifier_url")
    val notifierUrl: String
)

/**
 * Response from the Logout API.
 *
 * @property status Whether the logout operation completed successfully
 * @see <a href="https://upstox.com/developer/api-documentation/logout">Logout API</a>
 */
data class LogoutResponse(
    @JsonProperty("status")
    val status: Boolean
)
