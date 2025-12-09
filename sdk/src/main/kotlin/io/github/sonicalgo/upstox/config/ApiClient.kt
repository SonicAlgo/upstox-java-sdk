package io.github.sonicalgo.upstox.config

import io.github.sonicalgo.core.client.HttpClient
import io.github.sonicalgo.core.client.HttpClientProvider
import io.github.sonicalgo.core.exception.SdkException
import io.github.sonicalgo.upstox.exception.UpstoxApiException

/**
 * HTTP client for making API requests to Upstox.
 *
 * Extends [HttpClient] with Upstox-specific error handling.
 *
 * @property config The configuration for this SDK instance
 * @property clientProvider The HTTP client provider for this SDK instance
 */
internal class ApiClient(
    config: UpstoxConfig,
    clientProvider: HttpClientProvider
) : HttpClient(
    baseUrl = UpstoxConstants.BASE_URL_V3,
    config = config,
    clientProvider = clientProvider
) {

    /**
     * Handles API error responses by throwing [UpstoxApiException].
     */
    override fun handleError(responseBody: String, statusCode: Int): Nothing {
        throw UpstoxApiException(responseBody, statusCode)
    }

    /**
     * Creates a network exception as [UpstoxApiException].
     */
    override fun createNetworkException(e: Exception): SdkException {
        return UpstoxApiException("Network error: ${e.message}", null, e)
    }
}