package io.github.sonicalgo.upstox.usecase

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.Candle
import io.github.sonicalgo.upstox.common.CandleData
import io.github.sonicalgo.upstox.validation.Validators
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Gets historical candle data for an expired instrument. */
@JvmSynthetic
internal fun executeGetExpiredHistoricalCandles(
    apiClient: ApiClient,
    expiredInstrumentKey: String,
    interval: String,
    toDate: String,
    fromDate: String
): List<Candle> {
    Validators.validateExpiredInstrumentKey(expiredInstrumentKey, "expiredInstrumentKey")
    Validators.validateExpiredCandleInterval(interval)
    Validators.validateDateYYYYMMDD(toDate, "toDate")
    Validators.validateDateYYYYMMDD(fromDate, "fromDate")

    val encodedKey = URLEncoder.encode(expiredInstrumentKey, StandardCharsets.UTF_8)
    val endpoint = "/expired-instruments/historical-candle/$encodedKey/$interval/$toDate/$fromDate"

    val response: UpstoxResponse<CandleData> = apiClient.get(
        endpoint = endpoint,
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow().toCandles()
}
