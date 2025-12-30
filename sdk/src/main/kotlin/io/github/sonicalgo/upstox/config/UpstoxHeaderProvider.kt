package io.github.sonicalgo.upstox.config

import io.github.sonicalgo.core.client.HeaderProvider

/**
 * Header provider for Upstox API requests.
 *
 * This is a minimal implementation since [UpstoxHttpClientProvider] handles
 * endpoint-aware authentication with support for:
 * - Auth-exempt endpoints (login, token exchange)
 * - Sandbox mode for order endpoints
 *
 * @see UpstoxHttpClientProvider
 */
internal class UpstoxHeaderProvider : HeaderProvider {

    /**
     * Returns empty headers.
     *
     * Authentication headers are handled by [UpstoxHttpClientProvider]'s
     * endpoint-aware interceptor which supports auth-exempt endpoints
     * and sandbox mode.
     *
     * @return Empty map (headers added by interceptor)
     */
    override fun getHeaders(): Map<String, String> = emptyMap()
}
