package io.github.sonicalgo.core.websocket

import io.github.sonicalgo.core.config.WebSocketSdkConfig
import io.github.sonicalgo.core.exception.MaxReconnectAttemptsExceededException
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
 * - [getWebSocketUrl] to obtain the WebSocket URL
 * - [onWebSocketMessage] to handle incoming text messages
 * - [onWebSocketBinaryMessage] to handle incoming binary messages
 * - [onConnectionEstablished] to handle successful connection
 *
 * @param httpClient OkHttpClient configured for WebSocket connections
 * @param config WebSocket configuration (reconnect settings)
 * @param clientName Name used for the reconnection thread
 * @param initialReconnectDelayMs Initial delay for reconnection (default: 1000ms)
 * @param maxReconnectDelayMs Maximum delay for reconnection (default: 30000ms)
 */
abstract class BaseWebSocketClient(
    private val httpClient: OkHttpClient,
    protected val config: WebSocketSdkConfig,
    clientName: String,
    private val initialReconnectDelayMs: Long = 1_000L,
    private val maxReconnectDelayMs: Long = 30_000L
) : Closeable {

    private var webSocket: WebSocket? = null

    // Connection state tracking
    private val connectionState = AtomicInteger(ConnectionState.DISCONNECTED.ordinal)
    private val shouldReconnect = AtomicBoolean(true)
    private val reconnectAttempt = AtomicInteger(0)
    private val connectionStable = AtomicBoolean(false)
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "$clientName-Reconnect").apply { isDaemon = true }
    }

    companion object {
        // Time to wait before considering a connection stable (5 seconds)
        private const val STABILITY_TIMEOUT_MS = 5_000L
    }

    /**
     * Initiates the WebSocket connection.
     *
     * @param autoReconnect Whether to automatically reconnect on disconnection
     */
    protected fun initiateConnection(autoReconnect: Boolean = true) {
        shouldReconnect.set(autoReconnect && config.autoReconnectEnabled)
        reconnectAttempt.set(0)
        connectionStable.set(false)
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
                    connectionStable.set(false)
                    val wasReconnecting = reconnectAttempt.get() > 0
                    // Don't reset reconnectAttempt here - wait for connection to be stable
                    onConnectionEstablished(wasReconnecting)

                    // Schedule stability check - if still connected after timeout, consider stable
                    scheduler.schedule({
                        if (connectionState.get() == ConnectionState.CONNECTED.ordinal) {
                            markConnectionStable()
                        }
                    }, STABILITY_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    markConnectionStable() // First message = stable connection
                    try {
                        onWebSocketMessage(text)
                    } catch (e: Exception) {
                        onWebSocketError(e)
                    }
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    markConnectionStable() // First message = stable connection
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
                    // Extract error info from response if available
                    val errorMessage = buildString {
                        append(t.message ?: "Connection failure")
                        response?.let { resp ->
                            append(" [HTTP ${resp.code}]")
                            try {
                                resp.body.string().takeIf { it.isNotBlank() }?.let { body ->
                                    append(": $body")
                                }
                            } catch (_: Exception) {
                                // Ignore if body can't be read
                            }
                        }
                    }
                    // Wrap with enhanced message so user sees full error info
                    val error = RuntimeException(errorMessage, t)
                    handleDisconnection(response?.code ?: 1006, errorMessage, error)
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
        connectionStable.set(false) // Reset stability on disconnect
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

    /**
     * Marks the connection as stable and resets the reconnect counter.
     * Called when first message is received or after stability timeout.
     */
    private fun markConnectionStable() {
        if (connectionStable.compareAndSet(false, true)) {
            reconnectAttempt.set(0)
        }
    }

    private fun scheduleReconnect() {
        if (!shouldReconnect.get()) {
            return
        }

        val maxAttempts = config.maxReconnectAttempts
        val currentAttempt = reconnectAttempt.incrementAndGet()

        // Check if max attempts reached
        if (currentAttempt > maxAttempts) {
            onWebSocketError(MaxReconnectAttemptsExceededException(maxAttempts))
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
        // Exponential backoff: initialDelay * 2^(attempt-1), capped at maxDelay
        val exponentialDelay = initialReconnectDelayMs * (1L shl minOf(attempt - 1, 30))
        return minOf(exponentialDelay, maxReconnectDelayMs)
    }

    /**
     * Gets the WebSocket URL for connection.
     * Called each time a connection attempt is made (including reconnections).
     */
    protected abstract fun getWebSocketUrl(): String

    /**
     * Handles incoming text messages from the WebSocket.
     */
    protected abstract fun onWebSocketMessage(text: String)

    /**
     * Handles incoming binary messages from the WebSocket.
     */
    protected abstract fun onWebSocketBinaryMessage(bytes: ByteString)

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
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        connectionState.set(ConnectionState.DISCONNECTED.ordinal)
        scheduler.shutdownNow()
        try {
            scheduler.awaitTermination(1, TimeUnit.SECONDS)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
