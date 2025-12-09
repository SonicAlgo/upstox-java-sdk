package io.github.sonicalgo.core.config

/**
 * HTTP client configuration interface.
 * Implementations provide infrastructure settings for HTTP clients.
 * Broker-specific credentials should be handled separately via HeaderProvider.
 */
interface HttpSdkConfig {
    /** Enable HTTP request/response logging */
    val loggingEnabled: Boolean

    /** Number of retries on rate limit (HTTP 429), 0-5 */
    val rateLimitRetries: Int

    /** Connection timeout in milliseconds */
    val connectTimeoutMs: Long

    /** Read timeout in milliseconds */
    val readTimeoutMs: Long

    /** Write timeout in milliseconds */
    val writeTimeoutMs: Long
}
