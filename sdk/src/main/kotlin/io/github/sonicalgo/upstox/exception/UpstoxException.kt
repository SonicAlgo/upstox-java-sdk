package io.github.sonicalgo.upstox.exception

import io.github.sonicalgo.upstox.model.common.UpstoxError

/**
 * Base exception for all Upstox API errors.
 *
 * This exception is thrown when an API call fails. It contains
 * the HTTP status code and detailed error information from the
 * Upstox API.
 *
 * Example usage:
 * ```kotlin
 * val userApi = upstox.getUserApi()
 *
 * try {
 *     userApi.getProfile()
 * } catch (e: UpstoxApiException) {
 *     println("HTTP Status: ${e.httpStatusCode}")
 *     println("Error Code: ${e.errorCode}")
 *     println("Message: ${e.message}")
 *     e.errors?.forEach { error ->
 *         println("  - ${error.errorCode}: ${error.message}")
 *     }
 * }
 * ```
 *
 * @see <a href="https://upstox.com/developer/api-documentation/error-codes">Upstox Error Codes</a>
 */
class UpstoxApiException(
    /**
     * Human-readable error message.
     */
    override val message: String,

    /**
     * HTTP status code returned by the API.
     *
     * Common status codes:
     * - 400: Bad Request - incorrect parameters
     * - 401: Unauthorized - invalid or missing API key
     * - 403: Forbidden - resource access denied
     * - 404: Not Found - resource not found
     * - 405: Method Not Allowed - wrong HTTP method
     * - 406: Not Acceptable - wrong format requested
     * - 410: Gone - resource removed
     * - 429: Too Many Requests - rate limit exceeded
     * - 500: Internal Server Error
     * - 503: Service Unavailable - maintenance
     */
    val httpStatusCode: Int,

    /**
     * Primary error code from the API response.
     * May be null if the error couldn't be parsed.
     */
    val errorCode: String? = null,

    /**
     * List of all errors returned by the API.
     * Useful for multi-order operations that may have multiple failures.
     */
    val errors: List<UpstoxError>? = null,

    /**
     * Raw response body from the API.
     * Useful for debugging when error parsing fails.
     */
    val rawResponse: String? = null,

    /**
     * The underlying cause of this exception.
     */
    override val cause: Throwable? = null
) : RuntimeException(message, cause) {

    /**
     * Returns true if this is a rate limit error (HTTP 429).
     */
    val isRateLimitError: Boolean
        get() = httpStatusCode == 429 || errorCode == "UDAPI10005"

    /**
     * Returns true if this is an authentication error (HTTP 401).
     */
    val isAuthenticationError: Boolean
        get() = httpStatusCode == 401 || errorCode in listOf("UDAPI100050", "UDAPI100016")

    /**
     * Returns true if this is a validation error (HTTP 400).
     */
    val isValidationError: Boolean
        get() = httpStatusCode == 400 || errorCode in listOf("UDAPI100036", "UDAPI100038")

    /**
     * Returns true if this is a server error (HTTP 5xx).
     */
    val isServerError: Boolean
        get() = httpStatusCode in 500..599 || errorCode == "UDAPI100500"

    /**
     * Returns true if this is a not found error (HTTP 404).
     */
    val isNotFoundError: Boolean
        get() = httpStatusCode == 404

    /**
     * Returns true if the error is due to inactive client.
     */
    val isInactiveClientError: Boolean
        get() = errorCode == "UDAPI100073"

    /**
     * Returns true if the error is due to service being unavailable
     * (e.g., outside operating hours).
     */
    val isServiceUnavailableError: Boolean
        get() = httpStatusCode == 503 || errorCode == "UDAPI100072"

    override fun toString(): String {
        val sb = StringBuilder("UpstoxApiException(")
        sb.append("httpStatusCode=$httpStatusCode")
        if (errorCode != null) {
            sb.append(", errorCode=$errorCode")
        }
        sb.append(", message=$message")
        if (errors != null && errors.isNotEmpty()) {
            sb.append(", errors=[")
            sb.append(errors.joinToString(", ") { "${it.errorCode}: ${it.message}" })
            sb.append("]")
        }
        sb.append(")")
        return sb.toString()
    }

    companion object {
        /**
         * Creates an exception for rate limit errors.
         */
        fun rateLimitExceeded(): UpstoxApiException = UpstoxApiException(
            message = "Rate limit exceeded. Please slow down your requests.",
            httpStatusCode = 429,
            errorCode = "UDAPI10005"
        )

        /**
         * Creates an exception for authentication errors.
         */
        fun authenticationFailed(message: String = "Invalid or expired access token"): UpstoxApiException =
            UpstoxApiException(
                message = message,
                httpStatusCode = 401,
                errorCode = "UDAPI100050"
            )

        /**
         * Creates an exception for network errors.
         */
        fun networkError(cause: Throwable): UpstoxApiException = UpstoxApiException(
            message = "Network error: ${cause.message}",
            httpStatusCode = 0,
            cause = cause
        )

        /**
         * Creates an exception for parsing errors.
         */
        fun parseError(rawResponse: String?, cause: Throwable? = null): UpstoxApiException = UpstoxApiException(
            message = "Failed to parse API response",
            httpStatusCode = 0,
            rawResponse = rawResponse,
            cause = cause
        )
    }
}

/**
 * Exception thrown when attempting to access a feature that requires
 * Upstox Plus subscription.
 */
class UpstoxPlusRequiredException(
    message: String = "This API is available exclusively with an Upstox Plus plan subscription"
) : RuntimeException(message)

/**
 * Exception thrown when an operation is performed outside of allowed hours.
 */
class ServiceUnavailableException(
    message: String,
    val availableFrom: String? = null,
    val availableTo: String? = null
) : RuntimeException(message)
