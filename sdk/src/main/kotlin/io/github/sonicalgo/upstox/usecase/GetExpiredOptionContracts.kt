package io.github.sonicalgo.upstox.usecase

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.ExpiredContract
import io.github.sonicalgo.upstox.validation.Validators

/** Gets expired option contracts for an underlying and expiry date. */
@JvmSynthetic
internal fun executeGetExpiredOptionContracts(
    apiClient: ApiClient,
    instrumentKey: String,
    expiryDate: String
): List<ExpiredContract> {
    Validators.validateInstrumentKey(instrumentKey)
    Validators.validateDateYYYYMMDD(expiryDate, "expiryDate")

    val response: UpstoxResponse<List<ExpiredContract>> = apiClient.get(
        endpoint = "/expired-instruments/option/contract",
        queryParams = mapOf("instrument_key" to instrumentKey, "expiry_date" to expiryDate),
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}
