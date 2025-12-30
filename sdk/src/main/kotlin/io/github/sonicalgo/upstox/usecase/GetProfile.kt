package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.Exchange
import io.github.sonicalgo.upstox.common.OrderType
import io.github.sonicalgo.upstox.common.Product

/** Retrieves the profile of the authenticated user. */
@JvmSynthetic
internal fun executeGetProfile(apiClient: ApiClient): UserProfile {
    val response: UpstoxResponse<UserProfile> = apiClient.get(
        endpoint = "/user/profile",
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}

/**
 * User profile information.
 *
 * @property email User's email address
 * @property exchanges List of enabled exchanges (NSE, NFO, BSE, CDS, BFO, BCD)
 * @property products Product types enabled (INTRADAY, DELIVERY, COVER_ORDER, MTF)
 * @property broker Broker identifier
 * @property userId Unique user identifier (UCC)
 * @property userName User's registered name
 * @property orderTypes Supported order types (MARKET, LIMIT, SL, SL_M)
 * @property userType User registration role (typically "individual")
 * @property poa Power of attorney authorization status
 * @property ddpi DDPI (Demat Debit and Pledge Instruction) authorization status
 * @property isActive Account activity status
 * @see <a href="https://upstox.com/developer/api-documentation/get-profile">Get Profile API</a>
 */
data class UserProfile(
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

    @JsonProperty("ddpi")
    val ddpi: Boolean?,

    @JsonProperty("is_active")
    val isActive: Boolean?
)