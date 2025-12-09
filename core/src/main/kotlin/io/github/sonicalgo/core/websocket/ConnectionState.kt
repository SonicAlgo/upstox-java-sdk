package io.github.sonicalgo.core.websocket

/**
 * WebSocket connection state.
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
