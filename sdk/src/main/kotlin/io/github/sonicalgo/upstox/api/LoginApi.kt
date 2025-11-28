package io.github.sonicalgo.upstox.api

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants
import io.github.sonicalgo.upstox.exception.UpstoxApiException
import io.github.sonicalgo.upstox.model.request.AccessTokenRequestParams
import io.github.sonicalgo.upstox.model.request.AuthorizeParams
import io.github.sonicalgo.upstox.model.request.GetTokenParams
import io.github.sonicalgo.upstox.model.response.AccessTokenRequestResponse
import io.github.sonicalgo.upstox.model.response.TokenResponse
import java.net.URLEncoder

/**
 * API module for authentication and login operations.
 *
 * Provides methods for OAuth authorization flow, token management, and logout.
 *
 * Example - OAuth flow:
 * ```kotlin
 * val upstox = Upstox.getInstance()
 *
 * // Step 1: Get authorization URL and redirect user
 * val authUrl = upstox.getLoginApi().getAuthorizationUrl(AuthorizeParams(
 *     clientId = "your-api-key",
 *     redirectUri = "https://yourapp.com/callback"
 * ))
 *
 * // Step 2: After user authorizes, exchange code for token
 * val tokenResponse = upstox.getLoginApi().getToken(GetTokenParams(
 *     code = "authorization-code-from-callback",
 *     clientId = "your-api-key",
 *     clientSecret = "your-api-secret",
 *     redirectUri = "https://yourapp.com/callback"
 * ))
 *
 * // Step 3: Use the access token for subsequent API calls
 * upstox.setAccessToken(tokenResponse.accessToken)
 * ```
 *
 * @see <a href="https://upstox.com/developer/api-documentation/authorize">Authorize API</a>
 * @see <a href="https://upstox.com/developer/api-documentation/get-token">Get Token API</a>
 */
class LoginApi private constructor() {

    /**
     * Constructs the authorization URL for initiating OAuth flow.
     *
     * Redirect the user to this URL to begin the authorization process.
     * After successful authentication, the user will be redirected to
     * the provided redirect_uri with an authorization code.
     *
     * Example:
     * ```kotlin
     * val loginApi = upstox.getLoginApi()
     *
     * val authUrl = loginApi.getAuthorizationUrl(AuthorizeParams(
     *     clientId = "your-api-key",
     *     redirectUri = "https://yourapp.com/callback",
     *     state = "random-state-for-csrf-protection"
     * ))
     * // Redirect user to authUrl
     * ```
     *
     * @param params Authorization parameters
     * @return The authorization URL to redirect the user to
     *
     * @see <a href="https://upstox.com/developer/api-documentation/authorize">Authorize API</a>
     */
    fun getAuthorizationUrl(params: AuthorizeParams): String {
        val baseUrl = "${UpstoxConstants.BASE_URL_V2}${Endpoints.AUTHORIZATION_DIALOG}"
        val queryParams = buildString {
            append("response_type=${params.responseType}")
            append("&client_id=${URLEncoder.encode(params.clientId, "UTF-8")}")
            append("&redirect_uri=${URLEncoder.encode(params.redirectUri, "UTF-8")}")
            if (params.state != null) {
                append("&state=${URLEncoder.encode(params.state, "UTF-8")}")
            }
        }
        return "$baseUrl?$queryParams"
    }

    /**
     * Exchanges an authorization code for an access token.
     *
     * Call this method after the user has authorized your application
     * and you have received the authorization code from the callback.
     *
     * The returned access token is valid until 3:30 AM the following day.
     *
     * Example:
     * ```kotlin
     * val loginApi = upstox.getLoginApi()
     *
     * val tokenResponse = loginApi.getToken(GetTokenParams(
     *     code = "authorization-code",
     *     clientId = "your-api-key",
     *     clientSecret = "your-api-secret",
     *     redirectUri = "https://yourapp.com/callback"
     * ))
     * println("Access Token: ${tokenResponse.accessToken}")
     * println("User: ${tokenResponse.userName}")
     * ```
     *
     * @param params Token request parameters
     * @return [TokenResponse] Token response containing access token and user details
     * @throws UpstoxApiException if token exchange fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-token">Get Token API</a>
     */
    fun getToken(params: GetTokenParams): TokenResponse {
        return ApiClient.post(
            endpoint = Endpoints.GET_TOKEN,
            formParams = mapOf(
                "code" to params.code,
                "client_id" to params.clientId,
                "client_secret" to params.clientSecret,
                "redirect_uri" to params.redirectUri,
                "grant_type" to params.grantType
            ),
            baseUrl = UpstoxConstants.BASE_URL_V2,
            unwrap = false
        )
    }

    /**
     * Requests access token generation via V3 webhook flow.
     *
     * This is an alternative authentication method that uses webhook
     * notification to deliver the access token.
     *
     * Example:
     * ```kotlin
     * val loginApi = upstox.getLoginApi()
     *
     * val response = loginApi.requestAccessToken(
     *     clientId = "your-api-key",
     *     params = AccessTokenRequestParams(clientSecret = "your-api-secret")
     * )
     * println("Token will be sent to: ${response.notifierUrl}")
     * ```
     *
     * @param clientId The API key obtained during app generation
     * @param params Request parameters including client secret
     * @return [AccessTokenRequestResponse] Response containing webhook URL and expiry information
     * @throws UpstoxApiException if the request fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/access-token-request">Access Token Request API</a>
     */
    fun requestAccessToken(clientId: String, params: AccessTokenRequestParams): AccessTokenRequestResponse {
        return ApiClient.post(
            endpoint = "${Endpoints.REQUEST_ACCESS_TOKEN_BASE}/$clientId",
            body = params,
            baseUrl = UpstoxConstants.BASE_URL_V3
        )
    }

    /**
     * Logs out the user and invalidates the current session.
     *
     * After logout, the access token becomes invalid and cannot be used
     * for subsequent API calls. A new authentication is required.
     *
     * Example:
     * ```kotlin
     * val loginApi = upstox.getLoginApi()
     *
     * val success = loginApi.logout()
     * if (success) {
     *     println("Logged out successfully")
     * }
     * ```
     *
     * @return true if logout was successful
     * @throws UpstoxApiException if logout fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/logout">Logout API</a>
     */
    fun logout(): Boolean {
        return ApiClient.delete(
            endpoint = Endpoints.LOGOUT,
            baseUrl = UpstoxConstants.BASE_URL_V2
        )
    }

    internal object Endpoints {
        const val AUTHORIZATION_DIALOG = "/login/authorization/dialog"
        const val GET_TOKEN = "/login/authorization/token"
        const val REQUEST_ACCESS_TOKEN_BASE = "/login/auth/token/request"
        const val LOGOUT = "/logout"
    }

    companion object {
        internal val instance by lazy { LoginApi() }
    }
}
