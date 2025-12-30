package io.github.sonicalgo.upstox.usecase

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V3
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.CandleUnit
import io.github.sonicalgo.upstox.common.CandleData
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Gets historical candle data. */
@JvmSynthetic
internal fun executeGetHistoricalCandles(
    apiClient: ApiClient,
    instrumentKey: String,
    unit: CandleUnit,
    interval: Int,
    toDate: String,
    fromDate: String? = null
): CandleData {
    val encodedKey = URLEncoder.encode(instrumentKey, StandardCharsets.UTF_8)
    val endpoint = buildString {
        append("/historical-candle/$encodedKey/$unit/$interval/$toDate")
        fromDate?.let { append("/$it") }
    }

    val response: UpstoxResponse<CandleData> = apiClient.get(
        endpoint = endpoint,
        overrideBaseUrl = BASE_URL_V3
    )
    return response.dataOrThrow()
}
