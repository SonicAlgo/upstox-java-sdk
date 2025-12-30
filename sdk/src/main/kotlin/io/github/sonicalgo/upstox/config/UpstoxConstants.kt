package io.github.sonicalgo.upstox.config

/**
 * Constants used by the Upstox SDK.
 */
internal object UpstoxConstants {
    /** Base URL for Upstox API v2 endpoints. */
    const val BASE_URL_V2 = "https://api.upstox.com/v2"

    /** Base URL for Upstox API v3 endpoints. */
    const val BASE_URL_V3 = "https://api.upstox.com/v3"

    /** Base URL for High Frequency Trading (HFT) endpoints. */
    const val BASE_URL_HFT = "https://api-hft.upstox.com/v3"

    /** Base URL for sandbox mode endpoints. */
    const val BASE_URL_SANDBOX = "https://api-sandbox.upstox.com/v3"

    /** Base URL for authorization endpoint. */
    const val BASE_URL_AUTH = "https://api.upstox.com"

    /** Connection timeout in milliseconds (10 seconds). */
    const val CONNECT_TIMEOUT_MS = 10_000L

    /** Read timeout in milliseconds (30 seconds). */
    const val READ_TIMEOUT_MS = 30_000L

    /** Write timeout in milliseconds (30 seconds). */
    const val WRITE_TIMEOUT_MS = 30_000L

    /** WebSocket ping interval in milliseconds (10 seconds). */
    const val WEBSOCKET_PING_INTERVAL_MS = 10_000L

    /** Initial delay for WebSocket reconnection in milliseconds (1 second). */
    const val WEBSOCKET_RECONNECT_INITIAL_DELAY_MS = 1_000L

    /** Maximum delay for WebSocket reconnection in milliseconds (30 seconds). */
    const val WEBSOCKET_RECONNECT_MAX_DELAY_MS = 30_000L

    /** Default maximum number of WebSocket reconnection attempts. */
    const val WEBSOCKET_DEFAULT_MAX_RECONNECT_ATTEMPTS = 5

    /** Length of GUID used in WebSocket subscription messages. */
    const val SUBSCRIPTION_GUID_LENGTH = 20

    /** Timeout in seconds for HTTP client shutdown. */
    const val SHUTDOWN_TIMEOUT_SECONDS = 5L

    // Order API endpoints
    private const val ENDPOINT_PLACE_ORDER = "/order/place"
    private const val ENDPOINT_MODIFY_ORDER = "/order/modify"
    private const val ENDPOINT_CANCEL_ORDER = "/order/cancel"
    private const val ENDPOINT_PLACE_MULTI_ORDER = "/order/multi/place"

    // Login API endpoints
    private const val ENDPOINT_AUTHORIZATION_DIALOG = "/login/authorization/dialog"
    private const val ENDPOINT_GET_TOKEN = "/login/authorization/token"
    private const val ENDPOINT_LOGOUT = "/logout"

    /**
     * Endpoints that support sandbox mode.
     * When sandboxEnabled=true, these endpoints use sandboxToken instead of accessToken.
     */
    val SANDBOX_ENABLED_ENDPOINTS = setOf(
        ENDPOINT_PLACE_ORDER,
        ENDPOINT_MODIFY_ORDER,
        ENDPOINT_CANCEL_ORDER,
        ENDPOINT_PLACE_MULTI_ORDER
    )

    /**
     * Endpoints that don't require authentication.
     * These are the only endpoints that can be called without an access token.
     */
    val AUTH_EXEMPT_ENDPOINTS = setOf(
        ENDPOINT_AUTHORIZATION_DIALOG,
        ENDPOINT_GET_TOKEN,
        ENDPOINT_LOGOUT
    )
}
