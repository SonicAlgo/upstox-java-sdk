package io.github.sonicalgo.upstox.exception

import io.github.sonicalgo.core.exception.SdkException

/**
 * Exception for Upstox HTTP API errors.
 *
 * Extends [SdkException] to provide common error categorization methods
 * like [isRateLimitError], [isAuthenticationError], [isValidationError], etc.
 *
 * The message contains the raw response body from the API for debugging.
 *
 * @property httpStatusCode HTTP status code returned by the API
 * @param message Raw response body from the API
 */
class UpstoxApiException(
    message: String?,
    httpStatusCode: Int? = null,
    cause: Throwable? = null
) : SdkException(message, httpStatusCode, cause)