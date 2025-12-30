package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V3
import io.github.sonicalgo.upstox.common.UpstoxResponse

/** Requests access token generation via V3 webhook flow. */
@JvmSynthetic
internal fun executeRequestAccessToken(
    apiClient: ApiClient,
    clientId: String,
    clientSecret: String
): AccessTokenRequestResponse {
    val response: UpstoxResponse<AccessTokenRequestResponse> = apiClient.post(
        endpoint = "/login/auth/token/request/$clientId",
        body = mapOf("client_secret" to clientSecret),
        overrideBaseUrl = BASE_URL_V3
    )
    return response.dataOrThrow()
}

/**
 * Response from the V3 Access Token Request API.
 *
 * @property authorizationExpiry Expiration time (ms timestamp) for token generation
 * @property notifierUrl Webhook endpoint where access token will be sent
 * @see <a href="https://upstox.com/developer/api-documentation/access-token-request">Access Token Request API</a>
 */
data class AccessTokenRequestResponse(
    @JsonProperty("authorization_expiry")
    val authorizationExpiry: String?,

    @JsonProperty("notifier_url")
    val notifierUrl: String?
)
