package io.github.sonicalgo.upstox.usecase

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V3
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.Position

/** Gets MTF (Margin Trading Facility) positions. */
@JvmSynthetic
internal fun executeGetMtfPositions(apiClient: ApiClient): List<Position> {
    val response: UpstoxResponse<List<Position>> = apiClient.get(
        endpoint = "/portfolio/mtf-positions",
        overrideBaseUrl = BASE_URL_V3
    )
    return response.dataOrThrow()
}
