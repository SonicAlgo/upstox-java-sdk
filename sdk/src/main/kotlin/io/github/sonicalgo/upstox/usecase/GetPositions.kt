package io.github.sonicalgo.upstox.usecase

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.Position

/** Gets all current positions. */
@JvmSynthetic
internal fun executeGetPositions(apiClient: ApiClient): List<Position> {
    val response: UpstoxResponse<List<Position>> = apiClient.get(
        endpoint = "/portfolio/short-term-positions",
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}
