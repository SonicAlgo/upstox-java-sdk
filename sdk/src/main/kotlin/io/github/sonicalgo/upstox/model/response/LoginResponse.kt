package io.github.sonicalgo.upstox.model.response

import com.google.gson.annotations.SerializedName

/**
 * Response from the Get Token API.
 *
 * Contains user details and authentication tokens.
 *
 * @property email User's email address
 * @property exchanges List of enabled exchanges (NSE, NFO, BSE, CDS, BFO, BCD)
 * @property products Product types enabled (I, D, CO, MTF)
 * @property broker Broker identifier
 * @property userId Unique user identifier (UCC)
 * @property userName User's registered name
 * @property orderTypes Enabled order types (MARKET, LIMIT, SL, SL-M)
 * @property userType User registration role (typically "individual")
 * @property poa Power of attorney authorization status
 * @property isActive Account active status
 * @property accessToken Authentication token for API requests (valid until 3:30 AM next day)
 * @property extendedToken Token for prolonged read-only API access
 * @see <a href="https://upstox.com/developer/api-documentation/get-token">Get Token API</a>
 */
data class TokenResponse(
    val email: String,
    val exchanges: List<String>,
    val products: List<String>,
    val broker: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("order_types")
    val orderTypes: List<String>,
    @SerializedName("user_type")
    val userType: String,
    val poa: Boolean,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("extended_token")
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
    @SerializedName("authorization_expiry")
    val authorizationExpiry: String,
    @SerializedName("notifier_url")
    val notifierUrl: String
)

/**
 * Response from the Logout API.
 *
 * @property status Whether the logout operation completed successfully
 * @see <a href="https://upstox.com/developer/api-documentation/logout">Logout API</a>
 */
data class LogoutResponse(
    val status: Boolean
)
