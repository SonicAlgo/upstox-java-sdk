package io.github.sonicalgo.core.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.sonicalgo.core.config.HttpSdkConfig
import io.github.sonicalgo.core.exception.SdkException
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.Closeable
import java.net.URLEncoder

/**
 * Generic HTTP client for making API requests.
 *
 * Features:
 * - Support for GET, POST, PUT, DELETE methods
 * - JSON serialization/deserialization with Jackson
 * - Rate limit retry with exponential backoff
 * - Comprehensive error handling with [SdkException]
 *
 * @property baseUrl Base URL for API requests
 * @property config HTTP configuration
 * @property clientProvider HTTP client provider
 */
open class HttpClient(
    @PublishedApi internal val baseUrl: String,
    protected val config: HttpSdkConfig,
    protected val clientProvider: HttpClientProvider
) : Closeable {

    protected val httpClient get() = clientProvider.httpClient
    protected val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * Makes a GET request to the API.
     *
     * @param T Response type
     * @param endpoint API endpoint path
     * @param queryParams Optional query parameters
     * @param overrideBaseUrl Optional override for base URL
     * @return Parsed response of type T
     * @throws SdkException if the request fails
     */
    inline fun <reified T> get(
        endpoint: String,
        queryParams: Map<String, String?> = emptyMap(),
        overrideBaseUrl: String? = null
    ): T {
        val url = overrideBaseUrl ?: baseUrl
        val request = Request.Builder()
            .url(buildUrl(url, endpoint, queryParams))
            .get()
            .build()
        return execute(request, object : TypeReference<T>() {})
    }

    /**
     * Makes a POST request to the API.
     *
     * @param T Response type
     * @param endpoint API endpoint path
     * @param body Request body object (will be serialized to JSON)
     * @param formParams Form parameters for x-www-form-urlencoded requests
     * @param overrideBaseUrl Optional override for base URL
     * @return Parsed response of type T
     * @throws SdkException if the request fails
     */
    inline fun <reified T> post(
        endpoint: String,
        body: Any? = null,
        formParams: Map<String, String>? = null,
        overrideBaseUrl: String? = null
    ): T {
        val url = overrideBaseUrl ?: baseUrl
        val request = buildRequest(url, endpoint, "POST", body, formParams)
        return execute(request, object : TypeReference<T>() {})
    }

    /**
     * Makes a PUT request to the API.
     *
     * @param T Response type
     * @param endpoint API endpoint path
     * @param body Request body object
     * @param overrideBaseUrl Optional override for base URL
     * @return Parsed response of type T
     * @throws SdkException if the request fails
     */
    inline fun <reified T> put(
        endpoint: String,
        body: Any? = null,
        overrideBaseUrl: String? = null
    ): T {
        val url = overrideBaseUrl ?: baseUrl
        val request = buildRequest(url, endpoint, "PUT", body, null)
        return execute(request, object : TypeReference<T>() {})
    }

    /**
     * Makes a DELETE request to the API.
     *
     * @param T Response type
     * @param endpoint API endpoint path
     * @param body Optional request body
     * @param queryParams Optional query parameters
     * @param overrideBaseUrl Optional override for base URL
     * @return Parsed response of type T
     * @throws SdkException if the request fails
     */
    inline fun <reified T> delete(
        endpoint: String,
        body: Any? = null,
        queryParams: Map<String, String?> = emptyMap(),
        overrideBaseUrl: String? = null
    ): T {
        val url = overrideBaseUrl ?: baseUrl
        val request = if (body != null) {
            buildRequest(url, endpoint, "DELETE", body, null)
        } else {
            Request.Builder()
                .url(buildUrl(url, endpoint, queryParams))
                .delete()
                .build()
        }
        return execute(request, object : TypeReference<T>() {})
    }

    /**
     * Makes a raw GET request returning the response body as String.
     *
     * @param endpoint API endpoint path
     * @param queryParams Optional query parameters
     * @param overrideBaseUrl Optional override for base URL
     * @return Response body as String
     * @throws SdkException if the request fails
     */
    fun getRaw(
        endpoint: String,
        queryParams: Map<String, String?> = emptyMap(),
        overrideBaseUrl: String? = null
    ): String {
        val url = overrideBaseUrl ?: baseUrl
        val request = Request.Builder()
            .url(buildUrl(url, endpoint, queryParams))
            .get()
            .build()
        return executeRaw(request)
    }

    protected fun executeRaw(request: Request): String {
        try {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body.string()

                if (!response.isSuccessful) handleError(body, response.code)
                return body
            }
        } catch (e: SdkException) {
            throw e
        } catch (e: Exception) {
            throw createNetworkException(e)
        }
    }

    @PublishedApi
    internal fun buildRequest(
        baseUrl: String,
        endpoint: String,
        method: String,
        body: Any?,
        formParams: Map<String, String>?
    ): Request {
        val builder = Request.Builder().url("$baseUrl$endpoint")

        val requestBody: RequestBody = if (!formParams.isNullOrEmpty()) {
            builder.addHeader("Content-Type", "application/x-www-form-urlencoded")
            FormBody.Builder().apply {
                formParams.forEach { (key, value) -> add(key, value) }
            }.build()
        } else {
            builder.addHeader("Content-Type", "application/json")
            val jsonBody = body?.let { objectMapper.writeValueAsString(it) } ?: ""
            jsonBody.toRequestBody(JSON_MEDIA_TYPE)
        }

        return builder.method(method, requestBody).build()
    }

    @PublishedApi
    internal fun buildUrl(baseUrl: String, endpoint: String, queryParams: Map<String, String?>): String {
        val filtered = queryParams.filterValues { it != null }
        if (filtered.isEmpty()) return "$baseUrl$endpoint"

        val query = filtered.entries.joinToString("&") { (k, v) ->
            "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
        }
        return "$baseUrl$endpoint?$query"
    }

    @PublishedApi
    internal fun <T> execute(request: Request, typeRef: TypeReference<T>): T {
        try {
            val response = executeWithRateLimitRetry(request)
            response.use {
                val body = it.body.string()

                if (!it.isSuccessful) handleError(body, it.code)

                return objectMapper.readValue(body, typeRef)
            }
        } catch (e: SdkException) {
            throw e
        } catch (e: Exception) {
            throw createNetworkException(e)
        }
    }

    /**
     * Executes request with optional rate limit retry.
     * Retry behavior is controlled by [HttpSdkConfig.rateLimitRetries].
     */
    protected fun executeWithRateLimitRetry(request: Request): Response {
        val maxRetries = config.rateLimitRetries
        if (maxRetries <= 0) {
            return httpClient.newCall(request).execute()
        }

        var lastException: SdkException? = null
        repeat(maxRetries + 1) { attempt ->
            val response = httpClient.newCall(request).execute()
            if (response.code != 429) {
                return response
            }

            response.close()
            lastException = SdkException("Rate limit exceeded", 429)

            if (attempt < maxRetries) {
                val delayMs = (1L shl attempt) * 1000 // Exponential backoff: 1s, 2s, 4s, ...
                Thread.sleep(delayMs)
            }
        }
        throw lastException ?: SdkException("Rate limit exceeded", 429)
    }

    /**
     * Handles API error responses.
     * Override to provide custom error handling.
     */
    protected open fun handleError(responseBody: String, statusCode: Int): Nothing {
        throw SdkException(responseBody, statusCode)
    }

    /**
     * Creates a network exception.
     * Override to provide custom network exception handling.
     */
    protected open fun createNetworkException(e: Exception): SdkException {
        return SdkException("Network error: ${e.message}", null, e)
    }

    override fun close() {
        // OkHttpClient lifecycle is managed by HttpClientProvider
    }

    companion object {
        /**
         * Shared ObjectMapper instance.
         *
         * Configured with:
         * - Kotlin module for data class support
         * - JavaTimeModule for java.time.* handling
         * - Tolerant deserialization (ignores unknown properties)
         * - Unknown enum values deserialize as null
         */
        val objectMapper: ObjectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }
}
