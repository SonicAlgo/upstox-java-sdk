package io.github.sonicalgo.upstox.usecase

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.validation.Validators

/** Gets available expiry dates for expired instruments. */
@JvmSynthetic
internal fun executeGetExpiries(apiClient: ApiClient, instrumentKey: String): List<String> {
    Validators.validateInstrumentKey(instrumentKey)

    val response: UpstoxResponse<List<String>> = apiClient.get(
        endpoint = "/expired-instruments/expiries",
        queryParams = mapOf("instrument_key" to instrumentKey),
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}
