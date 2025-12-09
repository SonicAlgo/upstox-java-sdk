package io.github.sonicalgo.core.exception

/**
 * Base exception for SDK errors.
 * Provides common error categorization methods.
 */
open class SdkException(
    message: String?,
    val httpStatusCode: Int? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause) {

    /** Returns true if this is a rate limit error (HTTP 429) */
    open fun isRateLimitError(): Boolean = httpStatusCode == 429

    /** Returns true if this is an authentication error (HTTP 401 or 403) */
    open fun isAuthenticationError(): Boolean = httpStatusCode in listOf(401, 403)

    /** Returns true if this is a validation error (HTTP 400) */
    open fun isValidationError(): Boolean = httpStatusCode == 400

    /** Returns true if this is a server error (HTTP 5xx) */
    open fun isServerError(): Boolean = httpStatusCode != null && httpStatusCode in 500..599

    /** Returns true if this is a network error (no HTTP status) */
    fun isNetworkError(): Boolean = httpStatusCode == null && cause != null
}

/**
 * Exception thrown when maximum WebSocket reconnection attempts are exceeded.
 */
class MaxReconnectAttemptsExceededException(
    val attemptsMade: Int,
    message: String = "Max reconnect attempts ($attemptsMade) exceeded"
) : SdkException(message)
