package io.github.sonicalgo.core.client

import io.github.sonicalgo.core.config.HttpSdkConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Provides OkHttpClient instances for SDK API calls.
 *
 * Creates and manages HTTP clients with configurable timeouts,
 * automatic header injection via [HeaderProvider], and optional logging.
 *
 * @property config HTTP configuration (timeouts, logging, retries)
 * @property headerProvider Provider for request headers (including auth)
 * @property shutdownTimeoutSeconds Timeout for graceful shutdown
 */
open class HttpClientProvider(
    private val config: HttpSdkConfig,
    private val headerProvider: HeaderProvider,
    private val shutdownTimeoutSeconds: Long = 5L
) {

    @Volatile
    private var _httpClient: OkHttpClient? = null

    /**
     * OkHttpClient configured for REST API calls.
     *
     * Features:
     * - Configurable timeouts from [HttpSdkConfig]
     * - Automatic header injection via [HeaderProvider]
     * - Optional HTTP logging when [HttpSdkConfig.loggingEnabled] is true
     */
    val httpClient: OkHttpClient
        get() {
            val current = _httpClient
            if (current != null) return current

            synchronized(this) {
                return _httpClient ?: buildHttpClient().also {
                    _httpClient = it
                }
            }
        }

    /**
     * Builds the HTTP client.
     * Can be overridden by subclasses to customize client creation.
     */
    protected open fun buildHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(config.connectTimeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(config.readTimeoutMs, TimeUnit.MILLISECONDS)
            .writeTimeout(config.writeTimeoutMs, TimeUnit.MILLISECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                val requestBuilder = request.newBuilder()

                // Add headers from HeaderProvider
                headerProvider.getHeaders().forEach { (name, value) ->
                    requestBuilder.addHeader(name, value)
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

    @Volatile
    private var _wsHttpClient: OkHttpClient? = null

    /**
     * OkHttpClient configured for WebSocket connections.
     *
     * Configuration for long-lived connections:
     * - Read timeout disabled (0) to prevent premature closure
     * - Configurable ping interval for keepalive
     *
     * @param pingIntervalMs Ping interval in milliseconds for keepalive
     */
    fun getWsHttpClient(pingIntervalMs: Long): OkHttpClient {
        val current = _wsHttpClient
        if (current != null) return current

        synchronized(this) {
            return _wsHttpClient ?: OkHttpClient.Builder()
                .connectTimeout(config.connectTimeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .pingInterval(pingIntervalMs, TimeUnit.MILLISECONDS)
                .build()
                .also { _wsHttpClient = it }
        }
    }

    /**
     * Shuts down the HTTP clients and releases all resources.
     *
     * Waits up to [shutdownTimeoutSeconds] for in-flight requests to complete.
     */
    fun shutdown() {
        synchronized(this) {
            _httpClient?.shutdownGracefully()
            _httpClient = null

            _wsHttpClient?.shutdownGracefully()
            _wsHttpClient = null
        }
    }

    private fun OkHttpClient.shutdownGracefully() {
        dispatcher.executorService.shutdown()
        try {
            dispatcher.executorService.awaitTermination(
                shutdownTimeoutSeconds,
                TimeUnit.SECONDS
            )
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        connectionPool.evictAll()
    }
}
