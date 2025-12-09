package io.github.sonicalgo.upstox.model.common

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Standard Upstox API response wrapper.
 *
 * All Upstox API responses follow this structure with a status indicator
 * and either data or errors.
 *
 * Example success response:
 * ```json
 * {
 *   "status": "success",
 *   "data": { ... }
 * }
 * ```
 *
 * Example error response:
 * ```json
 * {
 *   "status": "error",
 *   "errors": [{ "error_code": "...", "message": "..." }]
 * }
 * ```
 *
 * @param T The type of data contained in the response
 * @property status Status of the API call ("success" or "error")
 * @property data The response data (present when status is "success")
 * @property errors List of errors (present when status is "error")
 */
data class UpstoxResponse<T>(
    @JsonProperty("status")
    val status: String,

    @JsonProperty("data")
    val data: T? = null,

    @JsonProperty("errors")
    val errors: List<UpstoxError>? = null
) {
    /** Returns true if the API call was successful. */
    val isSuccess: Boolean get() = status == "success"

    /** Returns true if the API call resulted in an error. */
    val isError: Boolean get() = status == "error"

    /**
     * Returns data if response is successful, otherwise throws exception.
     *
     * @return The response data
     * @throws UpstoxApiException if response is not successful or data is null
     */
    fun dataOrThrow(): T {
        if (!isSuccess) {
            val errorMessage = errors?.firstOrNull()?.let { "${it.errorCode}: ${it.message}" }
                ?: "API request failed with status: $status"
            throw io.github.sonicalgo.upstox.exception.UpstoxApiException(errorMessage, null)
        }
        return data ?: throw io.github.sonicalgo.upstox.exception.UpstoxApiException(
            "Response data is null", null
        )
    }
}

/**
 * Upstox API error details.
 *
 * Contains information about what went wrong with the API call.
 *
 * Common error codes:
 * - UDAPI10000: Request not supported by Upstox API
 * - UDAPI100016: Invalid Credentials
 * - UDAPI10005: Too Many Requests
 * - UDAPI100050: Invalid token
 * - UDAPI100500: Internal server error
 *
 * @property errorCode Specific error code identifier
 * @property message Human-readable error description
 * @property propertyPath Request component that triggered the error
 * @property invalidValue The problematic value that was submitted
 * @property orderId Associated order ID for order-related errors
 * @property instrumentKey Associated instrument key for instrument-related errors
 * @see <a href="https://upstox.com/developer/api-documentation/error-codes">Upstox Error Codes</a>
 */
data class UpstoxError(
    @JsonProperty("error_code")
    val errorCode: String,

    @JsonProperty("message")
    val message: String,

    @JsonProperty("property_path")
    val propertyPath: String? = null,

    @JsonProperty("invalid_value")
    val invalidValue: String? = null,

    @JsonProperty("order_id")
    val orderId: String? = null,

    @JsonProperty("instrument_key")
    val instrumentKey: String? = null
)

/**
 * Response wrapper with metadata including latency.
 *
 * Used by high-frequency trading (HFT) endpoints that provide
 * processing time information.
 *
 * @param T The type of data contained in the response
 * @property status Status of the API call
 * @property data The response data
 * @property metadata Metadata containing additional information like latency
 * @property errors List of errors if the request failed
 */
data class UpstoxResponseWithMetadata<T>(
    @JsonProperty("status")
    val status: String,

    @JsonProperty("data")
    val data: T? = null,

    @JsonProperty("metadata")
    val metadata: ResponseMetadata? = null,

    @JsonProperty("errors")
    val errors: List<UpstoxError>? = null
) {
    val isSuccess: Boolean get() = status == "success"
}

/**
 * Response metadata containing processing information.
 *
 * @property latency Time taken by API platform to process the request (milliseconds)
 */
data class ResponseMetadata(
    @JsonProperty("latency")
    val latency: Int? = null
)

/**
 * Response for multi-order operations with summary statistics.
 *
 * Used for place multi-order, cancel multi-order, and exit all positions.
 *
 * @param T The type of successful response data
 * @property status Status: "success", "partial_success", or "error"
 * @property data Successful operation results
 * @property errors List of errors for failed operations
 * @property summary Summary statistics for the operation
 */
data class MultiOrderResponse<T>(
    @JsonProperty("status")
    val status: String,

    @JsonProperty("data")
    val data: T? = null,

    @JsonProperty("errors")
    val errors: List<UpstoxError>? = null,

    @JsonProperty("summary")
    val summary: MultiOrderSummary? = null
) {
    val isSuccess: Boolean get() = status == "success"
    val isPartialSuccess: Boolean get() = status == "partial_success"
}

/**
 * Summary statistics for multi-order operations.
 *
 * @property total Total number of orders processed
 * @property payloadError Number of orders with payload errors
 * @property success Number of successfully processed orders
 * @property error Number of failed orders
 */
data class MultiOrderSummary(
    @JsonProperty("total")
    val total: Int,

    @JsonProperty("payload_error")
    val payloadError: Int? = null,

    @JsonProperty("success")
    val success: Int,

    @JsonProperty("error")
    val error: Int
)

/**
 * Pagination metadata for paginated responses.
 *
 * @property pageNumber Current page number (1-indexed)
 * @property pageSize Number of results per page
 * @property totalRecords Total number of matching records
 * @property totalPages Total number of available pages
 */
data class PageInfo(
    @JsonProperty("page_number")
    val pageNumber: Int,

    @JsonProperty("page_size")
    val pageSize: Int,

    @JsonProperty("total_records")
    val totalRecords: Int? = null,

    @JsonProperty("total_pages")
    val totalPages: Int? = null
)

/**
 * Response wrapper with pagination metadata.
 *
 * @param T The type of data contained in the response
 * @property status Status of the API call
 * @property data The response data
 * @property errors List of errors if the request failed
 * @property metaData Metadata containing pagination information
 */
data class PaginatedResponse<T>(
    @JsonProperty("status")
    val status: String,

    @JsonProperty("data")
    val data: T? = null,

    @JsonProperty("errors")
    val errors: List<UpstoxError>? = null,

    @JsonProperty("meta_data")
    val metaData: PaginationMetadata? = null
) {
    val isSuccess: Boolean get() = status == "success"
}

/**
 * Pagination metadata wrapper.
 *
 * @property page Page information
 */
data class PaginationMetadata(
    @JsonProperty("page")
    val page: PageInfo
)
