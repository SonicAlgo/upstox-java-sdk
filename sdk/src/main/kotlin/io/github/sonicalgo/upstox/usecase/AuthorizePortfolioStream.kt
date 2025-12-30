package io.github.sonicalgo.upstox.usecase

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.websocket.portfolio.AuthorizedRedirectResponse
import io.github.sonicalgo.upstox.websocket.portfolio.PortfolioUpdateType

/**
 * Gets authorized WebSocket URL for Portfolio Stream Feed.
 *
 * The returned URL is valid for one-time use only.
 *
 * @param updateTypes Types of updates to subscribe to. Defaults to all types.
 * @see <a href="https://upstox.com/developer/api-documentation/get-portfolio-stream-feed-authorize">Portfolio Stream Feed Authorize API</a>
 */
@JvmSynthetic
internal fun executeAuthorizePortfolioStream(
    apiClient: ApiClient,
    updateTypes: Set<PortfolioUpdateType> = PortfolioUpdateType.entries.toSet()
): AuthorizedRedirectResponse {
    val types = updateTypes.joinToString(",") { it.value }
    val response: UpstoxResponse<AuthorizedRedirectResponse> = apiClient.get(
        endpoint = "/feed/portfolio-stream-feed/authorize",
        queryParams = mapOf("update_types" to types),
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}
