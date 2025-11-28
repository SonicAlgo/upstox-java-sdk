package io.github.sonicalgo.upstox.api

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants
import io.github.sonicalgo.upstox.model.websocket.AuthorizedRedirectResponse

/**
 * API for WebSocket authorization endpoints.
 *
 * Provides methods to get authorized WebSocket URLs for market data feed
 * and portfolio stream connections.
 */
class WebSocketApi private constructor() {

    /**
     * Get authorized WebSocket URL for Market Data Feed V3.
     *
     * The returned URL is valid for one-time use only.
     *
     * @return AuthorizedRedirectResponse containing the WebSocket URL
     * @see <a href="https://upstox.com/developer/api-documentation/get-market-data-feed-authorize-v3">Market Data Feed Authorize API</a>
     */
    fun authorizeMarketDataFeed(): AuthorizedRedirectResponse {
        return ApiClient.get(
            endpoint = Endpoints.GET_MARKET_DATA_FEED_AUTH,
            baseUrl = UpstoxConstants.BASE_URL_V3
        )
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
    @JvmOverloads
    fun authorizePortfolioStream(
        updateTypes: Set<PortfolioUpdateType> = PortfolioUpdateType.entries.toSet()
    ): AuthorizedRedirectResponse {
        val types = updateTypes.joinToString(",") { it.value }
        return ApiClient.get(
            endpoint = Endpoints.GET_PORTFOLIO_STREAM_AUTH,
            queryParams = mapOf("update_types" to types),
            baseUrl = UpstoxConstants.BASE_URL_V2
        )
    }

    internal object Endpoints {
        const val GET_MARKET_DATA_FEED_AUTH = "/feed/market-data-feed/authorize"
        const val GET_PORTFOLIO_STREAM_AUTH = "/feed/portfolio-stream-feed/authorize"
    }

    companion object {
        internal val instance by lazy { WebSocketApi() }
    }
}

/**
 * Types of updates available in Portfolio Stream.
 */
enum class PortfolioUpdateType(val value: String) {
    ORDER("order"),
    GTT_ORDER("gtt_order"),
    POSITION("position"),
    HOLDING("holding")
}
