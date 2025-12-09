package io.github.sonicalgo.upstox.util

import com.fasterxml.jackson.databind.JsonNode
import io.github.sonicalgo.core.client.HttpClient

/**
 * Converts an object to query parameters map using Jackson.
 *
 * Useful for converting data class instances to query parameters
 * where field names match query parameter names.
 *
 * @param obj The object to convert
 * @return Map of query parameter names to values
 */
fun toQueryParams(obj: Any): Map<String, String?> {
    val jsonNode = HttpClient.objectMapper.valueToTree<JsonNode>(obj)
    return jsonNode.properties().associate { (key, value) ->
        key to when {
            value.isNull -> null
            value.isTextual -> value.asText()
            value.isNumber -> value.numberValue().toString()
            value.isBoolean -> value.asBoolean().toString()
            else -> value.toString()
        }
    }
}
