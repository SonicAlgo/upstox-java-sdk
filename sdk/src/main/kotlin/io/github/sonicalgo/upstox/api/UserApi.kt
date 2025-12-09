package io.github.sonicalgo.upstox.api

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.exception.UpstoxApiException
import io.github.sonicalgo.upstox.model.common.UpstoxResponse
import io.github.sonicalgo.upstox.model.enums.FundSegment
import io.github.sonicalgo.upstox.model.response.FundsAndMargin
import io.github.sonicalgo.upstox.model.response.UserProfile

/**
 * API module for user-related operations.
 *
 * Provides methods for retrieving user profile and fund/margin information.
 *
 * Example usage:
 * ```kotlin
 * val userApi = upstox.getUserApi()
 *
 * // Get user profile
 * val profile = userApi.getProfile()
 * println("User: ${profile.userName}")
 * println("Email: ${profile.email}")
 *
 * // Get funds and margin
 * val funds = userApi.getFundsAndMargin()
 * println("Available Margin: ${funds.equity?.availableMargin}")
 * ```
 *
 * @see <a href="https://upstox.com/developer/api-documentation/get-profile">Get Profile API</a>
 * @see <a href="https://upstox.com/developer/api-documentation/get-user-fund-margin">Get Funds and Margin API</a>
 */
class UserApi internal constructor(private val apiClient: ApiClient) {

    /**
     * Retrieves the profile of the authenticated user.
     *
     * Returns details including user ID, name, email, enabled exchanges,
     * products, order types, and account status.
     *
     * Example:
     * ```kotlin
     * val userApi = upstox.getUserApi()
     *
     * val profile = userApi.getProfile()
     * println("User ID: ${profile.userId}")
     * println("Name: ${profile.userName}")
     * println("Exchanges: ${profile.exchanges}")
     * println("Active: ${profile.isActive}")
     * ```
     *
     * @return User profile information
     * @throws UpstoxApiException if the request fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-profile">Get Profile API</a>
     */
    fun getProfile(): UserProfile {
        val response: UpstoxResponse<UserProfile> = apiClient.get(
            endpoint = Endpoints.GET_PROFILE,
            overrideBaseUrl = BASE_URL_V2
        )
        return response.dataOrThrow()
    }

    /**
     * Retrieves the funds and margin details for the user.
     *
     * Returns information about available margin, used margin, span margin,
     * exposure margin, and other fund-related details across equity and
     * commodity segments.
     *
     * Note: This service is accessible from 5:30 AM to 12:00 AM IST daily.
     *
     * Example - Get all segments:
     * ```kotlin
     * val userApi = upstox.getUserApi()
     *
     * val funds = userApi.getFundsAndMargin()
     * println("Equity Available: ${funds.equity?.availableMargin}")
     * println("Commodity Available: ${funds.commodity?.availableMargin}")
     * ```
     *
     * Example - Get specific segment:
     * ```kotlin
     * val funds = userApi.getFundsAndMargin(FundSegment.SECURITIES)
     * println("Equity Margin: ${funds.equity?.availableMargin}")
     * ```
     *
     * @param segment Optional segment filter (SECURITIES for Equity, COMMODITY for Commodity)
     * @return [FundsAndMargin] Funds and margin details
     * @throws UpstoxApiException if the request fails or service is unavailable
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-user-fund-margin">Get Funds and Margin API</a>
     */
    @JvmOverloads
    fun getFundsAndMargin(segment: FundSegment? = null): FundsAndMargin {
        val queryParams = mutableMapOf<String, String?>()
        segment?.let { queryParams["segment"] = it.name }

        val response: UpstoxResponse<FundsAndMargin> = apiClient.get(
            endpoint = Endpoints.GET_FUNDS_AND_MARGIN,
            queryParams = queryParams,
            overrideBaseUrl = BASE_URL_V2
        )
        return response.dataOrThrow()
    }

    internal object Endpoints {
        const val GET_PROFILE = "/user/profile"
        const val GET_FUNDS_AND_MARGIN = "/user/get-funds-and-margin"
    }
}
