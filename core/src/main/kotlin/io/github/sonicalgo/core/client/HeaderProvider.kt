package io.github.sonicalgo.core.client

/**
 * Provides HTTP headers for API requests.
 * Implementations should return broker-specific headers including authentication.
 * Called on each request to support dynamic values (e.g., rotating tokens).
 */
interface HeaderProvider {
    /**
     * Returns headers to be added to every HTTP request.
     * @return Map of header name to header value
     */
    fun getHeaders(): Map<String, String>
}
