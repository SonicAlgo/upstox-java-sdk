package io.github.sonicalgo.upstox.api

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V3
import io.github.sonicalgo.upstox.model.common.UpstoxResponse
import io.github.sonicalgo.upstox.model.enums.PortfolioUpdateType
import io.github.sonicalgo.upstox.model.websocket.AuthorizedRedirectResponse

/**
 * API for WebSocket authorization endpoints.
 *
 * Provides methods to get authorized WebSocket URLs for market data feed
 * and portfolio stream connections.
 */
class WebSocketApi internal constructor(private val apiClient: ApiClient) {

    /**
     * Get authorized WebSocket URL for Market Data Feed V3.
     *
     * The returned URL is valid for one-time use only.
     *
     * @return AuthorizedRedirectResponse containing the WebSocket URL
     * @see <a href="https://upstox.com/developer/api-documentation/get-market-data-feed-authorize-v3">Market Data Feed Authorize API</a>
     */
    fun authorizeMarketDataFeed(): AuthorizedRedirectResponse {
        val response: UpstoxResponse<AuthorizedRedirectResponse> = apiClient.get(
            endpoint = Endpoints.GET_MARKET_DATA_FEED_AUTH,
            overrideBaseUrl = BASE_URL_V3
        )
        return response.dataOrThrow()
    }

    /**
     * Get authorized WebSocket URL for Portfolio Stream Feed.
     *
     * The returned URL is valid for one-time use only.
     *
     * @param updateTypes Types of updates to subscribe to. Defaults to all types.
     * @return AuthorizedRedirectResponse containing the WebSocket URL
     * @see <a href="https://upstox.com/developer/api-documentation/get-portfolio-stream-feed-authorize">Portfolio Stream Feed Authorize API</a>
     */
    fun authorizePortfolioStream(
        updateTypes: Set<PortfolioUpdateType> = PortfolioUpdateType.entries.toSet()
    ): AuthorizedRedirectResponse {
        val types = updateTypes.joinToString(",") { it.value }
        val response: UpstoxResponse<AuthorizedRedirectResponse> = apiClient.get(
            endpoint = Endpoints.GET_PORTFOLIO_STREAM_AUTH,
            queryParams = mapOf("update_types" to types),
            overrideBaseUrl = BASE_URL_V2
        )
        return response.dataOrThrow()
    }

    internal object Endpoints {
        const val GET_MARKET_DATA_FEED_AUTH = "/feed/market-data-feed/authorize"
        const val GET_PORTFOLIO_STREAM_AUTH = "/feed/portfolio-stream-feed/authorize"
    }
}
