package io.github.sonicalgo.upstox.config

import io.github.sonicalgo.core.config.HttpSdkConfig

/**
 * Configuration for an Upstox SDK instance.
 *
 * Created via [io.github.sonicalgo.upstox.Upstox.Builder] and holds all configuration
 * for a single SDK instance.
 *
 * @property accessToken OAuth access token (mutable for token refresh)
 * @property sandboxToken Sandbox token for testing order operations
 * @property sandboxEnabled Whether sandbox mode is enabled
 * @property loggingEnabled Enable HTTP request/response logging
 * @property rateLimitRetries Number of retries on rate limit (0-5)
 * @property connectTimeoutMs Connection timeout in milliseconds
 * @property readTimeoutMs Read timeout in milliseconds
 * @property writeTimeoutMs Write timeout in milliseconds
 */
class UpstoxConfig internal constructor(
    @Volatile var accessToken: String = "",
    @Volatile var sandboxToken: String = "",
    @Volatile var sandboxEnabled: Boolean = false,
    override val loggingEnabled: Boolean = false,
    override val rateLimitRetries: Int = 0,
    override val connectTimeoutMs: Long = UpstoxConstants.CONNECT_TIMEOUT_MS,
    override val readTimeoutMs: Long = UpstoxConstants.READ_TIMEOUT_MS,
    override val writeTimeoutMs: Long = UpstoxConstants.WRITE_TIMEOUT_MS
) : HttpSdkConfig {
    init {
        require(rateLimitRetries in 0..5) { "rateLimitRetries must be between 0 and 5" }
    }
}
