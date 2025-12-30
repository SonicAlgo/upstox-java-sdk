package io.github.sonicalgo.upstox.util

import io.github.sonicalgo.core.client.HttpClient

/**
 * Parses a JSON response with a "data" object containing key-value pairs into a Map.
 *
 * @param rawResponse Raw JSON response string
 * @return Map of keys to parsed objects of type T
 */
internal inline fun <reified T> parseMapResponse(rawResponse: String): Map<String, T> {
    val rootNode = HttpClient.objectMapper.readTree(rawResponse)
    val dataNode = rootNode.get("data") ?: return emptyMap()
    val result = mutableMapOf<String, T>()

    dataNode.fieldNames().forEach { key ->
        val value = HttpClient.objectMapper.treeToValue(dataNode.get(key), T::class.java)
        result[key] = value
    }

    return result
}