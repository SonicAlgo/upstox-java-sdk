package io.github.sonicalgo.upstox.config

import io.github.sonicalgo.upstox.config.OkHttpClientFactory.httpClient
import io.github.sonicalgo.upstox.exception.UpstoxApiException
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Singleton factory providing OkHttpClient instances for the Upstox SDK.
 *
 * Provides separate client configurations for REST API calls and WebSocket connections.
 * The REST client includes standard timeouts and automatic token injection based on
 * sandbox mode settings. The WebSocket client is optimized for long-lived connections.
 *
 * Token selection logic:
 * - For sandbox-enabled endpoints (when [UpstoxConfig.sandboxEnabled] is true): uses [UpstoxConfig.sandboxToken]
 * - For all other requests: uses [UpstoxConfig.accessToken]
 *
 * @see UpstoxConstants.SANDBOX_ENABLED_ENDPOINTS for the list of sandbox-capable endpoints
 */
internal object OkHttpClientFactory {

    @Volatile
    private var _httpClient: OkHttpClient? = null

    @Volatile
    private var lastLoggingState: Boolean = false

    /**
     * OkHttpClient configured for REST API calls.
     *
     * Features:
     * - Connection timeout: [UpstoxConstants.CONNECT_TIMEOUT_MS]
     * - Read timeout: [UpstoxConstants.READ_TIMEOUT_MS]
     * - Write timeout: [UpstoxConstants.WRITE_TIMEOUT_MS]
     * - Automatic Authorization header injection with sandbox support
     * - Accept: application/json header on all requests
     * - Optional HTTP logging when [UpstoxConfig.loggingEnabled] is true
     */
    val httpClient: OkHttpClient
        get() {
            val current = _httpClient
            if (current != null) return current

            synchronized(this) {
                return _httpClient ?: buildHttpClient().also {
                    _httpClient = it
                    lastLoggingState = UpstoxConfig.loggingEnabled
                }
            }
        }

    private fun buildHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(UpstoxConstants.CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .readTimeout(UpstoxConstants.READ_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .writeTimeout(UpstoxConstants.WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                val endpoint = request.url.encodedPath

                // Check if this endpoint supports sandbox mode (O(n) but n=5, acceptable)
                val isSandboxCapable = UpstoxConstants.SANDBOX_ENABLED_ENDPOINTS.any {
                    endpoint.endsWith(it)
                }

                // Select token based on sandbox mode and endpoint capability
                val token = if (isSandboxCapable && UpstoxConfig.sandboxEnabled) {
                    if (UpstoxConfig.sandboxToken.isBlank()) {
                        throw UpstoxApiException(
                            message = "Sandbox mode enabled but sandboxToken is not set",
                            httpStatusCode = 0
                        )
                    }
                    UpstoxConfig.sandboxToken
                } else {
                    UpstoxConfig.accessToken
                }

                // Check if endpoint requires authentication
                val requiresAuth = !UpstoxConstants.AUTH_EXEMPT_ENDPOINTS.any {
                    endpoint.endsWith(it)
                }

                if (token.isBlank() && requiresAuth) {
                    throw UpstoxApiException(
                        message = "Access token not set. Call Upstox.getInstance().setAccessToken(token) before making API calls.",
                        httpStatusCode = 0
                    )
                }

                val requestBuilder = request.newBuilder()
                    .addHeader("Accept", "application/json")

                if (token.isNotBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }

                chain.proceed(requestBuilder.build())
            }

        // Add logging interceptor if enabled
        if (UpstoxConfig.loggingEnabled) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    /**
     * Rebuilds the HTTP client if logging configuration has changed.
     * Called automatically when [UpstoxConfig.loggingEnabled] changes.
     */
    fun rebuildClientIfNeeded() {
        synchronized(this) {
            if (_httpClient != null && lastLoggingState != UpstoxConfig.loggingEnabled) {
                _httpClient?.dispatcher?.executorService?.shutdown()
                _httpClient?.connectionPool?.evictAll()
                _httpClient = buildHttpClient()
                lastLoggingState = UpstoxConfig.loggingEnabled
            }
        }
    }

    /**
     * OkHttpClient configured for WebSocket connections.
     *
     * Derived from [httpClient] with modifications for long-lived WebSocket connections:
     * - Read timeout disabled (0) to prevent premature connection closure
     * - Ping interval: [UpstoxConstants.WEBSOCKET_PING_INTERVAL_MS] for keepalive
     *
     * The ping interval ensures the connection stays alive by sending WebSocket ping
     * frames at regular intervals. The server responds with pong frames, allowing
     * detection of dead connections.
     */
    val wsHttpClient: OkHttpClient by lazy {
        httpClient.newBuilder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .pingInterval(UpstoxConstants.WEBSOCKET_PING_INTERVAL_MS, TimeUnit.MILLISECONDS)
            .build()
    }

    /**
     * Shuts down the HTTP clients and releases all resources.
     *
     * This should be called when the clients are no longer needed to properly
     * clean up thread pools and connections. Waits up to [UpstoxConstants.SHUTDOWN_TIMEOUT_SECONDS]
     * seconds for in-flight requests to complete.
     */
    fun shutdown() {
        synchronized(this) {
            _httpClient?.let { client ->
                client.dispatcher.executorService.shutdown()
                try {
                    client.dispatcher.executorService.awaitTermination(
                        UpstoxConstants.SHUTDOWN_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS
                    )
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
                client.connectionPool.evictAll()
            }
            _httpClient = null
        }
    }
}
