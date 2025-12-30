package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse

/** Retrieves the funds and margin details for the user. */
@JvmSynthetic
internal fun executeGetFundsAndMargin(apiClient: ApiClient, segment: FundSegment? = null): FundsAndMargin {
    val queryParams = mutableMapOf<String, String?>()
    segment?.let { queryParams["segment"] = it.segment }

    val response: UpstoxResponse<FundsAndMargin> = apiClient.get(
        endpoint = "/user/get-funds-and-margin",
        queryParams = queryParams,
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}

/**
 * User funds and margin information.
 *
 * @property equity Equity segment funds and margin details
 * @property commodity Commodity segment funds and margin details
 * @see <a href="https://upstox.com/developer/api-documentation/get-user-fund-margin">Get Funds and Margin API</a>
 */
data class FundsAndMargin(
    @JsonProperty("equity")
    val equity: SegmentMargin?,

    @JsonProperty("commodity")
    val commodity: SegmentMargin?
)

/**
 * Margin details for a specific segment.
 *
 * @property usedMargin Amount blocked in open positions
 * @property payinAmount Instant deposit amount reflected
 * @property spanMargin Futures/options margin blocked for SPAN
 * @property adhocMargin Manually credited margin by broker
 * @property notionalCash Amount reserved for withdrawals
 * @property availableMargin Total tradable margin balance
 * @property exposureMargin Futures/options margin blocked for exposure
 */
data class SegmentMargin(
    @JsonProperty("used_margin")
    val usedMargin: Double?,

    @JsonProperty("payin_amount")
    val payinAmount: Double?,

    @JsonProperty("span_margin")
    val spanMargin: Double?,

    @JsonProperty("adhoc_margin")
    val adhocMargin: Double?,

    @JsonProperty("notional_cash")
    val notionalCash: Double?,

    @JsonProperty("available_margin")
    val availableMargin: Double?,

    @JsonProperty("exposure_margin")
    val exposureMargin: Double?
)

/**
 * Fund margin segment filter.
 *
 * Used when querying user funds and margin.
 */
enum class FundSegment(val segment: String) {
    /** Equity segment (Securities) */
    SECURITIES("SEC"),

    /** Commodity segment */
    COMMODITY("COM")
}
