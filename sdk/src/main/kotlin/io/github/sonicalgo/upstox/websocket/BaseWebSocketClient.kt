package io.github.sonicalgo.upstox.websocket

import io.github.sonicalgo.upstox.config.UpstoxConfig
import io.github.sonicalgo.upstox.config.UpstoxConstants
import io.github.sonicalgo.upstox.exception.MaxReconnectAttemptsExceededException
import okhttp3.*
import okio.ByteString
import java.io.Closeable
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Base class for WebSocket clients with automatic reconnection support.
 *
 * Provides common functionality for WebSocket management:
 * - Connection state tracking
 * - Automatic reconnection with exponential backoff
 * - Ping/pong keepalive (configured at OkHttpClient level)
 * - Thread-safe state management
 *
 * Subclasses must implement:
 * - [getWebSocketUrl] to obtain the authorized WebSocket URL
 * - [onWebSocketMessage] to handle incoming messages
 * - [onConnectionEstablished] to handle successful connection (first time or reconnect)
 *
 * @param httpClient OkHttpClient configured for WebSocket connections
 * @param clientName Name used for the reconnection thread (e.g., "MarketDataFeed")
 */
abstract class BaseWebSocketClient(
    private val httpClient: OkHttpClient,
    clientName: String
) : Closeable {

    private var webSocket: WebSocket? = null

    // Connection state tracking
    private val connectionState = AtomicInteger(ConnectionState.DISCONNECTED.ordinal)
    private val shouldReconnect = AtomicBoolean(true)
    private val reconnectAttempt = AtomicInteger(0)
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "$clientName-Reconnect").apply { isDaemon = true }
    }

    /**
     * Current connection state of the WebSocket.
     */
    enum class ConnectionState {
        /** Not connected to WebSocket */
        DISCONNECTED,
        /** Connection attempt in progress */
        CONNECTING,
        /** Connected and ready to send/receive */
        CONNECTED,
        /** Attempting to reconnect after disconnection */
        RECONNECTING
    }

    /**
     * Initiates the WebSocket connection.
     *
     * @param autoReconnect Whether to automatically reconnect on disconnection (default: true)
     */
    protected fun initiateConnection(autoReconnect: Boolean = true) {
        shouldReconnect.set(autoReconnect)
        reconnectAttempt.set(0)
        doConnect()
    }

    private fun doConnect() {
        if (!connectionState.compareAndSet(ConnectionState.DISCONNECTED.ordinal, ConnectionState.CONNECTING.ordinal) &&
            !connectionState.compareAndSet(ConnectionState.RECONNECTING.ordinal, ConnectionState.CONNECTING.ordinal)) {
            return
        }

        try {
            val wsUrl = getWebSocketUrl()

            val request = Request.Builder()
                .url(wsUrl)
                .build()

            webSocket = httpClient.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    connectionState.set(ConnectionState.CONNECTED.ordinal)
                    val wasReconnecting = reconnectAttempt.get() > 0
                    reconnectAttempt.set(0)
                    onConnectionEstablished(wasReconnecting)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    try {
                        onWebSocketMessage(text)
                    } catch (e: Exception) {
                        onWebSocketError(e)
                    }
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    try {
                        onWebSocketBinaryMessage(bytes)
                    } catch (e: Exception) {
                        onWebSocketError(e)
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    webSocket.close(code, reason)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    handleDisconnection(code, reason, null)
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    handleDisconnection(1006, t.message ?: "Connection failure", t)
                }
            })
        } catch (e: Exception) {
            connectionState.set(ConnectionState.DISCONNECTED.ordinal)
            onWebSocketError(e)
            scheduleReconnect()
        }
    }

    private fun handleDisconnection(code: Int, reason: String, error: Throwable?) {
        val previousState = connectionState.getAndSet(ConnectionState.DISCONNECTED.ordinal)
        this.webSocket = null

        // Only notify if we were actually connected or connecting
        if (previousState == ConnectionState.CONNECTED.ordinal || previousState == ConnectionState.CONNECTING.ordinal) {
            if (error != null) {
                onWebSocketError(error)
            }
            onWebSocketDisconnected(code, reason)
        }

        scheduleReconnect()
    }

    private fun scheduleReconnect() {
        if (!shouldReconnect.get()) {
            return
        }

        val maxAttempts = UpstoxConfig.maxWebSocketReconnectAttempts
        val currentAttempt = reconnectAttempt.incrementAndGet()

        // Check if max attempts reached
        if (currentAttempt > maxAttempts) {
            onWebSocketError(MaxReconnectAttemptsExceededException(
                "Maximum reconnection attempts ($maxAttempts) exceeded"
            ))
            return
        }

        // Calculate delay with exponential backoff
        val delay = calculateReconnectDelay(currentAttempt)

        connectionState.set(ConnectionState.RECONNECTING.ordinal)
        onWebSocketReconnecting(currentAttempt, delay)

        scheduler.schedule({
            if (shouldReconnect.get() && connectionState.get() == ConnectionState.RECONNECTING.ordinal) {
                doConnect()
            }
        }, delay, TimeUnit.MILLISECONDS)
    }

    private fun calculateReconnectDelay(attempt: Int): Long {
        val initialDelay = UpstoxConstants.WEBSOCKET_RECONNECT_INITIAL_DELAY_MS
        val maxDelay = UpstoxConstants.WEBSOCKET_RECONNECT_MAX_DELAY_MS

        // Exponential backoff: initialDelay * 2^(attempt-1), capped at maxDelay
        val exponentialDelay = initialDelay * (1L shl minOf(attempt - 1, 30))
        return minOf(exponentialDelay, maxDelay)
    }

    /**
     * Gets the authorized WebSocket URL for connection.
     * Called each time a connection attempt is made (including reconnections).
     */
    protected abstract fun getWebSocketUrl(): String

    /**
     * Handles incoming text messages from the WebSocket.
     */
    protected open fun onWebSocketMessage(text: String) {}

    /**
     * Handles incoming binary messages from the WebSocket.
     * Default implementation converts to UTF-8 and calls [onWebSocketMessage].
     */
    protected open fun onWebSocketBinaryMessage(bytes: ByteString) {
        onWebSocketMessage(bytes.utf8())
    }

    /**
     * Called when the WebSocket connection is established.
     *
     * @param isReconnect true if this is a reconnection, false if first connection
     */
    protected abstract fun onConnectionEstablished(isReconnect: Boolean)

    /**
     * Called when the WebSocket is disconnected.
     */
    protected abstract fun onWebSocketDisconnected(code: Int, reason: String)

    /**
     * Called when attempting to reconnect.
     */
    protected abstract fun onWebSocketReconnecting(attempt: Int, delayMs: Long)

    /**
     * Called when an error occurs.
     */
    protected abstract fun onWebSocketError(error: Throwable)

    /**
     * Sends a binary message through the WebSocket.
     *
     * @param bytes The binary data to send
     * @return true if the message was queued for sending, false if not connected
     */
    protected fun sendBinaryMessage(bytes: ByteString): Boolean {
        return webSocket?.send(bytes) ?: false
    }

    /**
     * Sends a text message through the WebSocket.
     *
     * @param text The text to send
     * @return true if the message was queued for sending, false if not connected
     */
    protected fun sendTextMessage(text: String): Boolean {
        return webSocket?.send(text) ?: false
    }

    /**
     * Disconnect from the WebSocket.
     *
     * This performs a graceful disconnect but does not disable auto-reconnection.
     * Use [close] to fully disconnect and prevent reconnection.
     */
    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        connectionState.set(ConnectionState.DISCONNECTED.ordinal)
    }

    /**
     * Current connection state.
     */
    val currentState: ConnectionState
        get() = ConnectionState.entries[connectionState.get()]

    /**
     * Check if connected to the WebSocket.
     *
     * @return true if the connection is in CONNECTED state
     */
    val isConnected: Boolean
        get() = connectionState.get() == ConnectionState.CONNECTED.ordinal

    /**
     * Check if the client is currently attempting to reconnect.
     *
     * @return true if in RECONNECTING state
     */
    val isReconnecting: Boolean
        get() = connectionState.get() == ConnectionState.RECONNECTING.ordinal

    /**
     * Enable or disable auto-reconnection.
     *
     * @param enabled Whether to enable auto-reconnection
     */
    fun setAutoReconnect(enabled: Boolean) {
        shouldReconnect.set(enabled)
        if (!enabled && connectionState.get() == ConnectionState.RECONNECTING.ordinal) {
            connectionState.set(ConnectionState.DISCONNECTED.ordinal)
        }
    }

    /**
     * Close the client and release all resources.
     *
     * This disables auto-reconnection, disconnects the WebSocket, and shuts down
     * the reconnection scheduler. After calling close(), the client should not be reused.
     */
    override fun close() {
        shouldReconnect.set(false)
        disconnect()
        scheduler.shutdownNow()
    }
}
