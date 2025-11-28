package io.github.sonicalgo.upstox.model.request

import com.google.gson.annotations.SerializedName

/**
 * Parameters for the OAuth authorization URL.
 *
 * Used to construct the authorization URL for user login.
 * After successful authentication, user is redirected to the redirect_uri
 * with an authorization code.
 *
 * Example usage:
 * ```kotlin
 * val loginApi = upstox.getLoginApi()
 *
 * val params = AuthorizeParams(
 *     clientId = "your-api-key",
 *     redirectUri = "https://yourapp.com/callback",
 *     state = "random-state-value"
 * )
 * val authUrl = loginApi.getAuthorizationUrl(params)
 * // Redirect user to authUrl
 * ```
 *
 * @property clientId The API key obtained during the app generation process
 * @property redirectUri URL to redirect user post authentication (must match app config)
 * @property state Optional parameter for maintaining state between request and callback
 * @see <a href="https://upstox.com/developer/api-documentation/authorize">Authorize API</a>
 */
data class AuthorizeParams(
    @SerializedName("client_id")
    val clientId: String,
    @SerializedName("redirect_uri")
    val redirectUri: String,
    val state: String? = null
) {
    /** Response type - always "code" for authorization code flow. */
    @SerializedName("response_type")
    val responseType: String = "code"
}

/**
 * Parameters for exchanging authorization code for access token.
 *
 * After user authorizes the app, exchange the received authorization code
 * for an access token using these parameters.
 *
 * Example usage:
 * ```kotlin
 * val loginApi = upstox.getLoginApi()
 *
 * val params = GetTokenParams(
 *     code = "authorization-code-from-callback",
 *     clientId = "your-api-key",
 *     clientSecret = "your-api-secret",
 *     redirectUri = "https://yourapp.com/callback"
 * )
 * val tokenResponse = loginApi.getToken(params)
 * ```
 *
 * @property code The unique authorization code received from the authorize callback
 * @property clientId The API key obtained during app generation
 * @property clientSecret The API secret (keep confidential, never expose client-side)
 * @property redirectUri The redirect URI used during authorization (must match exactly)
 * @property grantType Grant type - always "authorization_code" for this flow
 * @see <a href="https://upstox.com/developer/api-documentation/get-token">Get Token API</a>
 */
data class GetTokenParams(
    val code: String,
    @SerializedName("client_id")
    val clientId: String,
    @SerializedName("client_secret")
    val clientSecret: String,
    @SerializedName("redirect_uri")
    val redirectUri: String,
    @SerializedName("grant_type")
    val grantType: String = "authorization_code"
)

/**
 * Parameters for V3 access token request.
 *
 * Used for the newer V3 login flow that uses webhook notification.
 *
 * Example usage:
 * ```kotlin
 * val loginApi = upstox.getLoginApi()
 *
 * val params = AccessTokenRequestParams(
 *     clientSecret = "your-api-secret"
 * )
 * val response = loginApi.requestAccessToken("your-client-id", params)
 * ```
 *
 * @property clientSecret The API secret (confidential, known only to the application)
 * @see <a href="https://upstox.com/developer/api-documentation/access-token-request">Access Token Request API</a>
 */
data class AccessTokenRequestParams(
    @SerializedName("client_secret")
    val clientSecret: String
)
