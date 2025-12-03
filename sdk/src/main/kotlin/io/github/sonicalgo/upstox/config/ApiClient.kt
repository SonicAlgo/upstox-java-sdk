package io.github.sonicalgo.upstox.config

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import io.github.sonicalgo.upstox.exception.UpstoxApiException
import io.github.sonicalgo.upstox.model.common.UpstoxError
import io.github.sonicalgo.upstox.model.common.UpstoxResponse
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.Closeable
import java.lang.reflect.Type
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

/**
 * Internal HTTP client for making API requests to Upstox.
 *
 * This singleton object handles all HTTP communication with the Upstox API,
 * including request building, response parsing, and error handling.
 *
 * Features:
 * - Single shared [Gson] instance for JSON serialization/deserialization
 * - V3 API as default base URL
 * - Support for GET, POST, PUT, DELETE methods
 * - Automatic response unwrapping from Upstox response envelope
 * - Comprehensive error handling with [UpstoxApiException]
 */
internal object ApiClient : Closeable {

    private val httpClient get() = OkHttpClientFactory.httpClient
    private const val DEFAULT_BASE_URL = UpstoxConstants.BASE_URL_V3
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * Single shared Gson instance for the entire SDK.
     *
     * Configured with:
     * - Flexible date parsing for multiple API date formats
     * - Graceful handling of unknown enum values (returns null instead of throwing)
     */
    val gson: Gson = GsonBuilder()
        .registerTypeAdapterFactory(SafeEnumTypeAdapterFactory())
        .registerTypeAdapter(Date::class.java, FlexibleDateDeserializer())
        .create()

    inline fun <reified T> get(
        endpoint: String,
        queryParams: Map<String, String?> = emptyMap(),
        baseUrl: String = DEFAULT_BASE_URL,
        unwrap: Boolean = true
    ): T {
        val request = Request.Builder()
            .url(buildUrl(baseUrl, endpoint, queryParams))
            .get()
            .build()
        return execute(request, object : TypeToken<T>() {}.type, unwrap)
    }

    inline fun <reified T> get(
        endpoint: String,
        params: Any,
        baseUrl: String = DEFAULT_BASE_URL,
        unwrap: Boolean = true
    ): T {
        return get(endpoint, toQueryParams(params), baseUrl, unwrap)
    }

    fun toQueryParams(obj: Any): Map<String, String?> {
        val jsonObject = gson.toJsonTree(obj).asJsonObject
        return jsonObject.entrySet().associate { (key, value) ->
            key to when {
                value.isJsonNull -> null
                value.isJsonPrimitive -> value.asString
                else -> value.toString()
            }
        }
    }

    inline fun <reified T> post(
        endpoint: String,
        body: Any? = null,
        formParams: Map<String, String>? = null,
        baseUrl: String = DEFAULT_BASE_URL,
        unwrap: Boolean = true
    ): T {
        val request = buildRequest(baseUrl, endpoint, "POST", body, formParams)
        return execute(request, object : TypeToken<T>() {}.type, unwrap)
    }

    inline fun <reified T> put(
        endpoint: String,
        body: Any? = null,
        baseUrl: String = DEFAULT_BASE_URL,
        unwrap: Boolean = true
    ): T {
        val request = buildRequest(baseUrl, endpoint, "PUT", body, null)
        return execute(request, object : TypeToken<T>() {}.type, unwrap)
    }

    inline fun <reified T> delete(
        endpoint: String,
        body: Any? = null,
        queryParams: Map<String, String?> = emptyMap(),
        baseUrl: String = DEFAULT_BASE_URL,
        unwrap: Boolean = true
    ): T {
        val request = if (body != null) {
            buildRequest(baseUrl, endpoint, "DELETE", body, null)
        } else {
            Request.Builder()
                .url(buildUrl(baseUrl, endpoint, queryParams))
                .delete()
                .build()
        }
        return execute(request, object : TypeToken<T>() {}.type, unwrap)
    }

    fun getRaw(
        endpoint: String,
        queryParams: Map<String, String?> = emptyMap(),
        baseUrl: String = DEFAULT_BASE_URL
    ): String {
        val request = Request.Builder()
            .url(buildUrl(baseUrl, endpoint, queryParams))
            .get()
            .build()
        return executeRaw(request)
    }

    private fun executeRaw(request: Request): String {
        try {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body.string()

                if (!response.isSuccessful) handleError(body, response.code)
                return body
            }
        } catch (e: UpstoxApiException) {
            throw e
        } catch (e: Exception) {
            throw UpstoxApiException.networkError(e)
        }
    }

    fun buildRequest(
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
            val jsonBody = body?.let { gson.toJson(it) } ?: ""
            jsonBody.toRequestBody(JSON_MEDIA_TYPE)
        }

        return builder.method(method, requestBody).build()
    }

    fun buildUrl(baseUrl: String, endpoint: String, queryParams: Map<String, String?>): String {
        val filtered = queryParams.filterValues { it != null }
        if (filtered.isEmpty()) return "$baseUrl$endpoint"

        val query = filtered.entries.joinToString("&") { (k, v) ->
            "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
        }
        return "$baseUrl$endpoint?$query"
    }

    fun <T> execute(request: Request, responseType: Type, unwrap: Boolean, allowNullData: Boolean = false): T {
        try {
            val response = executeWithRateLimitRetry(request)
            response.use {
                val body = it.body.string()

                if (!it.isSuccessful) handleError(body, it.code, it.message)

                return if (unwrap) {
                    val wrappedType = TypeToken.getParameterized(UpstoxResponse::class.java, responseType).type
                    val parsed = gson.fromJson<UpstoxResponse<T>>(body, wrappedType)
                    if (parsed.data == null && !allowNullData) {
                        throw UpstoxApiException("Response data is null", it.code, rawResponse = body)
                    }
                    @Suppress("UNCHECKED_CAST")
                    parsed.data as T
                } else {
                    gson.fromJson(body, responseType)
                }
            }
        } catch (e: UpstoxApiException) {
            throw e
        } catch (e: Exception) {
            throw UpstoxApiException.networkError(e)
        }
    }

    /**
     * Executes request with optional rate limit retry.
     * Retry behavior is controlled by [UpstoxConfig.rateLimitRetries].
     */
    private fun executeWithRateLimitRetry(request: Request): Response {
        val maxRetries = UpstoxConfig.rateLimitRetries
        if (maxRetries <= 0) {
            return httpClient.newCall(request).execute()
        }

        var lastException: UpstoxApiException? = null
        repeat(maxRetries + 1) { attempt ->
            val response = httpClient.newCall(request).execute()
            if (response.code != 429) {
                return response
            }

            response.close()
            lastException = UpstoxApiException.rateLimitExceeded()

            if (attempt < maxRetries) {
                val delayMs = (1L shl attempt) * 1000 // Exponential backoff: 1s, 2s, 4s, ...
                Thread.sleep(delayMs)
            }
        }
        throw lastException ?: UpstoxApiException.rateLimitExceeded()
    }

    private fun handleError(responseBody: String, statusCode: Int, statusMessage: String = ""): Nothing {
        try {
            val errorResponse = gson.fromJson(responseBody, ErrorResponse::class.java)
            if (!errorResponse?.errors.isNullOrEmpty()) {
                val primary = errorResponse.errors.first()
                throw UpstoxApiException(
                    message = primary.message,
                    httpStatusCode = statusCode,
                    errorCode = primary.errorCode,
                    errors = errorResponse.errors,
                    rawResponse = responseBody
                )
            }
        } catch (e: Exception) {
            throw e
        }

        // Always preserve raw response even when parsing fails
        val message = if (statusMessage.isNotBlank()) {
            "HTTP $statusCode: $statusMessage"
        } else {
            "API request failed with status $statusCode"
        }
        throw UpstoxApiException(
            message = message,
            httpStatusCode = statusCode,
            rawResponse = responseBody
        )
    }

    override fun close() {
        // OkHttpClient lifecycle is managed by OkHttpClientFactory
    }
}

private data class ErrorResponse(
    val status: String?,
    val errors: List<UpstoxError>?
)

/**
 * TypeAdapterFactory that handles unknown enum values gracefully.
 *
 * When the API returns an enum value that doesn't exist in the SDK's enum definition,
 * this adapter returns null instead of throwing an exception. This prevents SDK breakage
 * when Upstox adds new enum values without SDK updates.
 */
private class SafeEnumTypeAdapterFactory : TypeAdapterFactory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val rawType = type.rawType
        if (!rawType.isEnum) return null

        val enumConstants = rawType.enumConstants as Array<Enum<*>>
        val nameToConstant = enumConstants.associateBy { it.name }

        return object : TypeAdapter<T>() {
            override fun write(out: JsonWriter, value: T?) {
                if (value == null) {
                    out.nullValue()
                } else {
                    out.value((value as Enum<*>).name)
                }
            }

            override fun read(`in`: JsonReader): T? {
                if (`in`.peek() == JsonToken.NULL) {
                    `in`.nextNull()
                    return null
                }
                val value = `in`.nextString()
                return nameToConstant[value] as T?
            }
        }
    }
}

/**
 * Flexible date deserializer that handles multiple date formats from Upstox API.
 *
 * Uses thread-safe DateTimeFormatter instead of SimpleDateFormat.
 *
 * Supports:
 * - ISO 8601 with milliseconds and timezone: "2024-01-15T10:30:00.000+05:30"
 * - ISO 8601 with timezone: "2024-01-15T10:30:00+05:30"
 * - ISO 8601 with milliseconds: "2024-01-15T10:30:00.000"
 * - ISO 8601 basic: "2024-01-15T10:30:00"
 * - Date only: "2024-01-15"
 */
private class FlexibleDateDeserializer : JsonDeserializer<Date> {
    companion object {
        private val OFFSET_DATETIME_WITH_MILLIS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        private val OFFSET_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
        private val LOCAL_DATETIME_WITH_MILLIS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        private val LOCAL_DATETIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        private val LOCAL_DATE = DateTimeFormatter.ISO_LOCAL_DATE
        private val DEFAULT_ZONE = ZoneId.systemDefault()
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date? {
        if (json == null || json.isJsonNull) return null

        val dateString = json.asString
        if (dateString.isBlank()) return null

        // Try parsing with offset (timezone-aware formats)
        tryParseOffsetDateTime(dateString, OFFSET_DATETIME_WITH_MILLIS)?.let { return it }
        tryParseOffsetDateTime(dateString, OFFSET_DATETIME)?.let { return it }

        // Try parsing without offset (local datetime formats)
        tryParseLocalDateTime(dateString, LOCAL_DATETIME_WITH_MILLIS)?.let { return it }
        tryParseLocalDateTime(dateString, LOCAL_DATETIME)?.let { return it }

        // Try parsing date only
        tryParseLocalDate(dateString)?.let { return it }

        // If all formats fail, return null instead of throwing
        return null
    }

    private fun tryParseOffsetDateTime(dateString: String, formatter: DateTimeFormatter): Date? {
        return try {
            val odt = OffsetDateTime.parse(dateString, formatter)
            Date.from(odt.toInstant())
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private fun tryParseLocalDateTime(dateString: String, formatter: DateTimeFormatter): Date? {
        return try {
            val ldt = LocalDateTime.parse(dateString, formatter)
            Date.from(ldt.atZone(DEFAULT_ZONE).toInstant())
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private fun tryParseLocalDate(dateString: String): Date? {
        return try {
            val ld = LocalDate.parse(dateString, LOCAL_DATE)
            Date.from(ld.atStartOfDay(DEFAULT_ZONE).toInstant())
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
