package io.github.sonicalgo.upstox.api

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.exception.UpstoxApiException
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
 * val upstox = Upstox.getInstance()
 *
 * // Get user profile
 * val profile = upstox.getUserApi().getProfile()
 * println("User: ${profile.userName}")
 * println("Email: ${profile.email}")
 *
 * // Get funds and margin
 * val funds = upstox.getUserApi().getFundsAndMargin()
 * println("Available Margin: ${funds.equity?.availableMargin}")
 * ```
 *
 * @see <a href="https://upstox.com/developer/api-documentation/get-profile">Get Profile API</a>
 * @see <a href="https://upstox.com/developer/api-documentation/get-user-fund-margin">Get Funds and Margin API</a>
 */
class UserApi private constructor() {

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
        return ApiClient.get(
            endpoint = Endpoints.GET_PROFILE,
            baseUrl = BASE_URL_V2
        )
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
     * val funds = userApi.getFundsAndMargin(FundSegment.SEC)
     * println("Equity Margin: ${funds.equity?.availableMargin}")
     * ```
     *
     * @param segment Optional segment filter (SEC for Equity, COM for Commodity)
     * @return [FundsAndMargin] Funds and margin details
     * @throws UpstoxApiException if the request fails or service is unavailable
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-user-fund-margin">Get Funds and Margin API</a>
     */
    @JvmOverloads
    fun getFundsAndMargin(segment: FundSegment? = null): FundsAndMargin {
        val queryParams = mutableMapOf<String, String?>()
        segment?.let { queryParams["segment"] = it.name }

        return ApiClient.get(
            endpoint = Endpoints.GET_FUNDS_AND_MARGIN,
            queryParams = queryParams,
            baseUrl = BASE_URL_V2
        )
    }

    internal object Endpoints {
        const val GET_PROFILE = "/user/profile"
        const val GET_FUNDS_AND_MARGIN = "/user/get-funds-and-margin"
    }

    companion object {
        internal val instance by lazy { UserApi() }
    }
}
