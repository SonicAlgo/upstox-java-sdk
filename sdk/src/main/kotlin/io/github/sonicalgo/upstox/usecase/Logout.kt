package io.github.sonicalgo.upstox.usecase

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConfig
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse

/**
 * Logs out the user and invalidates the current session.
 *
 * **Important:** On successful logout, this function clears the access token from the SDK config.
 * After logout, all subsequent API calls will fail until a new access token is obtained via
 * [executeGetAccessToken] or [executeRequestAccessToken].
 *
 * Note: WebSocket clients created from this Upstox instance will also stop working after logout
 * since they depend on the access token for authentication.
 *
 * @return true if logout was successful, the access token is cleared
 * @throws UpstoxApiException if the logout request fails (token is NOT cleared in this case)
 */
@JvmSynthetic
internal fun executeLogout(apiClient: ApiClient, config: UpstoxConfig): Boolean {
    val response: UpstoxResponse<Boolean> = apiClient.delete(
        endpoint = "/logout",
        overrideBaseUrl = BASE_URL_V2
    )
    val success = response.dataOrThrow()
    // Clear access token only on successful logout
    // This ensures subsequent API calls fail fast rather than making unauthorized requests
    if (success) {
        config.accessToken = ""
    }
    return success
}
