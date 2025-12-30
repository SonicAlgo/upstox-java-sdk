package io.github.sonicalgo.upstox.usecase

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V3
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.websocket.portfolio.AuthorizedRedirectResponse

/**
 * Gets authorized WebSocket URL for Market Data Feed V3.
 *
 * The returned URL is valid for one-time use only.
 *
 * @see <a href="https://upstox.com/developer/api-documentation/get-market-data-feed-authorize-v3">Market Data Feed Authorize API</a>
 */
@JvmSynthetic
internal fun executeAuthorizeMarketDataFeed(apiClient: ApiClient): AuthorizedRedirectResponse {
    val response: UpstoxResponse<AuthorizedRedirectResponse> = apiClient.get(
        endpoint = "/feed/market-data-feed/authorize",
        overrideBaseUrl = BASE_URL_V3
    )
    return response.dataOrThrow()
}
