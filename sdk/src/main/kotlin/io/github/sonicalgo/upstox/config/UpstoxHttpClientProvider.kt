package io.github.sonicalgo.upstox.config

import io.github.sonicalgo.core.client.HeaderProvider
import io.github.sonicalgo.core.client.HttpClientProvider
import io.github.sonicalgo.upstox.exception.UpstoxApiException
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Upstox-specific HTTP client provider with endpoint-aware authentication.
 *
 * Extends [HttpClientProvider] to add Upstox-specific interceptor logic:
 * - Skips authentication for auth-exempt endpoints (login/token)
 * - Uses sandbox token for sandbox-enabled endpoints when sandbox mode is active
 *
 * @property config Upstox SDK configuration
 * @property headerProvider Provider for base headers (not used for auth in this implementation)
 * @property shutdownTimeoutSeconds Timeout for graceful shutdown
 */
internal class UpstoxHttpClientProvider(
    private val config: UpstoxConfig,
    headerProvider: HeaderProvider,
    shutdownTimeoutSeconds: Long = UpstoxConstants.SHUTDOWN_TIMEOUT_SECONDS
) : HttpClientProvider(config, headerProvider, shutdownTimeoutSeconds) {

    /**
     * Builds the HTTP client with Upstox-specific endpoint-aware authentication.
     *
     * Token selection logic:
     * - For auth-exempt endpoints: No authorization header added
     * - For sandbox-enabled endpoints (when sandboxEnabled is true): uses sandboxToken
     * - For all other requests: uses accessToken
     *
     * @see UpstoxConstants.AUTH_EXEMPT_ENDPOINTS
     * @see UpstoxConstants.SANDBOX_ENABLED_ENDPOINTS
     */
    override fun buildHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(config.connectTimeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(config.readTimeoutMs, TimeUnit.MILLISECONDS)
            .writeTimeout(config.writeTimeoutMs, TimeUnit.MILLISECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                val endpoint = request.url.encodedPath

                // Check if this endpoint is auth-exempt (login, token exchange, logout)
                val isAuthExempt = UpstoxConstants.AUTH_EXEMPT_ENDPOINTS.any {
                    endpoint.endsWith(it)
                }

                // Check if this endpoint supports sandbox mode
                val isSandboxCapable = UpstoxConstants.SANDBOX_ENABLED_ENDPOINTS.any {
                    endpoint.endsWith(it)
                }

                // Select token based on sandbox mode and endpoint capability
                val token = if (isSandboxCapable && config.sandboxEnabled) {
                    if (config.sandboxToken.isBlank()) {
                        throw UpstoxApiException(
                            message = "Sandbox mode enabled but sandboxToken is not set",
                            httpStatusCode = 0
                        )
                    }
                    config.sandboxToken
                } else {
                    config.accessToken
                }

                // Validate token for authenticated endpoints
                if (!isAuthExempt && token.isBlank()) {
                    throw UpstoxApiException(
                        message = "Access token not set. Call upstox.setAccessToken(token) before making API calls.",
                        httpStatusCode = 0
                    )
                }

                val requestBuilder = request.newBuilder()
                    .addHeader("Accept", "application/json")

                // Add authorization header only for authenticated endpoints with valid token
                if (!isAuthExempt && token.isNotBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }

                chain.proceed(requestBuilder.build())
            }

        // Add logging interceptor if enabled
        if (config.loggingEnabled) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }
}
