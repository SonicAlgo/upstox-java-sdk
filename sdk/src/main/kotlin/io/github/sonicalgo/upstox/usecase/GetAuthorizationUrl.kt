package io.github.sonicalgo.upstox.usecase

import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Constructs the authorization URL for initiating OAuth flow. */
@JvmSynthetic
internal fun executeGetAuthorizationUrl(
    clientId: String,
    redirectUri: String,
    state: String? = null
): String {
    val baseUrl = "${BASE_URL_V2}/login/authorization/dialog"
    val queryParams = buildString {
        append("response_type=code")
        append("&client_id=${URLEncoder.encode(clientId, StandardCharsets.UTF_8)}")
        append("&redirect_uri=${URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)}")
        if (state != null) {
            append("&state=${URLEncoder.encode(state, StandardCharsets.UTF_8)}")
        }
    }
    return "$baseUrl?$queryParams"
}
